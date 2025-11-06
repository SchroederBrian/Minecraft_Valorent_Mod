package com.bobby.valorant.spawn;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;

final class SkySmokeCalibrationIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SkySmokeCalibrationIO() {}

    static SkySmokeCalibration.SkySmokeBounds load(Path path) {
        try {
            if (!Files.exists(path)) {
                return null;
            }
            try (Reader r = Files.newBufferedReader(path)) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);
                if (root == null) return null;

                int minX = root.has("minX") ? root.get("minX").getAsInt() : 0;
                int minZ = root.has("minZ") ? root.get("minZ").getAsInt() : 0;
                int maxX = root.has("maxX") ? root.get("maxX").getAsInt() : 512;
                int maxZ = root.has("maxZ") ? root.get("maxZ").getAsInt() : 512;
                double rotation = root.has("rotationDegrees") ? root.get("rotationDegrees").getAsDouble() : 0.0;

                return new SkySmokeCalibration.SkySmokeBounds(minX, minZ, maxX, maxZ, rotation);
            }
        } catch (IOException ex) {
            return null;
        }
    }

    static void save(Path path, SkySmokeCalibration.SkySmokeBounds bounds) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                JsonObject root = new JsonObject();
                root.addProperty("minX", bounds.minX());
                root.addProperty("minZ", bounds.minZ());
                root.addProperty("maxX", bounds.maxX());
                root.addProperty("maxZ", bounds.maxZ());
                root.addProperty("rotationDegrees", bounds.rotationDegrees());
                GSON.toJson(root, w);
            }
        } catch (IOException ignored) {}
    }

    static Map<ResourceLocation, SkySmokeCalibration.Transform2D> loadTransforms(Path path) {
        try {
            if (!Files.exists(path)) {
                return new HashMap<>();
            }
            try (Reader r = Files.newBufferedReader(path)) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);
                if (root == null) return new HashMap<>();

                Map<ResourceLocation, SkySmokeCalibration.Transform2D> transforms = new HashMap<>();

                if (root.has("dimensions") && root.get("dimensions").isJsonObject()) {
                    JsonObject dimensions = root.getAsJsonObject("dimensions");
                    for (Map.Entry<String, JsonElement> entry : dimensions.entrySet()) {
                        ResourceLocation dimension = ResourceLocation.tryParse(entry.getKey());
                        if (dimension != null && entry.getValue().isJsonObject()) {
                            JsonObject transformObj = entry.getValue().getAsJsonObject();
                            // New format: model + H[9]
                            if (transformObj.has("model") && transformObj.has("H") && transformObj.get("H").isJsonArray()) {
                                String modelStr = transformObj.get("model").getAsString();
                                java.util.List<Double> list = new java.util.ArrayList<>();
                                for (JsonElement el : transformObj.getAsJsonArray("H")) list.add(el.getAsDouble());
                                if (list.size() == 9) {
                                    double[] H = new double[9];
                                    for (int i=0;i<9;i++) H[i]=list.get(i);
                                    SkySmokeCalibration.Model model;
                                    try { model = SkySmokeCalibration.Model.valueOf(modelStr.toUpperCase()); } catch (Exception e) { model = SkySmokeCalibration.Model.SIMILARITY; }
                                    double[] Hinv = invert3x3(H);
                                    transforms.put(dimension, new SkySmokeCalibration.Transform2D(model, H, Hinv));
                                }
                            } else if (transformObj.has("a") && transformObj.has("b") && transformObj.has("tx") && transformObj.has("tz")) {
                                // Legacy format: similarity parameters
                                double a = transformObj.get("a").getAsDouble();
                                double b = transformObj.get("b").getAsDouble();
                                double tx = transformObj.get("tx").getAsDouble();
                                double tz = transformObj.get("tz").getAsDouble();
                                double[] H = new double[]{ a, -b, tx,  b, a, tz,  0, 0, 1 };
                                double[] Hinv = invert3x3(H);
                                transforms.put(dimension, new SkySmokeCalibration.Transform2D(SkySmokeCalibration.Model.SIMILARITY, H, Hinv));
                            }
                        }
                    }
                }

                return transforms;
            }
        } catch (IOException ex) {
            return new HashMap<>();
        }
    }

    static void saveTransforms(Path path, Map<ResourceLocation, SkySmokeCalibration.Transform2D> transforms) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                JsonObject root = new JsonObject();
                JsonObject dimensions = new JsonObject();

                for (Map.Entry<ResourceLocation, SkySmokeCalibration.Transform2D> entry : transforms.entrySet()) {
                    JsonObject transformObj = new JsonObject();
                    SkySmokeCalibration.Transform2D t = entry.getValue();
                    transformObj.addProperty("model", t.model.name());
                    com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
                    for (int i=0;i<9;i++) arr.add(t.H[i]);
                    transformObj.add("H", arr);
                    dimensions.add(entry.getKey().toString(), transformObj);
                }

                root.add("dimensions", dimensions);
                GSON.toJson(root, w);
            }
        } catch (IOException ignored) {}
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
}
