package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSkySmokeTransformS2CPacket(
    double a, // scale * cos(theta)
    double b, // scale * sin(theta)
    double tx, // translation X
    double tz  // translation Z
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSkySmokeTransformS2CPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_sky_smoke_transform"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkySmokeTransformS2CPacket> STREAM_CODEC =
        StreamCodec.of(SyncSkySmokeTransformS2CPacket::encode, SyncSkySmokeTransformS2CPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SyncSkySmokeTransformS2CPacket packet) {
        buf.writeDouble(packet.a());
        buf.writeDouble(packet.b());
        buf.writeDouble(packet.tx());
        buf.writeDouble(packet.tz());
    }

    private static SyncSkySmokeTransformS2CPacket decode(RegistryFriendlyByteBuf buf) {
        double a = buf.readDouble();
        double b = buf.readDouble();
        double tx = buf.readDouble();
        double tz = buf.readDouble();
        return new SyncSkySmokeTransformS2CPacket(a, b, tx, tz);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSkySmokeTransformS2CPacket packet, IPayloadContext context) {
        // Client-side: store the transform for projection
        com.bobby.valorant.client.SkySmokeCalibrationClient.setTransform(packet.a(), packet.b(), packet.tx(), packet.tz());
    }
}
