package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSkySmokeTransformS2CPacket(
    String model,
    double[] H
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSkySmokeTransformS2CPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_sky_smoke_transform"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkySmokeTransformS2CPacket> STREAM_CODEC =
        StreamCodec.of(SyncSkySmokeTransformS2CPacket::encode, SyncSkySmokeTransformS2CPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SyncSkySmokeTransformS2CPacket packet) {
        buf.writeUtf(packet.model());
        // Write 9 doubles
        int len = packet.H() != null ? packet.H().length : 0;
        if (len != 9) {
            // write identity if malformed
            buf.writeVarInt(9);
            for (int i=0;i<9;i++) buf.writeDouble(i%4==0?1.0:0.0);
        } else {
            buf.writeVarInt(9);
            for (int i=0;i<9;i++) buf.writeDouble(packet.H()[i]);
        }
    }

    private static SyncSkySmokeTransformS2CPacket decode(RegistryFriendlyByteBuf buf) {
        String model = buf.readUtf();
        int n = buf.readVarInt();
        double[] H = new double[Math.max(9, n)];
        for (int i=0;i<n;i++) H[i]=buf.readDouble();
        if (n < 9) {
            // identity fallback
            H = new double[]{1,0,0, 0,1,0, 0,0,1};
        }
        return new SyncSkySmokeTransformS2CPacket(model, H);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSkySmokeTransformS2CPacket packet, IPayloadContext context) {
        // Client-side: store the transform for projection
        com.bobby.valorant.client.SkySmokeCalibrationClient.setTransform(packet.model(), packet.H());
    }
}
