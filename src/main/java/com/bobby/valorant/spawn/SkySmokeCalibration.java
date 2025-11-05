package com.bobby.valorant.spawn;

import java.nio.file.Path;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;

import net.minecraft.server.MinecraftServer;

public final class SkySmokeCalibration {
    private SkySmokeCalibration() {}

    private static SkySmokeBounds CURRENT_BOUNDS = new SkySmokeBounds(
        Config.COMMON.skySmokeMapMinX.get(),
        Config.COMMON.skySmokeMapMinZ.get(),
        Config.COMMON.skySmokeMapMaxX.get(),
        Config.COMMON.skySmokeMapMaxZ.get(),
        Config.COMMON.skySmokeMapRotationDegrees.get()
    );

    // Per-dimension calibration sessions and transforms
    private static final java.util.Map<net.minecraft.resources.ResourceLocation, CalibrationSession> CALIBRATION_SESSIONS = new java.util.HashMap<>();
    private static final java.util.Map<net.minecraft.resources.ResourceLocation, SimilarityTransform> TRANSFORMS = new java.util.HashMap<>();

    // Calibration session data
    private static final class CalibrationSession {
        final net.minecraft.core.BlockPos[] worldPoints = new net.minecraft.core.BlockPos[3];
        final double[] mapU = new double[3];
        final double[] mapV = new double[3];
        int pointsCollected = 0;
        final net.minecraft.resources.ResourceLocation dimension;

        CalibrationSession(net.minecraft.resources.ResourceLocation dimension) {
            this.dimension = dimension;
        }
    }

    // Similarity transform: world = s·R·mapUV + t
    public static final class SimilarityTransform {
        final double a, b, tx, tz; // a = s*cosθ, b = s*sinθ

        SimilarityTransform(double a, double b, double tx, double tz) {
            this.a = a;
            this.b = b;
            this.tx = tx;
            this.tz = tz;
        }

        // Apply transform: [wx, wz] = [a, -b; b, a] · [u, v] + [tx, tz]
        net.minecraft.core.BlockPos apply(double u, double v) {
            double wx = a * u - b * v + tx;
            double wz = b * u + a * v + tz;
            return net.minecraft.core.BlockPos.containing(wx, 64, wz);
        }

        double getScale() { return Math.sqrt(a*a + b*b); }
        double getRotationDegrees() { return Math.toDegrees(Math.atan2(b, a)); }
    }

    public static void load(MinecraftServer server) {
        String pathStr = "config/valorant/sky_smoke_calibration.json";
        Path path = server.getFile(pathStr);
        SkySmokeBounds loaded = SkySmokeCalibrationIO.load(path);
        if (loaded != null) {
            CURRENT_BOUNDS = loaded;
            Valorant.LOGGER.info("[SkySmoke] Loaded calibration: {} to {}", loaded.toMinString(), loaded.toMaxString());
        } else {
            Valorant.LOGGER.info("[SkySmoke] No calibration file found, using defaults");
        }
    }

    public static SkySmokeBounds getBounds() {
        return CURRENT_BOUNDS;
    }

    public static void putAndSave(MinecraftServer server, int minX, int minZ, int maxX, int maxZ) {
        double rotation = Config.COMMON.skySmokeMapRotationDegrees.get();
        SkySmokeBounds newBounds = new SkySmokeBounds(minX, minZ, maxX, maxZ, rotation);
        CURRENT_BOUNDS = newBounds;

        String pathStr = "config/valorant/sky_smoke_calibration.json";
        Path path = server.getFile(pathStr);
        SkySmokeCalibrationIO.save(path, newBounds);

        // Sync to all connected clients
        syncToClients(server);

        Valorant.LOGGER.info("[SkySmoke] Saved calibration: {} to {} (rot: {}°)", newBounds.toMinString(), newBounds.toMaxString(), rotation);
    }

    // ===== CALIBRATION METHODS =====

    public static void startCalibration(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension) {
        CALIBRATION_SESSIONS.put(dimension, new CalibrationSession(dimension));
        Valorant.LOGGER.info("[SkySmoke] Started calibration session for dimension {}", dimension);
    }

