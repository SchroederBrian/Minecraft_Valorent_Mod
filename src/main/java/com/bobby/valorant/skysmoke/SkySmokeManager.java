package com.bobby.valorant.skysmoke;

import java.nio.file.Path;
import java.util.Map;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SkySmokeManager {
    private SkySmokeManager() {}

    private static Map<ResourceLocation, SkySmokeArea.SkySmokeDimensionAreas> AREAS = java.util.Collections.emptyMap();

    public static void load(MinecraftServer server) {
        String pathStr = Config.COMMON.skySmokeAreasConfigPath.get();
        Path path = server.getFile(pathStr);
        AREAS = SkySmokeAreasIO.load(path);
        Valorant.LOGGER.info("[SkySmoke] Loaded {} dimensions from {}", AREAS.size(), path);
    }

    public static SkySmokeArea.SkySmokeDimensionAreas get(ServerLevel level) {
        return AREAS.get(level.dimension().location());
    }

    public static void putAndSave(MinecraftServer server, ResourceLocation dim, String areaId, SkySmokeArea.Type type,
                                  SkySmokeArea.Mode mode, int y, boolean enabled, java.util.List<net.minecraft.core.BlockPos> vertices) {
        SkySmokeArea area = new SkySmokeArea(areaId, type, mode, y, enabled, vertices);

        Map<String, SkySmokeArea> areaMap = switch (type) {
            case ALLOWED -> {
                Map<String, SkySmokeArea> allowed = new java.util.HashMap<>(AREAS.getOrDefault(dim, new SkySmokeArea.SkySmokeDimensionAreas(java.util.Collections.emptyMap(), java.util.Collections.emptyMap())).allowed());
                allowed.put(areaId, area);
                yield allowed;
            }
            case BLOCKED -> {
                Map<String, SkySmokeArea> blocked = new java.util.HashMap<>(AREAS.getOrDefault(dim, new SkySmokeArea.SkySmokeDimensionAreas(java.util.Collections.emptyMap(), java.util.Collections.emptyMap())).blocked());
                blocked.put(areaId, area);
                yield blocked;
            }
        };

        SkySmokeArea.SkySmokeDimensionAreas dimAreas = AREAS.get(dim);
        SkySmokeArea.SkySmokeDimensionAreas newDimAreas = switch (type) {
            case ALLOWED -> new SkySmokeArea.SkySmokeDimensionAreas(areaMap, dimAreas != null ? dimAreas.blocked() : java.util.Collections.emptyMap());
            case BLOCKED -> new SkySmokeArea.SkySmokeDimensionAreas(dimAreas != null ? dimAreas.allowed() : java.util.Collections.emptyMap(), areaMap);
        };

        java.util.Map<ResourceLocation, SkySmokeArea.SkySmokeDimensionAreas> newAreas = new java.util.HashMap<>(AREAS);
        newAreas.put(dim, newDimAreas);
        AREAS = java.util.Collections.unmodifiableMap(newAreas);

        String pathStr = Config.COMMON.skySmokeAreasConfigPath.get();
        Path path = server.getFile(pathStr);
        SkySmokeAreasIO.save(path, AREAS);

        // Sync to all players in this dimension
        syncToClients(server, dim);

        Valorant.LOGGER.info("[SkySmoke] Saved {} area {} in {} with {} vertices at y={} to {}", type.name().toLowerCase(), areaId, dim, vertices.size(), y, path);
    }

    public static void toggleArea(MinecraftServer server, ResourceLocation dim, String areaId, boolean enabled) {
        SkySmokeArea.SkySmokeDimensionAreas dimAreas = AREAS.get(dim);
        if (dimAreas == null) return;

        // Check allowed areas
        SkySmokeArea area = dimAreas.allowed().get(areaId);
        SkySmokeArea.Type type = SkySmokeArea.Type.ALLOWED;
        if (area == null) {
            // Check blocked areas
            area = dimAreas.blocked().get(areaId);
            type = SkySmokeArea.Type.BLOCKED;
        }
        if (area == null) return;

        // Create updated area
        SkySmokeArea updatedArea = new SkySmokeArea(area.id(), area.type(), area.mode(), area.y(), enabled, area.vertices());

        putAndSave(server, dim, areaId, type, updatedArea.mode(), updatedArea.y(), enabled, updatedArea.vertices());
    }

    public static void syncToClients(MinecraftServer server, ResourceLocation dim) {
        // TODO: Send zones to clients via packet
        // For now, clients will render locally if they have the data
    }

    public static void syncFor(ServerPlayer player) {
        // Send all areas to the player
        var packet = new com.bobby.valorant.network.SyncSkySmokeAreasS2CPacket(AREAS);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
    }
}
