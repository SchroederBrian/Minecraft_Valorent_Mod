package com.bobby.valorant.spawn;

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

final class SpawnAreasConfigIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SpawnAreasConfigIO() {}

    static Map<ResourceLocation, Map<String, SpawnArea>> load(Path path) {
        ensureFileExists(path);
        try (Reader r = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            Map<ResourceLocation, Map<String, SpawnArea>> result = new HashMap<>();
            if (root == null) return result;

            JsonObject dims = root.has("dimensions") && root.get("dimensions").isJsonObject()
                    ? root.getAsJsonObject("dimensions") : new JsonObject();
            for (Map.Entry<String, JsonElement> dimEntry : dims.entrySet()) {
                ResourceLocation dimId = ResourceLocation.parse(dimEntry.getKey());
                JsonObject byTeam = dimEntry.getValue().getAsJsonObject();
                Map<String, SpawnArea> teamMap = new HashMap<>();
                for (Map.Entry<String, JsonElement> teamEntry : byTeam.entrySet()) {
                    String teamId = normalizeTeamId(teamEntry.getKey());
                    JsonObject obj = teamEntry.getValue().getAsJsonObject();
                    int y = obj.has("y") ? obj.get("y").getAsInt() : 64;
                    List<BlockPos> vertices = new ArrayList<>();
                    if (obj.has("vertices") && obj.get("vertices").isJsonArray()) {
                        for (JsonElement el : obj.getAsJsonArray("vertices")) {
                            if (!el.isJsonArray() || el.getAsJsonArray().size() < 2) continue;
                            int x = el.getAsJsonArray().get(0).getAsInt();
                            int z = el.getAsJsonArray().get(1).getAsInt();
                            vertices.add(new BlockPos(x, y, z));
                        }
                    }
                    if (vertices.size() >= 3) {
                        teamMap.put(teamId, new SpawnArea(teamId, dimId, y, vertices));
                    }
                }
                if (!teamMap.isEmpty()) {
                    result.put(dimId, teamMap);
                }
            }
            return result;
        } catch (IOException ex) {
            return java.util.Collections.emptyMap();
        }
    }

    static Map<ResourceLocation, Map<String, SpawnArea>> loadBombSites(Path path) {
        ensureFileExists(path);
        try (Reader r = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            Map<ResourceLocation, Map<String, SpawnArea>> result = new HashMap<>();
            if (root == null) return result;

            JsonObject dims = root.has("dimensions") && root.get("dimensions").isJsonObject()
                    ? root.getAsJsonObject("dimensions") : new JsonObject();
            for (Map.Entry<String, JsonElement> dimEntry : dims.entrySet()) {
                ResourceLocation dimId = ResourceLocation.parse(dimEntry.getKey());
                JsonObject dimObj = dimEntry.getValue().getAsJsonObject();
                if (!dimObj.has("bombSites")) continue;
                JsonObject sites = dimObj.getAsJsonObject("bombSites");
                Map<String, SpawnArea> siteMap = new HashMap<>();
                for (String site : new String[] {"A","B","C"}) {
                    if (!sites.has(site)) continue;
                    JsonObject obj = sites.getAsJsonObject(site);
                    int y = obj.has("y") ? obj.get("y").getAsInt() : 64;
                    List<BlockPos> vertices = new ArrayList<>();
                    if (obj.has("vertices") && obj.get("vertices").isJsonArray()) {
                        for (JsonElement el : obj.getAsJsonArray("vertices")) {
                            if (!el.isJsonArray() || el.getAsJsonArray().size() < 2) continue;
                            int x = el.getAsJsonArray().get(0).getAsInt();
                            int z = el.getAsJsonArray().get(1).getAsInt();
                            vertices.add(new BlockPos(x, y, z));
                        }
                    }
                    if (vertices.size() >= 3) {
                        siteMap.put(site, new SpawnArea(site, dimId, y, vertices));
                    }
                }
                if (!siteMap.isEmpty()) {
                    result.put(dimId, siteMap);
                }
            }
            return result;
        } catch (IOException ex) {
            return java.util.Collections.emptyMap();
        }
    }

    static void save(Path path, Map<ResourceLocation, Map<String, SpawnArea>> data) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(toJson(data), w);
            }
        } catch (IOException ignored) {}
    }

    static void saveAll(Path path,
                        Map<ResourceLocation, Map<String, SpawnArea>> spawnAreas,
                        Map<ResourceLocation, Map<String, SpawnArea>> bombSites) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(toJsonAll(spawnAreas, bombSites), w);
            }
        } catch (IOException ignored) {}
    }

    private static JsonObject toJson(Map<ResourceLocation, Map<String, SpawnArea>> data) {
        JsonObject root = new JsonObject();
        JsonObject dims = new JsonObject();
        for (Map.Entry<ResourceLocation, Map<String, SpawnArea>> dimEntry : data.entrySet()) {
            JsonObject byTeam = new JsonObject();
            Map<String, SpawnArea> teamMap = dimEntry.getValue();
            if (teamMap != null) {
                for (Map.Entry<String, SpawnArea> teamEntry : teamMap.entrySet()) {
                    SpawnArea area = teamEntry.getValue();
                    if (area == null || area.vertices == null || area.vertices.size() < 3) continue;
                    byTeam.add(teamEntry.getKey(), area(area.y, toVerts(area.vertices)));
                }
            }
            dims.add(dimEntry.getKey().toString(), byTeam);
        }
        root.add("dimensions", dims);
        return root;
    }

    private static JsonObject toJsonAll(Map<ResourceLocation, Map<String, SpawnArea>> spawnAreas,
                                        Map<ResourceLocation, Map<String, SpawnArea>> bombSites) {
        JsonObject root = new JsonObject();
        JsonObject dims = new JsonObject();
        // Collect all dimensions present in either map
        java.util.Set<ResourceLocation> allDims = new java.util.HashSet<>();
        allDims.addAll(spawnAreas.keySet());
        allDims.addAll(bombSites.keySet());
        for (ResourceLocation dim : allDims) {
            JsonObject dimObj = new JsonObject();
            // Spawns directly under the dim object for backward compatibility
            Map<String, SpawnArea> spawns = spawnAreas.get(dim);
            if (spawns != null && !spawns.isEmpty()) {
                for (Map.Entry<String, SpawnArea> e : spawns.entrySet()) {
                    SpawnArea area = e.getValue();
                    if (area == null || area.vertices == null || area.vertices.size() < 3) continue;
                    dimObj.add(e.getKey(), area(area.y, toVerts(area.vertices)));
                }
            }
            // Bomb sites under bombSites
            Map<String, SpawnArea> sites = bombSites.get(dim);
            if (sites != null && !sites.isEmpty()) {
                JsonObject siteObj = new JsonObject();
                for (String site : new String[] {"A","B","C"}) {
                    SpawnArea area = sites.get(site);
                    if (area == null) continue;
                    if (area.vertices == null || area.vertices.size() < 3) continue;
                    siteObj.add(site, area(area.y, toVerts(area.vertices)));
                }
                if (!siteObj.entrySet().isEmpty()) {
                    dimObj.add("bombSites", siteObj);
                }
            }
            dims.add(dim.toString(), dimObj);
        }
        root.add("dimensions", dims);
        return root;
    }

    private static List<int[]> toVerts(List<BlockPos> vertices) {
        List<int[]> list = new ArrayList<>(vertices.size());
        for (BlockPos p : vertices) {
            list.add(new int[] { p.getX(), p.getZ() });
        }
        return list;
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
        overworld.add("A", area(64, square( -8, -8, 8, 8)));
        overworld.add("V", area(64, square( 32, -8, 48, 8)));
        dims.add("minecraft:overworld", overworld);
        root.add("dimensions", dims);
        return root;
    }

    private static JsonObject area(int y, List<int[]> verts) {
        JsonObject o = new JsonObject();
        o.addProperty("y", y);
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        for (int[] v : verts) {
            com.google.gson.JsonArray p = new com.google.gson.JsonArray();
            p.add(v[0]);
            p.add(v[1]);
            arr.add(p);
        }
        o.add("vertices", arr);
        return o;
    }

    private static List<int[]> square(int x1, int z1, int x2, int z2) {
        List<int[]> list = new ArrayList<>();
        list.add(new int[] { x1, z1 });
        list.add(new int[] { x2, z1 });
        list.add(new int[] { x2, z2 });
        list.add(new int[] { x1, z2 });
        return list;
    }

    static String normalizeTeamId(String raw) {
        return ("A".equalsIgnoreCase(raw)) ? "A" : "V";
    }
}


