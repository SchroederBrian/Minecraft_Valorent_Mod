package com.bobby.valorant.client;

import java.util.Map;

import com.bobby.valorant.skysmoke.SkySmokeArea;

import net.minecraft.resources.ResourceLocation;

public final class SkySmokeAreaClient {
    private SkySmokeAreaClient() {}

    // Client-side cached areas for rendering
    private static Map<ResourceLocation, SkySmokeArea.SkySmokeDimensionAreas> AREAS = java.util.Collections.emptyMap();

    public static void setAreas(Map<ResourceLocation, SkySmokeArea.SkySmokeDimensionAreas> areas) {
        AREAS = areas != null ? Map.copyOf(areas) : java.util.Collections.emptyMap();
    }

    public static Map<ResourceLocation, SkySmokeArea.SkySmokeDimensionAreas> getAreas() {
        return AREAS;
    }

    public static SkySmokeArea.SkySmokeDimensionAreas getAreasForDimension(ResourceLocation dimension) {
        return AREAS.get(dimension);
    }

    public static boolean isInsideAllowedArea(double x, double z) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return false;
        var dimension = mc.level.dimension().location();
        var areas = getAreasForDimension(dimension);
        if (areas == null) return false;
        for (var area : areas.allowed().values()) {
            if (area.enabled() && area.isInside(x, z)) return true;
        }
        return false;
    }
}
