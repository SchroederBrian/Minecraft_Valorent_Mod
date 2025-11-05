package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SubmitCalibrationPointC2SPacket(
    int step,
    double u,
    double v
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SubmitCalibrationPointC2SPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "submit_calibration_point"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubmitCalibrationPointC2SPacket> STREAM_CODEC =
        StreamCodec.of(SubmitCalibrationPointC2SPacket::encode, SubmitCalibrationPointC2SPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SubmitCalibrationPointC2SPacket packet) {
        buf.writeVarInt(packet.step());
        buf.writeDouble(packet.u());
        buf.writeDouble(packet.v());
    }

    private static SubmitCalibrationPointC2SPacket decode(RegistryFriendlyByteBuf buf) {
        int step = buf.readVarInt();
        double u = buf.readDouble();
        double v = buf.readDouble();
        return new SubmitCalibrationPointC2SPacket(step, u, v);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SubmitCalibrationPointC2SPacket packet, IPayloadContext context) {
        // Server-side: receive the UV coordinates and continue calibration
        var player = context.player();
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            com.bobby.valorant.spawn.SkySmokeCalibration.receiveCalibrationPoint(sp.getServer(), sp.level().dimension().location(), packet.step(), packet.u(), packet.v());
        }
    }
}
