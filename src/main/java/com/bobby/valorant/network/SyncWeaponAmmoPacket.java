package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.WeaponAmmoData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncWeaponAmmoPacket(int slot, int currentAmmo, int reserveAmmo) implements CustomPacketPayload {
    public static final Type<SyncWeaponAmmoPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_weapon_ammo"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncWeaponAmmoPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.slot());
                buf.writeVarInt(p.currentAmmo());
                buf.writeVarInt(p.reserveAmmo());
            },
            buf -> new SyncWeaponAmmoPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    public static void handle(SyncWeaponAmmoPacket packet, IPayloadContext context) {
        if (context.player() == null) {
            return;
        }

        // Update the ammo data for the weapon in the specified slot
        ItemStack stack = context.player().getInventory().getItem(packet.slot);
        if (!stack.isEmpty()) {
            WeaponAmmoData.setCurrentAmmo(stack, packet.currentAmmo);
            WeaponAmmoData.setReserveAmmo(stack, packet.reserveAmmo);
        }
    }

    @Override
    public Type<SyncWeaponAmmoPacket> type() {
        return TYPE;
    }
}
