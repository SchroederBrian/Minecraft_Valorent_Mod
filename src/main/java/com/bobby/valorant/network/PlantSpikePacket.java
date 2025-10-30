package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.spike.SpikePlantingHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlantSpikePacket(Action action) implements CustomPacketPayload {
    public static final Type<PlantSpikePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "plant_spike"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlantSpikePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeEnum(packet.action()),
            buf -> new PlantSpikePacket(buf.readEnum(Action.class)));

    public enum Action { START, CANCEL }

    public static void handle(PlantSpikePacket packet, IPayloadContext context) {
        Player player = context.player();
        if (!(player instanceof ServerPlayer sp)) return;
        if (packet.action == Action.START) SpikePlantingHandler.startPlanting(sp);
        else SpikePlantingHandler.cancelPlanting(sp);
    }

    @Override
    public Type<PlantSpikePacket> type() { return TYPE; }
}


