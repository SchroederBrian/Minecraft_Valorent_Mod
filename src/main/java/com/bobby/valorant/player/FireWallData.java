package com.bobby.valorant.player;

import com.bobby.valorant.Config;
import com.bobby.valorant.network.SyncFireWallChargesPacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FireWallData {
    private static final String ROOT = "ValorantFireWall";
    private static final String CHARGES = "Charges";
    private static final String KILL_PROGRESS = "KillProgress";

    private FireWallData() {}

    public static int getCharges(Player player) {
        return root(player).getIntOr(CHARGES, 0);
    }

    public static void setCharges(Player player, int charges) {
        int actual = Math.max(0, charges);
        root(player).putInt(CHARGES, actual);
        if (player instanceof ServerPlayer sp && !player.level().isClientSide) {
            PacketDistributor.sendToPlayer(sp, new SyncFireWallChargesPacket(actual));
        }
    }

    public static boolean ensureInitialized(Player player) {
        CompoundTag tag = root(player);
        if (!tag.contains(CHARGES)) {
            int max = Config.COMMON.firewallMaxCharges.get();
            tag.putInt(CHARGES, max);
            tag.putInt(KILL_PROGRESS, 0);
            return true;
        }
        return false;
    }

    public static boolean tryConsumeCharge(Player player) {
        ensureInitialized(player);
        int charges = getCharges(player);
        if (charges <= 0) return false;
        setCharges(player, charges - 1);
        return true;
    }

    public static boolean restoreCharge(Player player) {
        ensureInitialized(player);
        int max = Config.COMMON.firewallMaxCharges.get();
        int current = getCharges(player);
        if (current >= max) return false;
        setCharges(player, current + 1);
        return true;
    }

    public static void addKillProgress(ServerPlayer player) {
        ensureInitialized(player);
        CompoundTag tag = root(player);
        int current = tag.getIntOr(KILL_PROGRESS, 0) + 1;
        int threshold = Config.COMMON.firewallKillRechargeThreshold.get();
        if (current >= threshold) {
            if (restoreCharge(player)) current -= threshold;
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
