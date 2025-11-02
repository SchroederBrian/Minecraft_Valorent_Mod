package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DropWeaponPacket() implements CustomPacketPayload {
    public static final Type<DropWeaponPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "drop_weapon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DropWeaponPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new DropWeaponPacket()
    );

    public static void handle(DropWeaponPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sp)) {
            return;
        }

        // Use the existing drop logic from DropPickupApi
        var result = com.bobby.valorant.drop.DropPickupApi.tryDropCurrentItem(sp);

        if (result == net.minecraft.world.InteractionResult.SUCCESS) {
            com.bobby.valorant.Valorant.LOGGER.info("[DROP] Player {} dropped weapon via G key", sp.getName().getString());
        }
    }

    @Override
    public Type<DropWeaponPacket> type() {
        return TYPE;
    }
}
