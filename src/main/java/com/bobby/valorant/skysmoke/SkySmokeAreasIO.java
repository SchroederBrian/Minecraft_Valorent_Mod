package com.bobby.valorant.skysmoke;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

final class SkySmokeAreasIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SkySmokeAreasIO() {}

    static Map<ResourceLocation, SkySmokeArea.SkySmokeDimensionAreas> load(Path path) {
        ensureFileExists(path);
        try (Reader r = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            Map<ResourceLocation, SkySmokeArea.SkySmokeDimensionAreas> result = new HashMap<>();
            if (root == null) return result;

            JsonObject dims = root.has("dimensions") && root.get("dimensions").isJsonObject()
                    ? root.getAsJsonObject("dimensions") : new JsonObject();
            for (Map.Entry<String, JsonElement> dimEntry : dims.entrySet()) {
                ResourceLocation dimId = ResourceLocation.parse(dimEntry.getKey());
                JsonObject dimObj = dimEntry.getValue().getAsJsonObject();

                Map<String, com.bobby.valorant.skysmoke.SkySmokeArea> allowed = new HashMap<>();
                Map<String, com.bobby.valorant.skysmoke.SkySmokeArea> blocked = new HashMap<>();

                // Load allowed zones
                if (dimObj.has("allowed") && dimObj.get("allowed").isJsonObject()) {
                    JsonObject allowedObj = dimObj.getAsJsonObject("allowed");
                    for (Map.Entry<String, JsonElement> entry : allowedObj.entrySet()) {
                        com.bobby.valorant.skysmoke.SkySmokeArea area = parseArea(entry.getKey(), entry.getValue().getAsJsonObject(), com.bobby.valorant.skysmoke.SkySmokeArea.Type.ALLOWED);
                        if (area != null) allowed.put(entry.getKey(), area);
                    }
                }

                // Load blocked zones
                if (dimObj.has("blocked") && dimObj.get("blocked").isJsonObject()) {
                    JsonObject blockedObj = dimObj.getAsJsonObject("blocked");
                    for (Map.Entry<String, JsonElement> entry : blockedObj.entrySet()) {
                        com.bobby.valorant.skysmoke.SkySmokeArea area = parseArea(entry.getKey(), entry.getValue().getAsJsonObject(), com.bobby.valorant.skysmoke.SkySmokeArea.Type.BLOCKED);
                        if (area != null) blocked.put(entry.getKey(), area);
                    }
                }

                if (!allowed.isEmpty() || !blocked.isEmpty()) {
                    result.put(dimId, new com.bobby.valorant.skysmoke.SkySmokeArea.SkySmokeDimensionAreas(allowed, blocked));
                }
            }
            return result;
        } catch (IOException ex) {
            return java.util.Collections.emptyMap();
        }
    }

    private static com.bobby.valorant.skysmoke.SkySmokeArea parseArea(String id, JsonObject obj, com.bobby.valorant.skysmoke.SkySmokeArea.Type type) {
        if (!obj.has("vertices") || !obj.get("vertices").isJsonArray()) return null;

        com.bobby.valorant.skysmoke.SkySmokeArea.Mode mode = com.bobby.valorant.skysmoke.SkySmokeArea.Mode.GROUND;
        if (obj.has("mode")) {
            String modeStr = obj.get("mode").getAsString();
            mode = "FIXED_Y".equals(modeStr) ? com.bobby.valorant.skysmoke.SkySmokeArea.Mode.FIXED_Y : com.bobby.valorant.skysmoke.SkySmokeArea.Mode.GROUND;
        }

        int y = obj.has("y") ? obj.get("y").getAsInt() : 64;
        boolean enabled = !obj.has("enabled") || obj.get("enabled").getAsBoolean();

        List<BlockPos> vertices = new ArrayList<>();
        for (JsonElement el : obj.getAsJsonArray("vertices")) {
            if (!el.isJsonArray()) continue;
            var arr = el.getAsJsonArray();

            // Support both old [x,z] format and new [x,y,z] format
            if (arr.size() >= 3) {
                // New format: [x,y,z]
                int x = arr.get(0).getAsInt();
                int vertexY = arr.get(1).getAsInt();
                int z = arr.get(2).getAsInt();
                vertices.add(new BlockPos(x, vertexY, z));
            } else if (arr.size() >= 2) {
                // Old format: [x,z] - use area y for backward compatibility
                int x = arr.get(0).getAsInt();
                int z = arr.get(1).getAsInt();
                vertices.add(new BlockPos(x, y, z));
            }
        }

        if (vertices.size() < 3) return null;

        return new com.bobby.valorant.skysmoke.SkySmokeArea(id, type, mode, y, enabled, vertices);
    }

    static void save(Path path, Map<ResourceLocation, com.bobby.valorant.skysmoke.SkySmokeArea.SkySmokeDimensionAreas> data) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(toJson(data), w);
            }
        } catch (IOException ignored) {}
    }

    private static JsonObject toJson(Map<ResourceLocation, com.bobby.valorant.skysmoke.SkySmokeArea.SkySmokeDimensionAreas> data) {
        JsonObject root = new JsonObject();
        JsonObject dims = new JsonObject();
        for (Map.Entry<ResourceLocation, com.bobby.valorant.skysmoke.SkySmokeArea.SkySmokeDimensionAreas> dimEntry : data.entrySet()) {
            JsonObject dimObj = new JsonObject();

            // Save allowed zones
            if (!dimEntry.getValue().allowed().isEmpty()) {
                JsonObject allowedObj = new JsonObject();
                for (Map.Entry<String, SkySmokeArea> entry : dimEntry.getValue().allowed().entrySet()) {
                    allowedObj.add(entry.getKey(), toJsonArea(entry.getValue()));
                }
                dimObj.add("allowed", allowedObj);
            }

            // Save blocked zones
            if (!dimEntry.getValue().blocked().isEmpty()) {
                JsonObject blockedObj = new JsonObject();
                for (Map.Entry<String, SkySmokeArea> entry : dimEntry.getValue().blocked().entrySet()) {
                    blockedObj.add(entry.getKey(), toJsonArea(entry.getValue()));
                }
                dimObj.add("blocked", blockedObj);
            }

            dims.add(dimEntry.getKey().toString(), dimObj);
        }
        root.add("dimensions", dims);
        return root;
    }

    private static JsonObject toJsonArea(SkySmokeArea area) {
        JsonObject obj = new JsonObject();
        obj.addProperty("mode", area.mode().name());
        obj.addProperty("y", area.y());
        obj.addProperty("enabled", area.enabled());

        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        for (BlockPos p : area.vertices()) {
            com.google.gson.JsonArray point = new com.google.gson.JsonArray();
            point.add(p.getX());
            point.add(p.getY());
            point.add(p.getZ());
            arr.add(point);
        }
        obj.add("vertices", arr);
        return obj;
    }

    private static void ensureFileExists(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                try (Writer w = Files.newBufferedWriter(path)) {
                    GSON.toJson(defaultJson(), w);
                }
            }
        } catch (IOException ignored) {}
    }

    private static JsonObject defaultJson() {
        JsonObject root = new JsonObject();
        JsonObject dims = new JsonObject();
        JsonObject overworld = new JsonObject();

        // Example allowed zone
        JsonObject allowed = new JsonObject();
        JsonObject midZone = new JsonObject();
        midZone.addProperty("mode", "GROUND");
        midZone.addProperty("y", 64);
        midZone.addProperty("enabled", true);
        com.google.gson.JsonArray midVerts = new com.google.gson.JsonArray();
        midVerts.add(createPoint(-10, 64, -10));
        midVerts.add(createPoint(10, 64, -10));
        midVerts.add(createPoint(10, 64, 10));
        midVerts.add(createPoint(-10, 64, 10));
        midZone.add("vertices", midVerts);
        allowed.add("mid", midZone);
        overworld.add("allowed", allowed);

        // Example blocked zone
        JsonObject blocked = new JsonObject();
        JsonObject spawnZone = new JsonObject();
        spawnZone.addProperty("mode", "GROUND");
        spawnZone.addProperty("enabled", true);
        com.google.gson.JsonArray spawnVerts = new com.google.gson.JsonArray();
        spawnVerts.add(createPoint(-5, 64, -5));
        spawnVerts.add(createPoint(5, 64, -5));
        spawnVerts.add(createPoint(5, 64, 5));
        spawnVerts.add(createPoint(-5, 64, 5));
        spawnZone.add("vertices", spawnVerts);
        blocked.add("spawn", spawnZone);
        overworld.add("blocked", blocked);

        dims.add("minecraft:overworld", overworld);
        root.add("dimensions", dims);
        return root;
    }

    private static com.google.gson.JsonArray createPoint(int x, int y, int z) {
        com.google.gson.JsonArray point = new com.google.gson.JsonArray();
        point.add(x);
        point.add(y);
        point.add(z);
        return point;
    }
}
