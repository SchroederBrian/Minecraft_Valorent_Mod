package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PlantingProgressPacket(int ticksRemaining) implements CustomPacketPayload {
    public static final Type<PlantingProgressPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "planting_progress"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlantingProgressPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVarInt(packet.ticksRemaining()),
            buf -> new PlantingProgressPacket(buf.readVarInt()));

    @Override
    public Type<PlantingProgressPacket> type() { return TYPE; }
}


