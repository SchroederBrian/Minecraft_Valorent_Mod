package com.bobby.valorant.client;

import com.bobby.valorant.Config;

public final class SkySmokeCalibrationClient {
    private SkySmokeCalibrationClient() {}

    // Client-side bounds that get synced from server
    private static int minX = Config.COMMON.skySmokeMapMinX.get();
    private static int minZ = Config.COMMON.skySmokeMapMinZ.get();
    private static int maxX = Config.COMMON.skySmokeMapMaxX.get();
    private static int maxZ = Config.COMMON.skySmokeMapMaxZ.get();
    private static double rotationDegrees = Config.COMMON.skySmokeMapRotationDegrees.get();

    // Similarity transform: world = s·R·mapUV + t, stored as a,b,tx,tz
    private static double transformA = 0.0; // scale * cos(theta)
    private static double transformB = 0.0; // scale * sin(theta)
    private static double transformTx = 0.0; // translation X
    private static double transformTz = 0.0; // translation Z
    private static boolean hasTransform = false;

    public static void setBounds(int minX, int minZ, int maxX, int maxZ, double rotationDegrees) {
        SkySmokeCalibrationClient.minX = minX;
        SkySmokeCalibrationClient.minZ = minZ;
        SkySmokeCalibrationClient.maxX = maxX;
        SkySmokeCalibrationClient.maxZ = maxZ;
        SkySmokeCalibrationClient.rotationDegrees = rotationDegrees;
    }

    public static void setTransform(double a, double b, double tx, double tz) {
        transformA = a;
        transformB = b;
        transformTx = tx;
        transformTz = tz;
        hasTransform = true;
    }

    public static void clearTransform() {
        hasTransform = false;
        transformA = transformB = transformTx = transformTz = 0.0;
    }

    public static int getMinX() { return minX; }
    public static int getMinZ() { return minZ; }
    public static int getMaxX() { return maxX; }
    public static int getMaxZ() { return maxZ; }
    public static double getRotationDegrees() { return rotationDegrees; }

    public static boolean hasTransform() { return hasTransform; }
    public static double getTransformA() { return transformA; }
    public static double getTransformB() { return transformB; }
    public static double getTransformTx() { return transformTx; }
    public static double getTransformTz() { return transformTz; }
}
