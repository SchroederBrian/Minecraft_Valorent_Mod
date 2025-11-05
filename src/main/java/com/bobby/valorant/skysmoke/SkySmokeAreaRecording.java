package com.bobby.valorant.skysmoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class SkySmokeAreaRecording {
    private SkySmokeAreaRecording() {}

    private static final Map<UUID, Session> SESSIONS = new java.util.HashMap<>();

    public static boolean start(ServerPlayer sp, String areaId, SkySmokeArea.Type type, SkySmokeArea.Mode mode, Integer yOverride) {
        ResourceLocation dim = sp.level().dimension().location();
        int y = yOverride != null ? yOverride : sp.blockPosition().getY();
        Session s = new Session(areaId, type, mode, dim, y);
        SESSIONS.put(sp.getUUID(), s);

        // Send empty recording points to client to clear any previous data
        syncRecordingPointsToClient(sp, type, java.util.Collections.emptyList());
        return true;
    }

    public static boolean addPoint(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null) return false;
        BlockPos bp = sp.blockPosition();
        // Store exact X, Y, Z coordinates for precise particle visualization
        BlockPos p = new BlockPos(bp.getX(), bp.getY(), bp.getZ());
        if (s.vertices.isEmpty() || !s.vertices.get(s.vertices.size() - 1).equals(p)) {
            s.vertices.add(p);
            // Sync updated points to client
            syncRecordingPointsToClient(sp, s.type, s.vertices);
        }
        return true;
    }

    public static boolean undo(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null || s.vertices.isEmpty()) return false;
        s.vertices.remove(s.vertices.size() - 1);
        // Sync updated points to client
        syncRecordingPointsToClient(sp, s.type, s.vertices);
        return true;
    }

    public static boolean clear(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null) return false;
        s.vertices.clear();
        // Sync cleared points to client
        syncRecordingPointsToClient(sp, s.type, s.vertices);
        return true;
    }

    public static boolean setY(ServerPlayer sp, int y) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null) return false;
        s.y = y;
        return true;
    }

    public static boolean cancel(ServerPlayer sp) {
        boolean wasActive = SESSIONS.remove(sp.getUUID()) != null;
        if (wasActive) {
            // Clear recording points from client
            clearRecordingPointsFromClient(sp);
        }
        return wasActive;
    }

    public static boolean save(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null || s.vertices.size() < 3) return false;

        SkySmokeManager.putAndSave(sp.getServer(), s.dimension, s.areaId, s.type, s.mode, s.y, true, java.util.List.copyOf(s.vertices));
        SESSIONS.remove(sp.getUUID());
        // Clear recording points from client
        clearRecordingPointsFromClient(sp);
        return true;
    }

    public static String status(ServerPlayer sp) {
        Session s = SESSIONS.get(sp.getUUID());
        if (s == null) return "No active recording";
        return "Recording " + s.type.name().toLowerCase() + " area '" + s.areaId + "' (" + s.mode.name().toLowerCase() + ") at y=" + s.y + ", points=" + s.vertices.size() + " in " + s.dimension;
    }

    private static void syncRecordingPointsToClient(ServerPlayer sp, SkySmokeArea.Type areaType, List<BlockPos> points) {
        var packet = new com.bobby.valorant.network.SyncSkySmokeRecordingPointsS2CPacket(sp.getUUID(), areaType, points);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, packet);
    }

    private static void clearRecordingPointsFromClient(ServerPlayer sp) {
        // Send empty list to clear client-side visualization
        syncRecordingPointsToClient(sp, SkySmokeArea.Type.ALLOWED, java.util.Collections.emptyList());
    }

    private static final class Session {
        final String areaId;
        final SkySmokeArea.Type type;
        final SkySmokeArea.Mode mode;
        final ResourceLocation dimension;
        int y;
        final List<BlockPos> vertices = new ArrayList<>();

        Session(String areaId, SkySmokeArea.Type type, SkySmokeArea.Mode mode, ResourceLocation dimension, int y) {
            this.areaId = areaId;
            this.type = type;
            this.mode = mode;
            this.dimension = dimension;
            this.y = y;
        }
    }

    public static boolean isRecording(ServerPlayer sp) {
        return SESSIONS.containsKey(sp.getUUID());
    }
}
