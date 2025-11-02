package com.bobby.valorant.spawn.client;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public final class SpawnAreaClientState {
    private SpawnAreaClientState() {}

    public static volatile ResourceLocation dimension = null;
    public static volatile int yA = 0;
    public static volatile int yV = 0;
    public static volatile java.util.List<BlockPos> vertsA = java.util.List.of();
    public static volatile java.util.List<BlockPos> vertsV = java.util.List.of();
    public static volatile int ySiteA = 0;
    public static volatile int ySiteB = 0;
    public static volatile int ySiteC = 0;
    public static volatile java.util.List<BlockPos> vertsSiteA = java.util.List.of();
    public static volatile java.util.List<BlockPos> vertsSiteB = java.util.List.of();
    public static volatile java.util.List<BlockPos> vertsSiteC = java.util.List.of();

    public static void update(ResourceLocation dim, int ya, List<BlockPos> a, int yv, List<BlockPos> v,
                              int ysa, List<BlockPos> sa, int ysb, List<BlockPos> sb, int ysc, List<BlockPos> sc) {
        dimension = dim;
        yA = ya;
        yV = yv;
        vertsA = a != null ? java.util.List.copyOf(a) : java.util.List.of();
        vertsV = v != null ? java.util.List.copyOf(v) : java.util.List.of();
        ySiteA = ysa;
        ySiteB = ysb;
        ySiteC = ysc;
        vertsSiteA = sa != null ? java.util.List.copyOf(sa) : java.util.List.of();
        vertsSiteB = sb != null ? java.util.List.copyOf(sb) : java.util.List.of();
        vertsSiteC = sc != null ? java.util.List.copyOf(sc) : java.util.List.of();
    }
}


