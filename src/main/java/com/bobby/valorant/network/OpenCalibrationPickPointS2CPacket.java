package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCalibrationPickPointS2CPacket(
    int step,
    String prompt
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenCalibrationPickPointS2CPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "open_calibration_pick_point"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCalibrationPickPointS2CPacket> STREAM_CODEC =
        StreamCodec.of(OpenCalibrationPickPointS2CPacket::encode, OpenCalibrationPickPointS2CPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, OpenCalibrationPickPointS2CPacket packet) {
        buf.writeVarInt(packet.step());
        buf.writeUtf(packet.prompt());
    }

    private static OpenCalibrationPickPointS2CPacket decode(RegistryFriendlyByteBuf buf) {
        int step = buf.readVarInt();
        String prompt = buf.readUtf();
        return new OpenCalibrationPickPointS2CPacket(step, prompt);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenCalibrationPickPointS2CPacket packet, IPayloadContext context) {
        // Client-side: open HavenMapScreen in calibration mode
        context.enqueueWork(() -> {
            com.bobby.valorant.client.SkySmokeScreen.openCalibrationMode(packet.step(), packet.prompt());
        });
    }
}
