package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.round.RoundState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncRoundStatePacket(boolean running, int remainingSeconds, int leftScore, int rightScore, int phaseOrdinal)
        implements CustomPacketPayload {
    public static final Type<SyncRoundStatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_round_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRoundStatePacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBoolean(p.running());
                buf.writeVarInt(p.remainingSeconds());
                buf.writeVarInt(p.leftScore());
                buf.writeVarInt(p.rightScore());
                buf.writeVarInt(p.phaseOrdinal());
            },
            buf -> new SyncRoundStatePacket(buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    public static void handle(SyncRoundStatePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            RoundState.update(packet.running(), packet.remainingSeconds(), packet.leftScore(), packet.rightScore());
            RoundState.updatePhase(packet.phaseOrdinal());
        });
    }

    @Override
    public Type<SyncRoundStatePacket> type() {
        return TYPE;
    }
}


