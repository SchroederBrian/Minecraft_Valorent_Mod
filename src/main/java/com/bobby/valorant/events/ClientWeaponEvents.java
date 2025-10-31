package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.ModKeyBindings;
import com.bobby.valorant.network.ReloadWeaponPacket;
import com.bobby.valorant.network.ShootGunPacket;
import com.bobby.valorant.world.item.GunItem;
import com.bobby.valorant.world.item.IWeapon;
import com.bobby.valorant.world.item.WeaponAmmoData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class ClientWeaponEvents {
    private static boolean isFiring = false;

    private ClientWeaponEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Continuous fire check
        if (isFiring) {
            tryFire(mc.player, InteractionHand.MAIN_HAND);
        }
    }

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

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        // We only care about left-click presses
        if (event.getButton() != 0 || event.getAction() != 1) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }

        isFiring = true;
        tryFire(mc.player, InteractionHand.MAIN_HAND);

        // Don't cancel the event, let vanilla handle block breaking etc. if fire fails
    }

    @SubscribeEvent
    public static void onMouseRelease(InputEvent.MouseButton.Post event) {
        // We only care about left-click releases
        if (event.getButton() != 0) return;
        isFiring = false;
    }

    private static void tryFire(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof GunItem) {
            // Send packet to server to request firing
            ClientPacketDistributor.sendToServer(new ShootGunPacket());
        }
    }
}
