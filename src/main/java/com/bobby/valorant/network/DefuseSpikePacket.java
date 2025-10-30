package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.spike.SpikeDefusingHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DefuseSpikePacket(Action action) implements CustomPacketPayload {
    public static final Type<DefuseSpikePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "defuse_spike"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DefuseSpikePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeEnum(packet.action()),
            buf -> new DefuseSpikePacket(buf.readEnum(Action.class)));

    public enum Action { START, CANCEL }

    public static void handle(DefuseSpikePacket packet, IPayloadContext context) {
        Player player = context.player();
        if (!(player instanceof ServerPlayer sp)) return;
        if (packet.action == Action.START) SpikeDefusingHandler.startDefuse(sp);
        else SpikeDefusingHandler.cancelDefuse(sp);
    }

    @Override
    public Type<DefuseSpikePacket> type() { return TYPE; }
}


