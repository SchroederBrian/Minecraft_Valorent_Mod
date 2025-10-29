package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.hud.FlashOverlay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TriggerFlashPacket(int windupTicks, int fullDurationTicks, int fadeDurationTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TriggerFlashPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "trigger_flash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TriggerFlashPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.windupTicks);
                buf.writeInt(packet.fullDurationTicks);
                buf.writeInt(packet.fadeDurationTicks);
            },
            buf -> new TriggerFlashPacket(buf.readInt(), buf.readInt(), buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TriggerFlashPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client-side execution
            FlashOverlay.triggerFlash(packet.windupTicks, packet.fullDurationTicks, packet.fadeDurationTicks);
        });
    }
}
