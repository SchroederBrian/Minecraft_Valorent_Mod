package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.ModKeyBindings;
import com.bobby.valorant.network.ReloadWeaponPacket;
import com.bobby.valorant.world.item.IWeapon;
import com.bobby.valorant.world.item.WeaponAmmoData;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class ClientWeaponEvents {

    private ClientWeaponEvents() {}

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (ModKeyBindings.RELOAD_WEAPON.consumeClick()) {
            handleReload(mc.player);
        }
    }

    private static void handleReload(Player player) {
        ItemStack heldStack = player.getMainHandItem();
        if (heldStack.isEmpty() || !(heldStack.getItem() instanceof IWeapon weapon)) {
            return;
        }

        int currentAmmo = WeaponAmmoData.getCurrentAmmo(heldStack);
        int magSize = weapon.getMagazineSize();

        if (currentAmmo >= magSize) {
            return; // Already full
        }

        int reserveAmmo = WeaponAmmoData.getReserveAmmo(heldStack);
        if (reserveAmmo <= 0) {
            return; // No ammo to reload
        }

        // Send reload packet to server - let server handle the logic and sync back
        ClientPacketDistributor.sendToServer(new ReloadWeaponPacket());
    }
}
