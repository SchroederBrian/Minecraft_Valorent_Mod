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
    private static final java.util.Map<net.minecraft.resources.ResourceLocation, Transform2D> TRANSFORMS = new java.util.HashMap<>();

    // Calibration session data
    private static final class CalibrationSession {
        final java.util.List<Double> worldX = new java.util.ArrayList<>();
        final java.util.List<Double> worldZ = new java.util.ArrayList<>();
        final java.util.List<Double> mapU = new java.util.ArrayList<>();
        final java.util.List<Double> mapV = new java.util.ArrayList<>();
        final net.minecraft.resources.ResourceLocation dimension;

        CalibrationSession(net.minecraft.resources.ResourceLocation dimension) {
            this.dimension = dimension;
        }

        int size() { return Math.min(Math.min(worldX.size(), worldZ.size()), Math.min(mapU.size(), mapV.size())); }
    }

    public enum Model { SIMILARITY, AFFINE, HOMOGRAPHY }

    // Unified 2D transform represented by a homography H (3x3). H maps [u,v,1]^T -> [wx,wz,w]^T (then divide by w)
    public static final class Transform2D {
        final Model model;
        final double[] H;   // length 9, row-major
        final double[] Hinv; // length 9, row-major (for world->uv)

        Transform2D(Model model, double[] H, double[] Hinv) {
            this.model = model;
            this.H = H;
            this.Hinv = Hinv;
        }

        net.minecraft.core.BlockPos apply(double u, double v) {
            double x = H[0]*u + H[1]*v + H[2];
            double z = H[3]*u + H[4]*v + H[5];
            double w = H[6]*u + H[7]*v + H[8];
            if (w == 0.0) w = 1e-9;
            double wx = x / w;
            double wz = z / w;
            return net.minecraft.core.BlockPos.containing(wx, 64, wz);
        }

        double[] mapUvToWorld(double u, double v) {
            double x = H[0]*u + H[1]*v + H[2];
            double z = H[3]*u + H[4]*v + H[5];
            double w = H[6]*u + H[7]*v + H[8];
            if (w == 0.0) w = 1e-9;
            return new double[]{ x / w, z / w };
        }

        double[] mapWorldToUv(double wx, double wz) {
            double u = Hinv[0]*wx + Hinv[1]*wz + Hinv[2];
            double v = Hinv[3]*wx + Hinv[4]*wz + Hinv[5];
            double w = Hinv[6]*wx + Hinv[7]*wz + Hinv[8];
            if (w == 0.0) w = 1e-9;
            return new double[]{ u / w, v / w };
        }
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

        // Capture precise world position (double precision)
        session.worldX.add(player.getX());
        session.worldZ.add(player.getZ());

        // Open map GUI for UV selection
        String prompt = "Click on the map where this point should be located";
        var packet = new com.bobby.valorant.network.OpenCalibrationPickPointS2CPacket(session.size() + 1, prompt);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);

        Valorant.LOGGER.debug("[SkySmoke] Captured world anchor {}: ({}, {}) in dimension {}", session.size(), String.format("%.3f", player.getX()), String.format("%.3f", player.getZ()), dimension);
        return session.size();
    }

    public static void receiveCalibrationPoint(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension, int step, double u, double v) {
        CalibrationSession session = CALIBRATION_SESSIONS.get(dimension);
        if (session == null || step < 1) return;

        // Append in order; step is informational
        session.mapU.add(u);
        session.mapV.add(v);

        Valorant.LOGGER.debug("[SkySmoke] Received map anchor {}: UV({},{}) in dimension {}", step, String.format("%.5f", u), String.format("%.5f", v), dimension);
    }

    public static boolean previewCalibration(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension, net.minecraft.server.level.ServerLevel level) {
        CalibrationSession session = CALIBRATION_SESSIONS.get(dimension);
        int minPoints = getRequiredPoints();
        if (session == null || session.size() < minPoints) return false;

        try {
            Transform2D transform = computeTransform(session);
            if (transform == null) return false;

            // Spawn particles at map corners and anchor residuals to show the calibration
            spawnPreviewParticles(level, transform, session);
            double rms = computeRmsError(transform, session.worldX, session.worldZ, session.mapU, session.mapV);
            Valorant.LOGGER.info("[SkySmoke] Calibration preview: model={} RMS error={}", transform.model, String.format("%.3f blocks", rms));
            return true;
        } catch (Exception e) {
            Valorant.LOGGER.error("[SkySmoke] Failed to compute calibration transform", e);
            return false;
        }
    }

    public static boolean applyCalibration(MinecraftServer server, net.minecraft.resources.ResourceLocation dimension) {
        CalibrationSession session = CALIBRATION_SESSIONS.get(dimension);
        int minPoints = getRequiredPoints();
        if (session == null || session.size() < minPoints) return false;

        try {
            Transform2D transform = computeTransform(session);
            if (transform == null) return false;

            // Store the transform
            TRANSFORMS.put(dimension, transform);

            // Save to file
            saveTransforms(server);

            // Sync to clients
            syncTransformsToClients(server, dimension);

            // Clean up session
            CALIBRATION_SESSIONS.remove(dimension);

            Valorant.LOGGER.info("[SkySmoke] Applied calibration for dimension {}: model={}",
                dimension, transform.model);
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
            Transform2D transform = TRANSFORMS.get(dimension);
            if (transform != null) {
                return "Calibrated: model=" + transform.model.name();
            }
            return "Not calibrated";
        }

        return "Calibration in progress: " + session.size() + " points collected";
    }

    private static Transform2D computeTransform(CalibrationSession session) {
        String modelStr = com.bobby.valorant.Config.COMMON.skySmokeCalibrationModel.get();
        Model model;
        try { model = Model.valueOf(modelStr.toUpperCase()); } catch (Exception e) { model = Model.SIMILARITY; }

        boolean useRansac = com.bobby.valorant.Config.COMMON.skySmokeCalibrationUseRansac.get();
        if (useRansac) {
            Transform2D ransac = ransacFit(session.worldX, session.worldZ, session.mapU, session.mapV, model,
                com.bobby.valorant.Config.COMMON.skySmokeCalibrationRansacIterations.get(),
                com.bobby.valorant.Config.COMMON.skySmokeCalibrationRansacThreshold.get());
            if (ransac != null) return ransac;
        }

        return switch (model) {
            case SIMILARITY -> fitSimilarity(session.worldX, session.worldZ, session.mapU, session.mapV);
            case AFFINE -> fitAffine(session.worldX, session.worldZ, session.mapU, session.mapV);
            case HOMOGRAPHY -> fitHomography(session.worldX, session.worldZ, session.mapU, session.mapV);
        };
    }

    private static Transform2D fitSimilarity(java.util.List<Double> wx, java.util.List<Double> wz, java.util.List<Double> mu, java.util.List<Double> mv) {
        int n = Math.min(Math.min(wx.size(), wz.size()), Math.min(mu.size(), mv.size()));
        if (n < 3) return null;
        double cx=0, cz=0, cu=0, cv=0;
        for (int i=0;i<n;i++){ cx+=wx.get(i); cz+=wz.get(i); cu+=mu.get(i); cv+=mv.get(i);} cx/=n; cz/=n; cu/=n; cv/=n;
        double Sux=0, Suz=0, Svv=0;
        for (int i=0;i<n;i++){
            double ui = mu.get(i)-cu, vi = mv.get(i)-cv;
            double xi = wx.get(i)-cx, zi = wz.get(i)-cz;
            Sux += ui*xi + vi*zi; // dot
            Suz += ui*zi - vi*xi; // cross
            Svv += ui*ui + vi*vi;
        }
        if (Svv == 0) return null;
        double a = Sux / Svv; // s*cos
        double b = Suz / Svv; // s*sin
        double tx = cx - (a*cu - b*cv);
        double tz = cz - (b*cu + a*cv);
        double[] H = new double[]{ a, -b, tx,  b, a, tz,  0, 0, 1 };
        double[] Hinv = invert3x3(H);
        return new Transform2D(Model.SIMILARITY, H, Hinv);
    }

    private static Transform2D fitAffine(java.util.List<Double> wx, java.util.List<Double> wz, java.util.List<Double> mu, java.util.List<Double> mv) {
        int n = Math.min(Math.min(wx.size(), wz.size()), Math.min(mu.size(), mv.size()));
        if (n < 3) return null;
        int rows = 2*n;
        double[][] AtA = new double[6][6];
        double[] AtY = new double[6];
        for (int i=0;i<n;i++){
            double u = mu.get(i), v = mv.get(i);
            // wx row
            double[] r1 = new double[]{u, v, 1, 0, 0, 0};
            double y1 = wx.get(i);
            accumulateNormal(AtA, AtY, r1, y1);
            // wz row
            double[] r2 = new double[]{0, 0, 0, u, v, 1};
            double y2 = wz.get(i);
            accumulateNormal(AtA, AtY, r2, y2);
        }
        double[] p = solveSymmetric6(AtA, AtY);
        if (p == null) return null;
        double a=p[0], c=p[1], tx=p[2], b=p[3], d=p[4], tz=p[5];
        double[] H = new double[]{ a, c, tx,  b, d, tz,  0, 0, 1 };
        double[] Hinv = invert3x3(H);
        return new Transform2D(Model.AFFINE, H, Hinv);
    }

    private static Transform2D fitHomography(java.util.List<Double> wx, java.util.List<Double> wz, java.util.List<Double> mu, java.util.List<Double> mv) {
        int n = Math.min(Math.min(wx.size(), wz.size()), Math.min(mu.size(), mv.size()));
        if (n < 4) return null;
        // Hartley normalization
        Normalization Tuv = normalize2D(mu, mv);
        Normalization Twx = normalize2D(wx, wz);

        int rows = 2*n;
        double[][] A = new double[rows][9];
        for (int i=0;i<n;i++){
            double u = Tuv.applyX(mu.get(i));
            double v = Tuv.applyY(mv.get(i));
            double x = Twx.applyX(wx.get(i));
            double z = Twx.applyY(wz.get(i));
            int r = 2*i;
            // x row
            A[r][0] = -u; A[r][1] = -v; A[r][2] = -1; A[r][3] = 0;  A[r][4] = 0;  A[r][5] = 0;  A[r][6] = u*x; A[r][7] = v*x; A[r][8] = x;
            // z row
            A[r+1][0] = 0;  A[r+1][1] = 0;  A[r+1][2] = 0;  A[r+1][3] = -u; A[r+1][4] = -v; A[r+1][5] = -1; A[r+1][6] = u*z; A[r+1][7] = v*z; A[r+1][8] = z;
        }
        // Compute AtA
        double[][] AtA = new double[9][9];
        for (int r=0;r<rows;r++){
            for (int c=0;c<9;c++){
                for (int k=0;k<9;k++){
                    AtA[c][k] += A[r][c]*A[r][k];
                }
            }
        }
        // Smallest eigenvector of AtA via Jacobi
        double[][] eigVecs = jacobiEigenSymmetric(AtA);
        // Find eigenvector with smallest eigenvalue: AtA * v = lambda * v; since not returning lambdas, approximate by minimizing ||A v||
        int best = 0; double bestNorm = Double.POSITIVE_INFINITY;
        for (int i=0;i<9;i++){
            double[] v = column(eigVecs, i);
            double normAv = normAv(A, v);
            if (normAv < bestNorm) { bestNorm = normAv; best = i; }
        }
        double[] h = column(eigVecs, best);
        // Denormalize: H = Twx^{-1} * Hn * Tuv
        double[] Hn = h;
        double[] HnMat = Hn;
        double[] TuvMat = Tuv.matrix;
        double[] TwxInv = invert3x3(Twx.matrix);
        double[] Hden = multiply3x3(TwxInv, multiply3x3(HnMat, TuvMat));
        // Normalize so H[8]=1 if possible
        if (Math.abs(Hden[8]) > 1e-9) {
            double s = 1.0 / Hden[8];
            for (int i=0;i<9;i++) Hden[i] *= s;
        }
        double[] Hinv = invert3x3(Hden);
        return new Transform2D(Model.HOMOGRAPHY, Hden, Hinv);
    }

    private static Transform2D ransacFit(java.util.List<Double> wx, java.util.List<Double> wz, java.util.List<Double> mu, java.util.List<Double> mv, Model model, int iters, double thr) {
        java.util.Random rng = new java.util.Random(12345L);
        int n = Math.min(Math.min(wx.size(), wz.size()), Math.min(mu.size(), mv.size()));
        int minReq = switch (model) { case HOMOGRAPHY -> 4; default -> 3; };
        if (n < minReq) return null;
        int bestInliers = -1; Transform2D bestT = null;
        for (int it=0; it<iters; it++){
            int[] idx = randomUniqueIndices(rng, n, minReq);
            java.util.List<Double> swx = new java.util.ArrayList<>(), swz = new java.util.ArrayList<>(), smu = new java.util.ArrayList<>(), smv = new java.util.ArrayList<>();
            for (int j : idx){ swx.add(wx.get(j)); swz.add(wz.get(j)); smu.add(mu.get(j)); smv.add(mv.get(j)); }
            Transform2D t = switch (model) {
                case SIMILARITY -> fitSimilarity(swx, swz, smu, smv);
                case AFFINE -> fitAffine(swx, swz, smu, smv);
                case HOMOGRAPHY -> fitHomography(swx, swz, smu, smv);
            };
            if (t == null) continue;
            int inliers = 0;
            for (int i=0;i<n;i++){
                double[] pred = t.mapUvToWorld(mu.get(i), mv.get(i));
                double dx = pred[0]-wx.get(i), dz = pred[1]-wz.get(i);
                double err = Math.sqrt(dx*dx+dz*dz);
                if (err <= thr) inliers++;
            }
            if (inliers > bestInliers){ bestInliers = inliers; bestT = t; }
        }
        if (bestT == null) return null;
        // Refit on all inliers of bestT
        java.util.List<Double> inWx = new java.util.ArrayList<>(), inWz = new java.util.ArrayList<>(), inMu = new java.util.ArrayList<>(), inMv = new java.util.ArrayList<>();
        for (int i=0;i<n;i++){
            double[] pred = bestT.mapUvToWorld(mu.get(i), mv.get(i));
            double dx = pred[0]-wx.get(i), dz = pred[1]-wz.get(i);
            double err = Math.sqrt(dx*dx+dz*dz);
            if (err <= thr){ inWx.add(wx.get(i)); inWz.add(wz.get(i)); inMu.add(mu.get(i)); inMv.add(mv.get(i)); }
        }
        Transform2D refit = switch (model) {
            case SIMILARITY -> fitSimilarity(inWx, inWz, inMu, inMv);
            case AFFINE -> fitAffine(inWx, inWz, inMu, inMv);
            case HOMOGRAPHY -> fitHomography(inWx, inWz, inMu, inMv);
        };
        return refit != null ? refit : bestT;
    }

    private static int[] randomUniqueIndices(java.util.Random rng, int n, int k) {
        java.util.Set<Integer> set = new java.util.HashSet<>();
        while (set.size() < k) set.add(rng.nextInt(n));
        int[] out = new int[k]; int i=0; for (int v : set) out[i++]=v; return out;
    }

    private static double computeRmsError(Transform2D t, java.util.List<Double> wx, java.util.List<Double> wz, java.util.List<Double> mu, java.util.List<Double> mv) {
        int n = Math.min(Math.min(wx.size(), wz.size()), Math.min(mu.size(), mv.size()));
        if (n == 0) return Double.NaN;
        double sum=0;
        for (int i=0;i<n;i++){
            double[] pred = t.mapUvToWorld(mu.get(i), mv.get(i));
            double dx = pred[0]-wx.get(i), dz = pred[1]-wz.get(i);
            sum += dx*dx + dz*dz;
        }
        return Math.sqrt(sum / n);
    }

    private static void spawnPreviewParticles(net.minecraft.server.level.ServerLevel level, Transform2D transform, CalibrationSession session) {
        // Corners
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
        // Draw anchors and residuals (best-effort)
        int n = session.size();
        for (int i=0;i<n;i++){
            double[] pred = transform.mapUvToWorld(session.mapU.get(i), session.mapV.get(i));
            double wx = session.worldX.get(i);
            double wz = session.worldZ.get(i);
            double dx = pred[0]-wx, dz = pred[1]-wz;
            double err = Math.sqrt(dx*dx+dz*dz);
            // Color by error: small -> green, large -> red
            int particles = 8;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                pred[0]+0.5, 65.0, pred[1]+0.5, particles, 0.05, 0.05, 0.05, 0.01);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                wx+0.5, 65.0, wz+0.5, particles, 0.05, 0.05, 0.05, 0.01);
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
        Transform2D transform = TRANSFORMS.get(dimension);
        if (transform != null) {
            var packet = new com.bobby.valorant.network.SyncSkySmokeTransformS2CPacket(
                transform.model.name(), transform.H);
            // Send to all players (could filter by dimension if needed)
            for (var player : server.getPlayerList().getPlayers()) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    public static void syncTransformsFor(net.minecraft.server.level.ServerPlayer player) {
        net.minecraft.resources.ResourceLocation dimension = player.level().dimension().location();
        Transform2D transform = TRANSFORMS.get(dimension);
        if (transform != null) {
            var packet = new com.bobby.valorant.network.SyncSkySmokeTransformS2CPacket(
                transform.model.name(), transform.H);
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

    private static int getRequiredPoints() {
        String modelStr = com.bobby.valorant.Config.COMMON.skySmokeCalibrationModel.get();
        Model model;
        try { model = Model.valueOf(modelStr.toUpperCase()); } catch (Exception e) { model = Model.SIMILARITY; }
        int minCfg = com.bobby.valorant.Config.COMMON.skySmokeCalibrationMinPoints.get();
        int base = switch (model) { case HOMOGRAPHY -> 4; default -> 3; };
        return Math.max(minCfg, base);
    }

    private static void accumulateNormal(double[][] AtA, double[] AtY, double[] row, double y) {
        for (int i=0;i<row.length;i++) {
            AtY[i] += row[i]*y;
            for (int j=0;j<row.length;j++) {
                AtA[i][j] += row[i]*row[j];
            }
        }
    }

    private static double[] solveSymmetric6(double[][] A, double[] b) {
        // Simple Gaussian elimination with partial pivoting for 6x6
        int n=6;
        double[][] M = new double[n][n+1];
        for (int i=0;i<n;i++) {
            System.arraycopy(A[i], 0, M[i], 0, n);
            M[i][n] = b[i];
        }
        for (int p=0;p<n;p++) {
            int max=p; for (int i=p+1;i<n;i++) if (Math.abs(M[i][p])>Math.abs(M[max][p])) max=i;
            double[] tmp = M[p]; M[p]=M[max]; M[max]=tmp;
            double piv = M[p][p]; if (Math.abs(piv) < 1e-12) return null;
            for (int j=p;j<=n;j++) M[p][j] /= piv;
            for (int i=0;i<n;i++) if (i!=p) {
                double f = M[i][p];
                for (int j=p;j<=n;j++) M[i][j] -= f*M[p][j];
            }
        }
        double[] x = new double[n];
        for (int i=0;i<n;i++) x[i]=M[i][n];
        return x;
    }

    private static double[] invert3x3(double[] m) {
        double a=m[0], b=m[1], c=m[2], d=m[3], e=m[4], f=m[5], g=m[6], h=m[7], i=m[8];
        double A = e*i - f*h;
        double B = -(d*i - f*g);
        double C = d*h - e*g;
        double D = -(b*i - c*h);
        double E = a*i - c*g;
        double F = -(a*h - b*g);
        double G = b*f - c*e;
        double H = -(a*f - c*d);
        double I = a*e - b*d;
        double det = a*A + b*B + c*C;
        if (Math.abs(det) < 1e-12) det = 1e-12;
        double invDet = 1.0/det;
        return new double[]{ A*invDet, D*invDet, G*invDet,  B*invDet, E*invDet, H*invDet,  C*invDet, F*invDet, I*invDet };
    }

    private static double[] multiply3x3(double[] A, double[] B) {
        double[] r = new double[9];
        for (int rI=0;rI<3;rI++){
            for (int cI=0;cI<3;cI++){
                r[3*rI+cI] = A[3*rI+0]*B[cI+0] + A[3*rI+1]*B[cI+3] + A[3*rI+2]*B[cI+6];
            }
        }
        return r;
    }

    private static class Normalization {
        final double[] matrix; // 3x3
        final double sx, sy, tx, ty;
        Normalization(double[] matrix, double sx, double sy, double tx, double ty){ this.matrix=matrix; this.sx=sx; this.sy=sy; this.tx=tx; this.ty=ty; }
        double applyX(double x){ return sx*x + tx; }
        double applyY(double y){ return sy*y + ty; }
    }

    private static Normalization normalize2D(java.util.List<Double> xs, java.util.List<Double> ys) {
        int n = Math.min(xs.size(), ys.size());
        double cx=0, cy=0; for (int i=0;i<n;i++){ cx+=xs.get(i); cy+=ys.get(i);} cx/=n; cy/=n;
        double meanDist=0; for (int i=0;i<n;i++){ double dx=xs.get(i)-cx, dy=ys.get(i)-cy; meanDist += Math.sqrt(dx*dx+dy*dy);} meanDist/=n;
        double s = (meanDist > 0) ? Math.sqrt(2) / meanDist : 1.0;
        double tx = -s*cx; double ty = -s*cy;
        double[] T = new double[]{ s,0,tx, 0,s,ty, 0,0,1 };
        return new Normalization(T, s, s, tx, ty);
    }

    private static double[][] jacobiEigenSymmetric(double[][] A) {
        int n = A.length;
        double[][] a = new double[n][n];
        for (int i=0;i<n;i++) System.arraycopy(A[i], 0, a[i], 0, n);
        double[][] V = new double[n][n];
        for (int i=0;i<n;i++) V[i][i]=1.0;
        for (int iter=0; iter<100; iter++){
            int p=0,q=1; double max=0;
            for (int i=0;i<n;i++) for (int j=i+1;j<n;j++) { double val=Math.abs(a[i][j]); if (val>max){max=val;p=i;q=j;} }
            if (max < 1e-12) break;
            double app=a[p][p], aqq=a[q][q], apq=a[p][q];
            double phi = 0.5 * Math.atan2(2*apq, (aqq-app));
            double c = Math.cos(phi), s=Math.sin(phi);
            for (int k=0;k<n;k++){
                double aik=a[k][p], akq=a[k][q];
                a[k][p]=c*aik - s*akq; a[k][q]=s*aik + c*akq;
            }
            for (int k=0;k<n;k++){
                double aip=a[p][k], aiq=a[q][k];
                a[p][k]=c*aip - s*aiq; a[q][k]=s*aip + c*aiq;
            }
            a[p][p]=c*c*app - 2*s*c*apq + s*s*aqq;
            a[q][q]=s*s*app + 2*s*c*apq + c*c*aqq;
            a[p][q]=a[q][p]=0.0;
            for (int k=0;k<n;k++){
                double vip=V[k][p], viq=V[k][q];
                V[k][p]=c*vip - s*viq; V[k][q]=s*vip + c*viq;
            }
        }
        return V;
    }

    private static double[] column(double[][] M, int col){
        double[] v = new double[M.length]; for (int i=0;i<M.length;i++) v[i]=M[i][col]; return v;
    }

    private static double normAv(double[][] A, double[] v){
        double sum=0; int rows=A.length, cols=A[0].length;
        for (int r=0;r<rows;r++){
            double acc=0; for (int c=0;c<cols;c++) acc += A[r][c]*v[c];
            sum += acc*acc;
        }
        return Math.sqrt(sum);
    }
}
