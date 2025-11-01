package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.ReloadStateData;
import com.bobby.valorant.round.RoundController;
import com.bobby.valorant.spike.SpikePlantingHandler;
import com.bobby.valorant.spike.SpikeDefusingHandler;
import com.bobby.valorant.world.entity.FireWallEntity;
import com.bobby.valorant.world.item.IWeapon;
import com.bobby.valorant.world.item.WeaponAmmoData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class RoundTickEvents {
    private RoundTickEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null) return;
        // Drive per-level controllers
        for (var level : event.getServer().getAllLevels()) {
            RoundController.get(level).tick();
            // Planting progress
            if (!level.isClientSide()) SpikePlantingHandler.tick(level);
            if (!level.isClientSide()) SpikeDefusingHandler.tick(level);
        }

        // Tick active fire walls
        FireWallEntity.tickAll();

        // Tick reload timers and complete reloads
        for (var level : event.getServer().getAllLevels()) {
            if (level.isClientSide()) continue;

            for (var player : level.players()) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    // Check for weapon switches (cancel reload if switched away from weapon)
                    checkWeaponSwitch(serverPlayer);

                    // Check if reload just completed
                    boolean wasReloading = ReloadStateData.isReloading(serverPlayer);
                    ReloadStateData.tick(serverPlayer);

                    // If reload just completed, perform the ammo transfer
                    if (wasReloading && !ReloadStateData.isReloading(serverPlayer)) {
                        completeReload(serverPlayer);
                    }
                }
            }
        }
    }

    private static void completeReload(net.minecraft.server.level.ServerPlayer player) {
        // Find the weapon that was being reloaded
        var inventory = player.getInventory();
        int selectedSlot;
        try {
            var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            selectedSlot = field.getInt(inventory);
        } catch (Exception e) {
            selectedSlot = 0; // fallback
        }

        var weaponStack = inventory.getItem(selectedSlot);
        if (weaponStack.isEmpty() || !(weaponStack.getItem() instanceof IWeapon weapon)) {
            return;
        }

        int currentAmmo = WeaponAmmoData.getCurrentAmmo(weaponStack);
        int magSize = weapon.getMagazineSize();

        if (currentAmmo >= magSize) {
            return; // Already full
        }

        int reserveAmmo = WeaponAmmoData.getReserveAmmo(weaponStack);
        if (reserveAmmo <= 0) {
            return; // No ammo to reload
        }

        int needed = magSize - currentAmmo;
        int toReload = Math.min(needed, reserveAmmo);

        WeaponAmmoData.setCurrentAmmo(weaponStack, currentAmmo + toReload);
        WeaponAmmoData.setReserveAmmo(weaponStack, reserveAmmo - toReload);

        // Sync the updated ammo back to the client
        int newCurrentAmmo = WeaponAmmoData.getCurrentAmmo(weaponStack);
        int newReserveAmmo = WeaponAmmoData.getReserveAmmo(weaponStack);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
            new com.bobby.valorant.network.SyncWeaponAmmoPacket(selectedSlot, newCurrentAmmo, newReserveAmmo));
    }

    private static void checkWeaponSwitch(net.minecraft.server.level.ServerPlayer player) {
        // Check if player is reloading and has switched away from weapon
        if (!ReloadStateData.isReloading(player)) {
            return;
        }

        var inventory = player.getInventory();
        int selectedSlot;
        try {
            var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            selectedSlot = field.getInt(inventory);
        } catch (Exception e) {
            selectedSlot = 0;
        }

        var currentItem = inventory.getItem(selectedSlot);
        // If the current selected item is not a weapon, cancel reload
        if (currentItem.isEmpty() || !(currentItem.getItem() instanceof IWeapon)) {
            ReloadStateData.cancelReload(player);
        }
    }
}


