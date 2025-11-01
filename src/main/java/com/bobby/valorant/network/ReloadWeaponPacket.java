package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.ReloadStateData;
import com.bobby.valorant.world.item.IWeapon;
import com.bobby.valorant.world.item.WeaponAmmoData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReloadWeaponPacket() implements CustomPacketPayload {
    public static final Type<ReloadWeaponPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "reload_weapon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadWeaponPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new ReloadWeaponPacket()
    );

    public static void handle(ReloadWeaponPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sp)) {
            return;
        }

        // Find the weapon in the player's inventory (prefer main hand, fallback to inventory)
        ItemStack weaponStack = sp.getMainHandItem();
        int weaponSlot;
        try {
            var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            weaponSlot = field.getInt(sp.getInventory());
        } catch (Exception e) {
            weaponSlot = 0; // fallback
        }

        // If main hand isn't a weapon, check inventory slots
        if (weaponStack.isEmpty() || !(weaponStack.getItem() instanceof IWeapon)) {
            for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
                ItemStack stack = sp.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof IWeapon) {
                    weaponStack = stack;
                    weaponSlot = i;
                    break;
                }
            }
        }

        if (weaponStack.isEmpty() || !(weaponStack.getItem() instanceof IWeapon weapon)) {
            return;
        }

        // Check if already reloading
        if (ReloadStateData.isReloading(sp)) {
            return; // Can't start another reload while one is in progress
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

        // Start reload timer
        int reloadTimeTicks = weapon.getReloadTimeTicks();
        ReloadStateData.startReload(sp, weaponSlot, reloadTimeTicks);

        // Play reload sound
        com.bobby.valorant.Valorant.LOGGER.info("[Reload] Starting reload for {}, slot={}, durationTicks={}", sp.getGameProfile().getName(), weaponSlot, reloadTimeTicks);
        com.bobby.valorant.util.SoundManager.playReloadSound(sp);
    }

    @Override
    public Type<ReloadWeaponPacket> type() {
        return TYPE;
    }
}
