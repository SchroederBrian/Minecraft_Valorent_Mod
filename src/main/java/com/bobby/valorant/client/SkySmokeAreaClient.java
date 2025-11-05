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
}
