package com.bobby.valorant.player;

import com.bobby.valorant.Config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class CurveballData {
    private static final String ROOT = "ValorantCurveball";
    private static final String CHARGES = "Charges";
    private static final String KILL_PROGRESS = "KillProgress";

    private CurveballData() {}

    public static int getCharges(Player player) {
        return root(player).getIntOr(CHARGES, 0);
    }

    public static void setCharges(Player player, int charges) {
        root(player).putInt(CHARGES, Math.max(0, charges));
    }

    public static boolean ensureInitialized(Player player) {
        CompoundTag tag = root(player);
        if (!tag.contains(CHARGES)) {
            int max = Config.COMMON.curveballMaxCharges.get();
            tag.putInt(CHARGES, max);
            tag.putInt(KILL_PROGRESS, 0);
            return true;
        }
        return false;
    }

    public static boolean tryConsumeCharge(Player player) {
        ensureInitialized(player);
        int charges = getCharges(player);
        if (charges <= 0) {
            return false;
        }
        setCharges(player, charges - 1);
        return true;
    }

    public static boolean restoreCharge(Player player) {
        ensureInitialized(player);
        int max = Config.COMMON.curveballMaxCharges.get();
        int current = getCharges(player);
        if (current >= max) {
            return false;
        }
        setCharges(player, current + 1);
        return true;
    }

    public static void resetKillProgress(Player player) {
        root(player).putInt(KILL_PROGRESS, 0);
    }

    public static void addKillProgress(ServerPlayer player) {
        ensureInitialized(player);
        CompoundTag tag = root(player);
        int current = tag.getIntOr(KILL_PROGRESS, 0) + 1;
        int threshold = Config.COMMON.curveballKillRechargeThreshold.get();
        if (current >= threshold) {
            if (restoreCharge(player)) {
                current -= threshold;
            }
        }
        tag.putInt(KILL_PROGRESS, current);
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

