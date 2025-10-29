package com.bobby.valorant.player;

import com.bobby.valorant.network.SyncUltimatePointsPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public final class UltimateData {
    private static final String ROOT = "ValorantUltimate";
    private static final String POINTS = "Points";

    private UltimateData() {}

    public static int getPoints(Player player) {
        return root(player).getIntOr(POINTS, 0);
    }

    public static void setPoints(Player player, int points) {
        int actualPoints = Math.max(0, points);
        root(player).putInt(POINTS, actualPoints);

        if (player instanceof ServerPlayer serverPlayer && !player.level().isClientSide) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncUltimatePointsPacket(actualPoints));
        }
    }

    public static void addPoint(Player player) {
        if (!player.level().isClientSide) {
            setPoints(player, getPoints(player) + 1);
        }
    }

    public static void resetPoints(Player player) {
        setPoints(player, 0);
    }

    public static boolean ensureInitialized(Player player) {
        CompoundTag tag = root(player);
        if (!tag.contains(POINTS)) {
            tag.putInt(POINTS, 0);
            return true;
        }
        return false;
    }

    public static void copy(Player original, Player clone) {
        CompoundTag originalRoot = root(original).copy();
        clone.getPersistentData().put(ROOT, originalRoot);
    }

    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        return persistent.getCompound(ROOT).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            persistent.put(ROOT, created);
            return created;
        });
    }
}
