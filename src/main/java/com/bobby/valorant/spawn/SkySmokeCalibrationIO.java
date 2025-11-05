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

    static Map<ResourceLocation, SkySmokeCalibration.SimilarityTransform> loadTransforms(Path path) {
        try {
            if (!Files.exists(path)) {
                return new HashMap<>();
            }
            try (Reader r = Files.newBufferedReader(path)) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);
                if (root == null) return new HashMap<>();

                Map<ResourceLocation, SkySmokeCalibration.SimilarityTransform> transforms = new HashMap<>();

                if (root.has("dimensions") && root.get("dimensions").isJsonObject()) {
                    JsonObject dimensions = root.getAsJsonObject("dimensions");
                    for (Map.Entry<String, JsonElement> entry : dimensions.entrySet()) {
                        ResourceLocation dimension = ResourceLocation.tryParse(entry.getKey());
                        if (dimension != null && entry.getValue().isJsonObject()) {
                            JsonObject transformObj = entry.getValue().getAsJsonObject();
                            if (transformObj.has("a") && transformObj.has("b") && transformObj.has("tx") && transformObj.has("tz")) {
                                double a = transformObj.get("a").getAsDouble();
                                double b = transformObj.get("b").getAsDouble();
                                double tx = transformObj.get("tx").getAsDouble();
                                double tz = transformObj.get("tz").getAsDouble();
                                transforms.put(dimension, new SkySmokeCalibration.SimilarityTransform(a, b, tx, tz));
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

    static void saveTransforms(Path path, Map<ResourceLocation, SkySmokeCalibration.SimilarityTransform> transforms) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                JsonObject root = new JsonObject();
                JsonObject dimensions = new JsonObject();

                for (Map.Entry<ResourceLocation, SkySmokeCalibration.SimilarityTransform> entry : transforms.entrySet()) {
                    JsonObject transformObj = new JsonObject();
                    SkySmokeCalibration.SimilarityTransform transform = entry.getValue();
                    transformObj.addProperty("a", transform.a);
                    transformObj.addProperty("b", transform.b);
                    transformObj.addProperty("tx", transform.tx);
                    transformObj.addProperty("tz", transform.tz);
                    dimensions.add(entry.getKey().toString(), transformObj);
                }

                root.add("dimensions", dimensions);
                GSON.toJson(root, w);
            }
        } catch (IOException ignored) {}
    }
}
