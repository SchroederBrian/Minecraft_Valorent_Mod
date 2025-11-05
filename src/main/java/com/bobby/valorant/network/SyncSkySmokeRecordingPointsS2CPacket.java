package com.bobby.valorant.network;

import java.util.List;
import java.util.UUID;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.skysmoke.SkySmokeArea;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSkySmokeRecordingPointsS2CPacket(
    UUID playerId,
    SkySmokeArea.Type areaType,
    List<BlockPos> points
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSkySmokeRecordingPointsS2CPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_sky_smoke_recording_points"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkySmokeRecordingPointsS2CPacket> STREAM_CODEC =
        StreamCodec.of(SyncSkySmokeRecordingPointsS2CPacket::encode, SyncSkySmokeRecordingPointsS2CPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SyncSkySmokeRecordingPointsS2CPacket packet) {
        buf.writeUUID(packet.playerId());
        buf.writeEnum(packet.areaType());
        buf.writeVarInt(packet.points().size());
        for (BlockPos point : packet.points()) {
            buf.writeVarInt(point.getX());
            buf.writeVarInt(point.getY());
            buf.writeVarInt(point.getZ());
        }
    }

    private static SyncSkySmokeRecordingPointsS2CPacket decode(RegistryFriendlyByteBuf buf) {
        UUID playerId = buf.readUUID();
        SkySmokeArea.Type areaType = buf.readEnum(SkySmokeArea.Type.class);
        int pointCount = buf.readVarInt();
        List<BlockPos> points = new java.util.ArrayList<>();
        for (int i = 0; i < pointCount; i++) {
            int x = buf.readVarInt();
            int y = buf.readVarInt();
            int z = buf.readVarInt();
            points.add(new BlockPos(x, y, z));
        }
        return new SyncSkySmokeRecordingPointsS2CPacket(playerId, areaType, points);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSkySmokeRecordingPointsS2CPacket packet, IPayloadContext context) {
        // Client-side: store the received recording points for visualization
        com.bobby.valorant.client.SkySmokeRecordingClient.setRecordingPoints(
            packet.playerId(),
            packet.areaType(),
            packet.points()
        );
    }
}
