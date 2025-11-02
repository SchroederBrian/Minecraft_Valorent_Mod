package com.bobby.valorant.spawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class BombSiteRecording {
    private BombSiteRecording() {}

    private static final Map<UUID, Session> SESSIONS = new java.util.HashMap<>();

    public static boolean start(ServerPlayer sp, String siteId, Integer yOverride) {
        ResourceLocation dim = sp.level().dimension().location();
        int y = yOverride != null ? yOverride : sp.blockPosition().getY();
        String normalized = normalize(siteId);
        Session s = new Session(normalized, dim, y);
        SESSIONS.put(sp.getUUID(), s);
        return true;
    }

    public static boolean addPoint(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null) return false;
        BlockPos bp = sp.blockPosition();
        BlockPos p = new BlockPos(bp.getX(), s.y, bp.getZ());
        if (s.vertices.isEmpty() || !s.vertices.get(s.vertices.size() - 1).equals(p)) {
            s.vertices.add(p);
        }
        return true;
    }

    public static boolean undo(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null || s.vertices.isEmpty()) return false;
        s.vertices.remove(s.vertices.size() - 1);
        return true;
    }

    public static boolean clear(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null) return false;
        s.vertices.clear();
        return true;
    }

    public static boolean setY(ServerPlayer sp, int y) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null) return false;
        s.y = y;
        return true;
    }

    public static boolean cancel(ServerPlayer sp) {
        return SESSIONS.remove(sp.getUUID()) != null;
    }

    public static boolean save(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null || s.vertices.size() < 3) return false;
        com.bobby.valorant.spawn.SpawnAreaManager.putBombSiteAndSave(sp.getServer(), s.dimension, s.siteId, s.y, java.util.List.copyOf(s.vertices));
        SESSIONS.remove(sp.getUUID());
        return true;
    }

    public static String status(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null) return "No active bomb site recording";
        return "Recording bomb site " + s.siteId + " at y=" + s.y + ", points=" + s.vertices.size() + " in " + s.dimension;
    }

    private static String normalize(String siteId) {
        if ("B".equalsIgnoreCase(siteId)) return "B";
        if ("C".equalsIgnoreCase(siteId)) return "C";
        return "A";
    }

    private static final class Session {
        final String siteId;
        final ResourceLocation dimension;
        int y;
        final List<BlockPos> vertices = new ArrayList<>();

        Session(String siteId, ResourceLocation dimension, int y) {
            this.siteId = siteId;
            this.dimension = dimension;
            this.y = y;
        }
    }
}


