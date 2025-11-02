package com.bobby.valorant.spawn;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * Polygonal team spawn area defined on the XZ plane at a fixed Y.
 */
public final class SpawnArea {
    public final String teamId; // "A" or "V"
    public final ResourceLocation dimension;
    public final int y;
    public final List<BlockPos> vertices; // Only X and Z are used; Y will equal this.y

    public SpawnArea(String teamId, ResourceLocation dimension, int y, List<BlockPos> vertices) {
        this.teamId = teamId;
        this.dimension = dimension;
        this.y = y;
        this.vertices = List.copyOf(vertices);
    }
}


