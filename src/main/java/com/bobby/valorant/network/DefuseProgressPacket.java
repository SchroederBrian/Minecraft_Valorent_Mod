package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DefuseProgressPacket(int ticksRemaining) implements CustomPacketPayload {
    public static final Type<DefuseProgressPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "defuse_progress"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DefuseProgressPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVarInt(packet.ticksRemaining()),
            buf -> new DefuseProgressPacket(buf.readVarInt()));

    @Override
    public Type<DefuseProgressPacket> type() { return TYPE; }
}