    public static int addCalibrationPoint(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension, net.minecraft.server.level.ServerPlayer player) {
        CalibrationSession session = CALIBRATION_SESSIONS.get(dimension);
        if (session == null) return -1; // Not started
        if (!dimension.equals(session.dimension)) return -2; // Wrong dimension
        if (session.pointsCollected >= 3) return -3; // Already complete

        // Capture world position
        net.minecraft.core.BlockPos worldPos = player.blockPosition();
        session.worldPoints[session.pointsCollected] = worldPos;

        // Open map GUI for UV selection
        String prompt = "Click on the map where point " + (session.pointsCollected + 1) + " should be located";
        var packet = new com.bobby.valorant.network.OpenCalibrationPickPointS2CPacket(session.pointsCollected + 1, prompt);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);

        Valorant.LOGGER.debug("[SkySmoke] Captured world point {}: {} in dimension {}", session.pointsCollected + 1, worldPos, dimension);
        return session.pointsCollected + 1;
    }

    public static void receiveCalibrationPoint(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension, int step, double u, double v) {
        CalibrationSession session = CALIBRATION_SESSIONS.get(dimension);
        if (session == null || step < 1 || step > 3) return;

        int index = step - 1;
        session.mapU[index] = u;
        session.mapV[index] = v;
        session.pointsCollected = Math.max(session.pointsCollected, step);

        Valorant.LOGGER.debug("[SkySmoke] Received map point {}: UV({},{}) in dimension {}", step, u, v, dimension);

        // If we have all 3 points, we could auto-complete, but let the user manually apply
    }

    public static boolean previewCalibration(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension, net.minecraft.server.level.ServerLevel level) {
        CalibrationSession session = CALIBRATION_SESSIONS.get(dimension);
        if (session == null || session.pointsCollected < 3) return false;

        try {
            SimilarityTransform transform = computeSimilarityTransform(session);
            if (transform == null) return false;

            // Spawn particles at map corners to show the calibration
            spawnPreviewParticles(level, transform);
            Valorant.LOGGER.info("[SkySmoke] Calibration preview: scale={:.2f}, rotation={:.1f}°, tx={:.1f}, tz={:.1f}",
                transform.getScale(), transform.getRotationDegrees(), transform.tx, transform.tz);
            return true;
        } catch (Exception e) {
            Valorant.LOGGER.error("[SkySmoke] Failed to compute calibration transform", e);
            return false;
        }
    }

    public static boolean applyCalibration(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension) {
        CalibrationSession session = CALIBRATION_SESSIONS.get(dimension);
        if (session == null || session.pointsCollected < 3) return false;

        try {
            SimilarityTransform transform = computeSimilarityTransform(session);
            if (transform == null) return false;

            // Store the transform
            TRANSFORMS.put(dimension, transform);

            // Save to file
            saveTransforms(server);

            // Sync to clients
            syncTransformsToClients(server, dimension);

            // Clean up session
            CALIBRATION_SESSIONS.remove(dimension);

            Valorant.LOGGER.info("[SkySmoke] Applied calibration for dimension {}: scale={:.2f}, rotation={:.1f}°, tx={:.1f}, tz={:.1f}",
                dimension, transform.getScale(), transform.getRotationDegrees(), transform.tx, transform.tz);
            return true;
        } catch (Exception e) {
            Valorant.LOGGER.error("[SkySmoke] Failed to apply calibration", e);
            return false;
        }
    }

    public static void cancelCalibration(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension) {
        CALIBRATION_SESSIONS.remove(dimension);
        Valorant.LOGGER.info("[SkySmoke] Cancelled calibration for dimension {}", dimension);
    }

    public static String getCalibrationStatus(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension) {
        CalibrationSession session = CALIBRATION_SESSIONS.get(dimension);
        if (session == null) {
            SimilarityTransform transform = TRANSFORMS.get(dimension);
            if (transform != null) {
                return "Calibrated: scale=" + String.format("%.2f", transform.getScale()) +
                       ", rotation=" + String.format("%.1f", transform.getRotationDegrees()) + "°";
            }
            return "Not calibrated";
        }

        return "Calibration in progress: " + session.pointsCollected + "/3 points collected";
    }

    private static SimilarityTransform computeSimilarityTransform(CalibrationSession session) {
        // Extract points
        double[] wx = new double[3], wz = new double[3], mu = new double[3], mv = new double[3];
        for (int i = 0; i < 3; i++) {
            net.minecraft.core.BlockPos wp = session.worldPoints[i];
            wx[i] = wp.getX();
            wz[i] = wp.getZ();
            mu[i] = session.mapU[i];
            mv[i] = session.mapV[i];
        }

        // Compute centroids
        double cx = (wx[0] + wx[1] + wx[2]) / 3;
        double cz = (wz[0] + wz[1] + wz[2]) / 3;
        double cu = (mu[0] + mu[1] + mu[2]) / 3;
        double cv = (mv[0] + mv[1] + mv[2]) / 3;

        // Center the points
        double[] wx_c = {wx[0] - cx, wx[1] - cx, wx[2] - cx};
        double[] wz_c = {wz[0] - cz, wz[1] - cz, wz[2] - cz};
        double[] mu_c = {mu[0] - cu, mu[1] - cu, mu[2] - cu};
        double[] mv_c = {mv[0] - cv, mv[1] - cv, mv[2] - cv};

        // Compute rotation using cross-correlation method
        double num1 = 0, num2 = 0, den1 = 0, den2 = 0;
        for (int i = 0; i < 3; i++) {
            num1 += mu_c[i] * wz_c[i] - mv_c[i] * wx_c[i];
            num2 += mu_c[i] * wx_c[i] + mv_c[i] * wz_c[i];
            den1 += mu_c[i] * mu_c[i] + mv_c[i] * mv_c[i];
            den2 += wx_c[i] * wx_c[i] + wz_c[i] * wz_c[i];
        }

        double theta = Math.atan2(num1, num2);
        double scale = Math.sqrt((den1 > 0 ? den2 / den1 : 1.0));

        // Compute translation
        double a = scale * Math.cos(theta);
        double b = scale * Math.sin(theta);
        double tx = cx - (a * cu - b * cv);
        double tz = cz - (b * cu + a * cv);

        return new SimilarityTransform(a, b, tx, tz);
    }

    private static void spawnPreviewParticles(net.minecraft.server.level.ServerLevel level, SimilarityTransform transform) {
        // Spawn particles at map corners: (0,0), (1,0), (1,1), (0,1)
        net.minecraft.core.BlockPos[] corners = {
            transform.apply(0, 0),
            transform.apply(1, 0),
            transform.apply(1, 1),
            transform.apply(0, 1)
        };

        for (net.minecraft.core.BlockPos corner : corners) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.GLOW,
                corner.getX() + 0.5, corner.getY() + 1.0, corner.getZ() + 0.5,
                10, 0.2, 0.2, 0.2, 0.01);
        }
    }

    private static void saveTransforms(MinecraftServer server) {
        Path transformsPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
            .resolve("config")
            .resolve("valorant")
            .resolve("sky_smoke_transforms.json");
        SkySmokeCalibrationIO.saveTransforms(transformsPath, TRANSFORMS);
    }

    public static void loadTransforms(MinecraftServer server) {
        Path transformsPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
            .resolve("config")
            .resolve("valorant")
            .resolve("sky_smoke_transforms.json");
        TRANSFORMS.clear();
        TRANSFORMS.putAll(SkySmokeCalibrationIO.loadTransforms(transformsPath));
    }

    private static void syncTransformsToClients(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension) {
        SimilarityTransform transform = TRANSFORMS.get(dimension);
        if (transform != null) {
            var packet = new com.bobby.valorant.network.SyncSkySmokeTransformS2CPacket(
                transform.a, transform.b, transform.tx, transform.tz);
            // Send to all players (could filter by dimension if needed)
            for (var player : server.getPlayerList().getPlayers()) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    public static void syncTransformsFor(net.minecraft.server.level.ServerPlayer player) {
        net.minecraft.resources.ResourceLocation dimension = player.level().dimension().location();
        SimilarityTransform transform = TRANSFORMS.get(dimension);
        if (transform != null) {
            var packet = new com.bobby.valorant.network.SyncSkySmokeTransformS2CPacket(
                transform.a, transform.b, transform.tx, transform.tz);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
        }
    }

    public static void syncToClients(MinecraftServer server) {
        // Send bounds to all connected clients
        var packet = new com.bobby.valorant.network.SyncSkySmokeCalibrationS2CPacket(
            CURRENT_BOUNDS.minX(),
            CURRENT_BOUNDS.minZ(),
            CURRENT_BOUNDS.maxX(),
            CURRENT_BOUNDS.maxZ(),
            CURRENT_BOUNDS.rotationDegrees()
        );

        for (var player : server.getPlayerList().getPlayers()) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
        }
    }

    public static record SkySmokeBounds(int minX, int minZ, int maxX, int maxZ, double rotationDegrees) {
        public String toMinString() {
            return minX + "," + minZ;
        }

        public String toMaxString() {
            return maxX + "," + maxZ;
        }

        public boolean contains(int x, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }
    }
}
