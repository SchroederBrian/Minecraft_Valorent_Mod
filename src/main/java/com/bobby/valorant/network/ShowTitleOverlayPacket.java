package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.hud.TitleOverlay;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ShowTitleOverlayPacket(String title, String subtitle,
                                     int fadeInTicks, int stayTicks, int fadeOutTicks,
                                     int titleColor, int subtitleColor) implements CustomPacketPayload {

    public static final Type<ShowTitleOverlayPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "show_title_overlay"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowTitleOverlayPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeUtf(p.title());
                buf.writeUtf(p.subtitle());
                buf.writeVarInt(p.fadeInTicks());
                buf.writeVarInt(p.stayTicks());
                buf.writeVarInt(p.fadeOutTicks());
                buf.writeVarInt(p.titleColor());
                buf.writeVarInt(p.subtitleColor());
            },
            buf -> new ShowTitleOverlayPacket(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShowTitleOverlayPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Hide any active progress overlay before showing success/failure messages
            TitleOverlay.hide();

            // If this is a defuse success message, mark spike as defused on client
            if ("SPIKE DEFUSED".equals(packet.title())) {
                com.bobby.valorant.events.SpikeClientEvents.setSpikeDefused(true);
            }

            TitleOverlay.show(
                    packet.title, packet.subtitle,
                    packet.fadeInTicks, packet.stayTicks, packet.fadeOutTicks,
                    packet.titleColor, packet.subtitleColor
            );
        });
    }
}
