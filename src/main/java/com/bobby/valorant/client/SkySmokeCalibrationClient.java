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

    // Unified transform (homography) uv->world
    private static String model = "SIMILARITY";
    private static double[] H = new double[]{1,0,0, 0,1,0, 0,0,1};
    private static double[] Hinv = new double[]{1,0,0, 0,1,0, 0,0,1};
    private static boolean hasTransform = false;

    public static void setBounds(int minX, int minZ, int maxX, int maxZ, double rotationDegrees) {
        SkySmokeCalibrationClient.minX = minX;
        SkySmokeCalibrationClient.minZ = minZ;
        SkySmokeCalibrationClient.maxX = maxX;
        SkySmokeCalibrationClient.maxZ = maxZ;
        SkySmokeCalibrationClient.rotationDegrees = rotationDegrees;
    }

    public static void setTransform(String modelName, double[] h) {
        model = modelName != null ? modelName : "SIMILARITY";
        if (h != null && h.length == 9) {
            H = h.clone();
        } else {
            H = new double[]{1,0,0, 0,1,0, 0,0,1};
        }
        Hinv = invert3x3(H);
        hasTransform = true;
    }

    public static void clearTransform() {
        hasTransform = false;
        H = new double[]{1,0,0, 0,1,0, 0,0,1};
        Hinv = new double[]{1,0,0, 0,1,0, 0,0,1};
    }

    public static int getMinX() { return minX; }
    public static int getMinZ() { return minZ; }
    public static int getMaxX() { return maxX; }
    public static int getMaxZ() { return maxZ; }
    public static double getRotationDegrees() { return rotationDegrees; }

    public static boolean hasTransform() { return hasTransform; }
    public static String getModel() { return model; }
    public static double[] getH() { return H; }
    public static double[] getHinv() { return Hinv; }

    public static double[] uvToWorld(double u, double v) {
        double x = H[0]*u + H[1]*v + H[2];
        double z = H[3]*u + H[4]*v + H[5];
        double w = H[6]*u + H[7]*v + H[8];
        if (w == 0.0) w = 1e-9;
        return new double[]{ x / w, z / w };
    }

    public static double[] worldToUv(double wx, double wz) {
        double u = Hinv[0]*wx + Hinv[1]*wz + Hinv[2];
        double v = Hinv[3]*wx + Hinv[4]*wz + Hinv[5];
        double w = Hinv[6]*wx + Hinv[7]*wz + Hinv[8];
        if (w == 0.0) w = 1e-9;
        return new double[]{ u / w, v / w };
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
        double Hh = -(a*f - c*d);
        double I = a*e - b*d;
        double det = a*A + b*B + c*C;
        if (Math.abs(det) < 1e-12) det = 1e-12;
        double invDet = 1.0/det;
        return new double[]{ A*invDet, D*invDet, G*invDet,  B*invDet, E*invDet, Hh*invDet,  C*invDet, F*invDet, I*invDet };
    }
}
