package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSkySmokeCalibrationS2CPacket(int minX, int minZ, int maxX, int maxZ, double rotationDegrees) implements CustomPacketPayload {
    public static final Type<SyncSkySmokeCalibrationS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_sky_smoke_calibration"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkySmokeCalibrationS2CPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeVarInt(packet.minX());
            buf.writeVarInt(packet.minZ());
            buf.writeVarInt(packet.maxX());
            buf.writeVarInt(packet.maxZ());
            buf.writeDouble(packet.rotationDegrees());
        },
        buf -> new SyncSkySmokeCalibrationS2CPacket(
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readDouble()
        )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSkySmokeCalibrationS2CPacket packet, IPayloadContext context) {
        // Client-side: store the bounds for map projection
        com.bobby.valorant.client.SkySmokeCalibrationClient.setBounds(
            packet.minX(), packet.minZ(), packet.maxX(), packet.maxZ(), packet.rotationDegrees()
        );
    }
}
