package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.ReloadStateData;
import com.bobby.valorant.world.item.IWeapon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class WeaponSwitchEvents {
    private WeaponSwitchEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;

        // Check if player is reloading and has switched weapons
        if (ReloadStateData.isReloading(serverPlayer)) {
            var inventory = serverPlayer.getInventory();
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
                ReloadStateData.cancelReload(serverPlayer);
            }
        }
    }
}
