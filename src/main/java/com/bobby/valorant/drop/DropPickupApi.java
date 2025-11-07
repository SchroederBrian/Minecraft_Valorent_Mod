package com.bobby.valorant.drop;

import java.util.List;

import com.bobby.valorant.Config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class DropPickupApi {
    private DropPickupApi() {}

    public static boolean isDroppable(ItemStack stack) {
        Object raw = Config.COMMON.droppableWhitelist.get();
        if (!(raw instanceof List<?> list) || list.isEmpty()) return false;

        for (Object o : list) {
            if (!(o instanceof String s)) continue;
            s = s.trim(); if (s.isEmpty()) continue;

            if (s.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(s.substring(1));
                if (id == null) continue;
                TagKey<Item> key = ItemTags.create(id);
                if (stack.is(key)) return true;
            } else {
                ResourceLocation id = ResourceLocation.tryParse(s);
                if (id == null) continue;
                if (id.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) return true;
            }
        }
        return false;
    }

    public static int getTargetSlot(ItemStack stack) {
        Object raw = Config.COMMON.itemTargetSlots.get();
        if (!(raw instanceof com.electronwill.nightconfig.core.Config conf)) return -1;
        for (java.util.Map.Entry<String, Object> e : conf.valueMap().entrySet()) {
            String key = e.getKey();
            int slotIdx; try { slotIdx = Integer.parseInt(String.valueOf(e.getValue())); } catch (Exception ex) { continue; }
            if (key.startsWith("#")) {
                var id = ResourceLocation.tryParse(key.substring(1)); if (id == null) continue;
                var tag = ItemTags.create(id);
                if (stack.is(tag)) return slotIdx;
            } else {
                var id = ResourceLocation.tryParse(key); if (id == null) continue;
                if (id.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) return slotIdx;
            }
        }
        return -1;
    }

    public static InteractionResult tryDropCurrentItem(ServerPlayer sp) {
        ItemStack hand = sp.getMainHandItem();
        if (hand.isEmpty() || !isDroppable(hand)) return InteractionResult.PASS;

        boolean allowStack = Boolean.TRUE.equals(Config.COMMON.allowStackDrop.get());
        ItemStack toDrop = allowStack ? hand.copy() : hand.copyWithCount(1);
        if (allowStack) {
            sp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            if (hand.getCount() > 1) hand.shrink(1); else sp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        sp.containerMenu.broadcastChanges();

        ServerLevel level = (ServerLevel) sp.level();
        DroppedWeaponStandEntity stand = new DroppedWeaponStandEntity(level);
        Vec3 eye = sp.getEyePosition(); Vec3 look = sp.getLookAngle();

        // Apply individual Y offset for each item type
        double itemYOffset = getItemDropYOffset(toDrop);
        stand.setPos(eye.x + look.x * 0.4D, sp.getY() + 0.1D + itemYOffset, eye.z + look.z * 0.4D);

        // Set rotation for throwing physics (keep player's look direction)
        stand.setYRot(sp.getYRot());
        stand.setXRot(sp.getXRot());

        stand.setHeadItem(toDrop);
        if (Boolean.TRUE.equals(Config.COMMON.enableGlow.get())) stand.setGlowingTag(true);

        // Apply throw impulse first (uses current rotation)
        stand.applyThrowImpulse(0.6D, 0.15D);

        // Now add random Y rotation variation for visual variety (±60 degrees)
        float randomYawOffset = (level.random.nextFloat() - 0.5F) * 180.0F; // ±90 degrees
        stand.setYRot(stand.getYRot() + randomYawOffset);
        int despawn = Math.max(0, Config.COMMON.autoDespawnTicks.get());
        if (despawn > 0) stand.scheduleAutoDespawn(despawn);
        level.addFreshEntity(stand);
        return InteractionResult.SUCCESS;
    }

    /**
     * Get Y offset for dropped items to make them appear at different heights
     */
    private static double getItemDropYOffset(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        // Pistols start slightly lower
        if ("valorant:classic".equals(itemId.toString())) {
            return Config.COMMON.classicDropYOffset.get();
        }

        // Ghost starts lower
        if ("valorant:ghost".equals(itemId.toString())) {
            return Config.COMMON.ghostDropYOffset.get();
        }

        // Spike starts higher
        if ("valorant:spike".equals(itemId.toString())) {
            return Config.COMMON.spikeDropYOffset.get();
        }

        // Rifles start highest
        if ("valorant:vandal".equals(itemId.toString())) {
            return Config.COMMON.vandalDropYOffset.get();
        }

        // Sheriff starts higher
        if ("valorant:sheriff".equals(itemId.toString())) {
            return Config.COMMON.sheriffDropYOffset.get();
        }

        // Frenzy starts highest
        if ("valorant:frenzy".equals(itemId.toString())) {
            return Config.COMMON.frenzyDropYOffset.get();
        }

        // Default offset for any other items
        return 0.0D;
    }

    /**
     * Drop a specific item stack at a given position (used for death drops)
     */
    public static void dropItemAtPosition(ServerLevel level, Vec3 position, ItemStack stack) {
        if (stack.isEmpty()) return;

        DroppedWeaponStandEntity stand = new DroppedWeaponStandEntity(level);

        // Apply individual Y offset for each item type
        double itemYOffset = getItemDropYOffset(stack);
        stand.setPos(position.x, position.y + itemYOffset - 1.5F, position.z);

        // Random rotation for visual variety (full random since no physics)
        stand.setYRot(level.random.nextFloat() * 360.0F);
        stand.setXRot((level.random.nextFloat() - 0.5F) * 180.0F); // X tilt 90 degrees
        stand.setHeadItem(stack);

        if (Boolean.TRUE.equals(Config.COMMON.enableGlow.get())) stand.setGlowingTag(true);

        // No throw impulse for death drops - just place it
        stand.lockToCurrentPose(); // Lock it immediately

        int despawn = Math.max(0, Config.COMMON.autoDespawnTicks.get());
        if (despawn > 0) stand.scheduleAutoDespawn(despawn);

        level.addFreshEntity(stand);

        if (Boolean.TRUE.equals(Config.COMMON.enableDropSfx.get()) && Boolean.TRUE.equals(Config.COMMON.soundEnabled.get())) {
            level.playSound(null, position.x, position.y, position.z, net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM, net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.0F);
        }
        if (Boolean.TRUE.equals(Config.COMMON.enableParticles.get())) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, position.x, position.y + 0.2, position.z, 8, 0.05, 0.05, 0.05, 0.01);
        }
    }

    public static InteractionResult tryPickupStand(ServerPlayer sp, DroppedWeaponStandEntity stand) {
        return stand.interact(sp, InteractionHand.MAIN_HAND);
    }
}


