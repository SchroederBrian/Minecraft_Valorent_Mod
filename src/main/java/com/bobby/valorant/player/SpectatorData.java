package com.bobby.valorant.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class SpectatorData {
    private SpectatorData() {}

    private static final String ROOT = "ValorantSpectator";
    private static final String DIM = "Dim";
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String Z = "Z";

    public static void markDeath(ServerPlayer sp) {
        CompoundTag tag = root(sp);
        ResourceKey<Level> dim = ((ServerLevel) sp.level()).dimension();
        tag.putString(DIM, dim.location().toString());
        tag.putDouble(X, sp.getX());
        tag.putDouble(Y, sp.getY());
        tag.putDouble(Z, sp.getZ());
    }

    public static boolean hasDeath(ServerPlayer sp) {
        CompoundTag tag = root(sp);
        return tag.contains(DIM) && tag.contains(X) && tag.contains(Y) && tag.contains(Z);
    }

    public static String dimId(ServerPlayer sp) { return root(sp).getStringOr(DIM, "minecraft:overworld"); }
    public static double x(ServerPlayer sp) { return root(sp).getDoubleOr(X, 0.0D); }
    public static double y(ServerPlayer sp) { return root(sp).getDoubleOr(Y, 0.0D); }
    public static double z(ServerPlayer sp) { return root(sp).getDoubleOr(Z, 0.0D); }

    private static CompoundTag root(ServerPlayer sp) {
        CompoundTag persistent = sp.getPersistentData();
        return persistent.getCompound(ROOT).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            persistent.put(ROOT, created);
            return created;
        });
    }
}
