package com.bobby.valorant.player;

import com.bobby.valorant.network.SyncReloadStatePacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ReloadStateData {
    private static final String ROOT = "ValorantReloadState";
    private static final String IS_RELOADING = "IsReloading";
    private static final String TICKS_REMAINING = "TicksRemaining";
    private static final String TOTAL_TICKS = "TotalTicks";

    private ReloadStateData() {}

    public static boolean isReloading(Player player) {
        CompoundTag tag = root(player);
        return tag.getBoolean(IS_RELOADING).orElse(false);
    }

    public static void startReload(Player player, int slot, int durationTicks) {
        CompoundTag tag = root(player);
        tag.putBoolean(IS_RELOADING, true);
        tag.putInt(TICKS_REMAINING, durationTicks);
        tag.putInt(TOTAL_TICKS, durationTicks);

        // Sync to client if on server
        if (player instanceof ServerPlayer serverPlayer && !player.level().isClientSide) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncReloadStatePacket(true, durationTicks, durationTicks));
        }
    }

    public static void cancelReload(Player player) {
        CompoundTag tag = root(player);
        if (tag.getBoolean(IS_RELOADING).orElse(false)) {
            tag.putBoolean(IS_RELOADING, false);
            tag.remove(TICKS_REMAINING);
            tag.remove(TOTAL_TICKS);

            // Sync to client if on server
            if (player instanceof ServerPlayer serverPlayer && !player.level().isClientSide) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncReloadStatePacket(false, 0, 0));
            }
        }
    }

    public static void tick(ServerPlayer player) {
        CompoundTag tag = root(player);
        if (!tag.getBoolean(IS_RELOADING).orElse(false)) {
            return;
        }

        int ticksRemaining = tag.getInt(TICKS_REMAINING).orElse(0) - 1;
        if (ticksRemaining <= 0) {
            // Reload complete
            tag.putBoolean(IS_RELOADING, false);
            tag.remove(TICKS_REMAINING);
            tag.remove(TOTAL_TICKS);
            // Sync to client
            PacketDistributor.sendToPlayer(player, new SyncReloadStatePacket(false, 0, 0));
        } else {
            // Still reloading
            tag.putInt(TICKS_REMAINING, ticksRemaining);
            // Sync to client
            PacketDistributor.sendToPlayer(player, new SyncReloadStatePacket(true, ticksRemaining, tag.getInt(TOTAL_TICKS).orElse(0)));
        }
    }

    public static float getReloadProgress(Player player) {
        CompoundTag tag = root(player);
        if (!tag.getBoolean(IS_RELOADING).orElse(false)) {
            return 0.0f;
        }
        int totalTicks = tag.getInt(TOTAL_TICKS).orElse(0);
        int remainingTicks = tag.getInt(TICKS_REMAINING).orElse(0);
        return (totalTicks - remainingTicks) / (float) totalTicks;
    }

    public static int getTicksRemaining(Player player) {
        CompoundTag tag = root(player);
        return tag.getBoolean(IS_RELOADING).orElse(false) ? tag.getInt(TICKS_REMAINING).orElse(0) : 0;
    }

    public static int getTotalTicks(Player player) {
        CompoundTag tag = root(player);
        return tag.getBoolean(IS_RELOADING).orElse(false) ? tag.getInt(TOTAL_TICKS).orElse(0) : 0;
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
