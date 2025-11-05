package com.bobby.valorant.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.skysmoke.SkySmokeArea;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSkySmokeAreasS2CPacket(
    Map<ResourceLocation, SkySmokeArea.SkySmokeDimensionAreas> areas
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSkySmokeAreasS2CPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_sky_smoke_areas"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkySmokeAreasS2CPacket> STREAM_CODEC =
        StreamCodec.of(SyncSkySmokeAreasS2CPacket::encode, SyncSkySmokeAreasS2CPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SyncSkySmokeAreasS2CPacket packet) {
        // Write number of dimensions
        buf.writeVarInt(packet.areas.size());

        for (var dimEntry : packet.areas.entrySet()) {
            ResourceLocation dim = dimEntry.getKey();
            var dimAreas = dimEntry.getValue();

            // Write dimension
            buf.writeResourceLocation(dim);

            // Write allowed zones
            encodeZoneMap(buf, dimAreas.allowed());

            // Write blocked zones
            encodeZoneMap(buf, dimAreas.blocked());
        }
    }

    private static SyncSkySmokeAreasS2CPacket decode(RegistryFriendlyByteBuf buf) {
        Map<ResourceLocation, SkySmokeArea.SkySmokeDimensionAreas> areas = new HashMap<>();

        int dimCount = buf.readVarInt();
        for (int i = 0; i < dimCount; i++) {
            ResourceLocation dim = buf.readResourceLocation();

            // Read allowed zones
            Map<String, SkySmokeArea> allowed = decodeZoneMap(buf);

            // Read blocked zones
            Map<String, SkySmokeArea> blocked = decodeZoneMap(buf);

            areas.put(dim, new SkySmokeArea.SkySmokeDimensionAreas(allowed, blocked));
        }

        return new SyncSkySmokeAreasS2CPacket(areas);
    }

    private static void encodeZoneMap(RegistryFriendlyByteBuf buf, Map<String, SkySmokeArea> zones) {
        buf.writeVarInt(zones.size());
        for (var entry : zones.entrySet()) {
            SkySmokeArea zone = entry.getValue();

            // Write zone data
            buf.writeUtf(entry.getKey()); // id
            buf.writeEnum(zone.type()); // type
            buf.writeEnum(zone.mode()); // mode
            buf.writeVarInt(zone.y()); // y
            buf.writeBoolean(zone.enabled()); // enabled

            // Write vertices (include per-vertex Y to preserve exact heights)
            List<BlockPos> vertices = zone.vertices();
            buf.writeVarInt(vertices.size());
            for (BlockPos vertex : vertices) {
                buf.writeVarInt(vertex.getX());
                buf.writeVarInt(vertex.getY());
                buf.writeVarInt(vertex.getZ());
            }
        }
    }

    private static Map<String, SkySmokeArea> decodeZoneMap(RegistryFriendlyByteBuf buf) {
        Map<String, SkySmokeArea> zones = new HashMap<>();
        int zoneCount = buf.readVarInt();

        for (int i = 0; i < zoneCount; i++) {
            String id = buf.readUtf();
            SkySmokeArea.Type type = buf.readEnum(SkySmokeArea.Type.class);
            SkySmokeArea.Mode mode = buf.readEnum(SkySmokeArea.Mode.class);
            int y = buf.readVarInt();
            boolean enabled = buf.readBoolean();

            // Read vertices (per-vertex Y)
            int vertexCount = buf.readVarInt();
            List<BlockPos> vertices = new java.util.ArrayList<>();
            for (int j = 0; j < vertexCount; j++) {
                int x = buf.readVarInt();
                int vy = buf.readVarInt();
                int z = buf.readVarInt();
                vertices.add(new BlockPos(x, vy, z));
            }

            zones.put(id, new SkySmokeArea(id, type, mode, y, enabled, vertices));
        }

        return zones;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSkySmokeAreasS2CPacket packet, IPayloadContext context) {
        // Client-side: store the received areas for rendering
        com.bobby.valorant.client.SkySmokeAreaClient.setAreas(packet.areas);
    }
}
