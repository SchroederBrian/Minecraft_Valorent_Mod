package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.ModKeyBindings;
import com.bobby.valorant.drop.DroppedWeaponStandEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class DropPickupClientEvents {
    private DropPickupClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        // Check if G key was pressed for drop/pickup
        if (ModKeyBindings.DROP_PICKUP_WEAPON.consumeClick()) {
            // First check if we can pickup
            if (tryPickupWeapon(player, level)) {
                return; // Pickup attempted
            }

            // Otherwise try to drop
            tryDropWeapon(player);
        }
    }

    private static boolean tryPickupWeapon(LocalPlayer player, ClientLevel level) {
        double range = com.bobby.valorant.Config.COMMON.pickupRange.get();

        // Find the best (closest) DroppedWeaponStandEntity within range and LOS
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB box = player.getBoundingBox().inflate(range);

        List<DroppedWeaponStandEntity> nearby = level.getEntitiesOfClass(DroppedWeaponStandEntity.class, box);
        DroppedWeaponStandEntity best = null;
        double bestDist2 = Double.MAX_VALUE;

        for (DroppedWeaponStandEntity stand : nearby) {
            if (stand.isRemoved()) continue;
            ItemStack head = stand.getItemBySlot(EquipmentSlot.HEAD);
            if (head.isEmpty()) continue;

            // Account for item-specific Y offset when checking pickup position
            double itemYOffset = getItemPickupYOffset(head);
            Vec3 target = new Vec3(stand.getX(), stand.getY() + 0.4 + itemYOffset, stand.getZ());
            double dist2 = eye.distanceToSqr(target);
            if (dist2 > range * range) continue;

            // Check line-of-sight
            ClipContext ctx = new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
            BlockHitResult hit = level.clip(ctx);
            boolean unobstructed = hit.getType() == HitResult.Type.MISS ||
                    hit.getLocation().distanceToSqr(eye) >= dist2 - 1.0E-3;

            if (!unobstructed) continue;

            // Prefer closest
            if (dist2 < bestDist2) {
                bestDist2 = dist2;
                best = stand;
            }
        }

        if (best != null) {
            // Send pickup request to server
            ClientPacketDistributor.sendToServer(
                new com.bobby.valorant.network.PickupWeaponPacket(best.getId())
            );
            return true; // Pickup attempted
        }
        return false; // No pickup target found
    }

    private static void tryDropWeapon(LocalPlayer player) {
        // Check if player has a droppable item in main hand
        var mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty()) return;

        // Check if it's droppable (client-side check for UX, server will validate)
        if (!com.bobby.valorant.drop.DropPickupApi.isDroppable(mainHandItem)) return;

        // Send drop request to server
        ClientPacketDistributor.sendToServer(
            new com.bobby.valorant.network.DropWeaponPacket()
        );
    }

    /**
     * Get Y offset for pickup detection to match the drop offsets
     */
    private static double getItemPickupYOffset(net.minecraft.world.item.ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        // Pistols
        if ("valorant:classic".equals(itemId.toString())) {
            return com.bobby.valorant.Config.COMMON.classicDropYOffset.get();
        }
        if ("valorant:ghost".equals(itemId.toString())) {
            return com.bobby.valorant.Config.COMMON.ghostDropYOffset.get();
        }

        // Spike
        if ("valorant:spike".equals(itemId.toString())) {
            return com.bobby.valorant.Config.COMMON.spikeDropYOffset.get();
        }

        // Rifles
        if ("valorant:vandal".equals(itemId.toString())) {
            return com.bobby.valorant.Config.COMMON.vandalDropYOffset.get();
        }

        // Sheriff
        if ("valorant:sheriff".equals(itemId.toString())) {
            return com.bobby.valorant.Config.COMMON.sheriffDropYOffset.get();
        }

        // Default offset for any other items
        return 0.0D;
    }
}
