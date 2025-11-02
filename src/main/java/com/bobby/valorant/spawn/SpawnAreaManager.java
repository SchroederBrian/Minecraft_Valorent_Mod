package com.bobby.valorant.spawn;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.SyncSpawnAreasS2CPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SpawnAreaManager {
    private SpawnAreaManager() {}

    private static Map<ResourceLocation, Map<String, SpawnArea>> AREAS = java.util.Collections.emptyMap();
    private static Map<ResourceLocation, Map<String, SpawnArea>> BOMB_SITES = java.util.Collections.emptyMap();

    public static void load(MinecraftServer server) {
        String pathStr = Config.COMMON.spawnAreaConfigPath.get();
        Path path = server.getFile(pathStr);
        AREAS = SpawnAreasConfigIO.load(path);
        BOMB_SITES = SpawnAreasConfigIO.loadBombSites(path);
        Valorant.LOGGER.info("[SpawnAreas] Loaded {} dimensions from {}", AREAS.size(), path);
    }

    public static SpawnArea get(ServerLevel level, String teamId) {
        Map<String, SpawnArea> byTeam = AREAS.get(level.dimension().location());
        if (byTeam == null) return null;
        return byTeam.get(SpawnAreasConfigIO.normalizeTeamId(teamId));
    }

    public static void syncFor(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        Map<String, SpawnArea> byTeam = AREAS.get(level.dimension().location());
        SpawnArea a = byTeam != null ? byTeam.get("A") : null;
        SpawnArea v = byTeam != null ? byTeam.get("V") : null;
        Map<String, SpawnArea> sites = BOMB_SITES.get(level.dimension().location());
        SpawnArea sa = sites != null ? sites.get("A") : null;
        SpawnArea sb = sites != null ? sites.get("B") : null;
        SpawnArea sc = sites != null ? sites.get("C") : null;
        PacketDistributor.sendToPlayer(player, SyncSpawnAreasS2CPacket.of(level.dimension().location(), a, v, sa, sb, sc));
    }

    public static void putAndSave(MinecraftServer server, ResourceLocation dim, String teamId, int y, java.util.List<BlockPos> vertices) {
        teamId = SpawnAreasConfigIO.normalizeTeamId(teamId);
        Map<String, SpawnArea> byTeam = new java.util.HashMap<>(AREAS.getOrDefault(dim, java.util.Collections.emptyMap()));
        byTeam.put(teamId, new SpawnArea(teamId, dim, y, vertices));
        java.util.Map<ResourceLocation, Map<String, SpawnArea>> newAreas = new java.util.HashMap<>(AREAS);
        newAreas.put(dim, byTeam);
        AREAS = java.util.Collections.unmodifiableMap(newAreas);

        String pathStr = Config.COMMON.spawnAreaConfigPath.get();
        Path path = server.getFile(pathStr);
        SpawnAreasConfigIO.saveAll(path, AREAS, BOMB_SITES);

        // Sync to all players in this dimension
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            if (sp.level().dimension().location().equals(dim)) {
                syncFor(sp);
            }
        }
        Valorant.LOGGER.info("[SpawnAreas] Saved team {} in {} with {} vertices at y={} to {}", teamId, dim, vertices.size(), y, path);
    }

    public static void putBombSiteAndSave(MinecraftServer server, ResourceLocation dim, String siteId, int y, java.util.List<BlockPos> vertices) {
        siteId = siteId.equalsIgnoreCase("B") ? "B" : (siteId.equalsIgnoreCase("C") ? "C" : "A");
        Map<String, SpawnArea> bySite = new java.util.HashMap<>(BOMB_SITES.getOrDefault(dim, java.util.Collections.emptyMap()));
        bySite.put(siteId, new SpawnArea(siteId, dim, y, vertices));
        java.util.Map<ResourceLocation, Map<String, SpawnArea>> newSites = new java.util.HashMap<>(BOMB_SITES);
        newSites.put(dim, bySite);
        BOMB_SITES = java.util.Collections.unmodifiableMap(newSites);

        String pathStr = Config.COMMON.spawnAreaConfigPath.get();
        Path path = server.getFile(pathStr);
        SpawnAreasConfigIO.saveAll(path, AREAS, BOMB_SITES);

        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            if (sp.level().dimension().location().equals(dim)) {
                syncFor(sp);
            }
        }
        Valorant.LOGGER.info("[SpawnAreas] Saved bomb site {} in {} with {} vertices at y={} to {}", siteId, dim, vertices.size(), y, path);
    }

    public static SpawnArea getBombSite(ServerLevel level, String siteId) {
        Map<String, SpawnArea> bySite = BOMB_SITES.get(level.dimension().location());
        if (bySite == null) return null;
        String key = siteId.equalsIgnoreCase("B") ? "B" : (siteId.equalsIgnoreCase("C") ? "C" : "A");
        return bySite.get(key);
    }

    public static BlockPos chooseRandomPointInside(SpawnArea area, ServerLevel level, net.minecraft.util.RandomSource rng) {
        if (area == null || area.vertices.size() < 3) return null;
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : area.vertices) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getZ() < minZ) minZ = p.getZ();
            if (p.getZ() > maxZ) maxZ = p.getZ();
        }
        for (int tries = 0; tries < 200; tries++) {
            double x = minX + rng.nextDouble() * (maxX - minX);
            double z = minZ + rng.nextDouble() * (maxZ - minZ);
            if (isInside(area.vertices, x, z)) {
                int xi = (int) Math.floor(x);
                int zi = (int) Math.floor(z);
                return new BlockPos(xi, area.y + 1, zi);
            }
        }
        // Fallback to centroid
        double cx = 0, cz = 0;
        for (BlockPos p : area.vertices) { cx += p.getX(); cz += p.getZ(); }
        cx /= area.vertices.size();
        cz /= area.vertices.size();
        return new BlockPos((int)Math.floor(cx), area.y + 1, (int)Math.floor(cz));
    }

    private static boolean isInside(List<BlockPos> vertices, double x, double z) {
        boolean inside = false;
        int n = vertices.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = vertices.get(i).getX();
            double zi = vertices.get(i).getZ();
            double xj = vertices.get(j).getX();
            double zj = vertices.get(j).getZ();
            boolean intersect = ((zi > z) != (zj > z)) &&
                    (x < (xj - xi) * (z - zi) / (zj - zi + 1e-9) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }
}


