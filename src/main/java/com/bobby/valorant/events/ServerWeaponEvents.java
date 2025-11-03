package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.round.RoundController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class ServerWeaponEvents {
    private ServerWeaponEvents() {}

    @SubscribeEvent
    public static void onDeathRemoveWeapons(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        Vec3 deathPos = new Vec3(sp.getX(), sp.getY(), sp.getZ());

        // Drop Spike if present using armor stand system
        int invSize = sp.getInventory().getContainerSize();
        for (int i = 0; i < invSize; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.is(ModItems.SPIKE.get())) {
                com.bobby.valorant.drop.DropPickupApi.dropItemAtPosition((net.minecraft.server.level.ServerLevel) sp.level(), deathPos, s.copyWithCount(1));
                sp.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        // Find first primary (rifle-like) to drop using armor stand system
        ItemStack toDrop = ItemStack.EMPTY;
        int dropSlot = -1;
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            if (isPrimaryWeapon(s)) { toDrop = s.copy(); dropSlot = i; break; }
        }
        if (!toDrop.isEmpty()) {
            com.bobby.valorant.drop.DropPickupApi.dropItemAtPosition((net.minecraft.server.level.ServerLevel) sp.level(), deathPos, toDrop.copyWithCount(1));
            // remove from inventory so it doesn't also persist
            if (dropSlot >= 0) sp.getInventory().setItem(dropSlot, ItemStack.EMPTY);
        }
        // Remove purchased weapons; keep knife
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(ModItems.KNIFE.get())) continue;
            // Consider anything that matches primary/secondary set to be removable
            boolean isPrimary = isPrimaryWeapon(s);
            boolean isSecondary = s.is(ModItems.GHOST.get());
            if (isPrimary || isSecondary) {
                sp.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
        // Ensure knife present
        ensureKnife(sp);

        // Persist death location for custom spectator flow
        com.bobby.valorant.player.SpectatorData.markDeath(sp);

        // Apply spectator-like effects immediately
        sp.getAbilities().mayfly = true;
        sp.getAbilities().flying = true;
        sp.onUpdateAbilities();
        sp.setInvulnerable(true);
        sp.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 60 * 60, 0, false, false));
        sp.setInvisible(true);

        // Re-apply on next tick to survive respawn overrides
        sp.getServer().execute(() -> {
            if (!sp.isAlive()) return;
            sp.getAbilities().mayfly = true;
            sp.getAbilities().flying = true;
            sp.onUpdateAbilities();
            sp.setInvulnerable(true);
            sp.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 60 * 60, 0, false, false));
            sp.setInvisible(true);
        });

        // Broadcast killfeed message if killed by a player
        var src = event.getSource();
        if (src != null && src.getEntity() instanceof ServerPlayer killer) {
            String killerName = killer.getScoreboardName();
            String victimName = sp.getScoreboardName();
            net.minecraft.world.item.ItemStack weapon = killer.getMainHandItem();
            net.minecraft.resources.ResourceLocation wid = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(weapon.getItem());
            String weaponId = wid != null ? wid.toString() : "";

            com.bobby.valorant.network.KillfeedMessageS2CPacket pkt = new com.bobby.valorant.network.KillfeedMessageS2CPacket(killerName, victimName, weaponId);
            for (ServerPlayer p : sp.getServer().getPlayerList().getPlayers()) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, pkt);
            }
        }
    }

    @SubscribeEvent
    public static void onCloneGiveKnife(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        Player p = event.getEntity();
        if (p instanceof ServerPlayer sp) {
            RoundController.ensureKnife(sp);
            // Also give a free Classic if the player lacks a sidearm
            RoundController.ensureDefaultPistol(sp);
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            // Remove all default drops; we already spawned weapons/spike using armor stands in onDeathRemoveWeapons
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onXp(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            event.setDroppedExperience(0);
        }
    }

    private static void ensureKnife(ServerPlayer sp) {
        if (!sp.getInventory().contains(ModItems.KNIFE.get().getDefaultInstance())) {
            sp.getInventory().add(ModItems.KNIFE.get().getDefaultInstance());
            sp.containerMenu.broadcastChanges();
        }
    }

    private static boolean isPrimaryWeapon(ItemStack s) {
        return s.is(ModItems.VANDAL_RIFLE.get()) ||
                s.is(net.minecraft.world.item.Items.BOW) ||
                s.is(net.minecraft.world.item.Items.TRIDENT) ||
                s.is(net.minecraft.world.item.Items.IRON_AXE) ||
                s.is(net.minecraft.world.item.Items.NETHERITE_AXE);
    }
}
