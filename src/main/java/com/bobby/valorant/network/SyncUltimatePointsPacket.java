package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.UltimateData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncUltimatePointsPacket(int points) implements CustomPacketPayload {
    public static final Type<SyncUltimatePointsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_ultimate_points"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncUltimatePointsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVarInt(packet.points()),
            buf -> new SyncUltimatePointsPacket(buf.readVarInt()));

    public static void handle(SyncUltimatePointsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                UltimateData.setPoints(player, packet.points);
            }
        });
    }

    @Override
    public Type<SyncUltimatePointsPacket> type() {
        return TYPE;
    }
}
