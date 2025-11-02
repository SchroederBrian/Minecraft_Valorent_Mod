package com.bobby.valorant.spawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class SpawnAreaRecording {
    private SpawnAreaRecording() {}

    private static final Map<UUID, Session> SESSIONS = new java.util.HashMap<>();

    public static boolean start(ServerPlayer sp, String teamId, Integer yOverride) {
        ResourceLocation dim = sp.level().dimension().location();
        int y = yOverride != null ? yOverride : sp.blockPosition().getY();
        Session s = new Session(SpawnAreasConfigIO.normalizeTeamId(teamId), dim, y);
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
        com.bobby.valorant.spawn.SpawnAreaManager.putAndSave(sp.getServer(), s.dimension, s.teamId, s.y, java.util.List.copyOf(s.vertices));
        SESSIONS.remove(sp.getUUID());
        return true;
    }

    public static String status(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null) return "No active recording";
        return "Recording team " + s.teamId + " at y=" + s.y + ", points=" + s.vertices.size() + " in " + s.dimension;
    }

    private static final class Session {
        final String teamId;
        final ResourceLocation dimension;
        int y;
        final List<BlockPos> vertices = new ArrayList<>();

        Session(String teamId, ResourceLocation dimension, int y) {
            this.teamId = teamId;
            this.dimension = dimension;
            this.y = y;
        }
    }
}


