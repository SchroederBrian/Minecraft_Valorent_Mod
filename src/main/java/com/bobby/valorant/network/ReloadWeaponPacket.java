package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.IWeapon;
import com.bobby.valorant.world.item.WeaponAmmoData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
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

        // Play reload sound
        com.bobby.valorant.util.SoundManager.playReloadSound(sp);

        // Sync the updated ammo back to the client
        int newCurrentAmmo = WeaponAmmoData.getCurrentAmmo(weaponStack);
        int newReserveAmmo = WeaponAmmoData.getReserveAmmo(weaponStack);
        PacketDistributor.sendToPlayer(sp, new SyncWeaponAmmoPacket(weaponSlot, newCurrentAmmo, newReserveAmmo));
    }

    @Override
    public Type<ReloadWeaponPacket> type() {
        return TYPE;
    }
}
