package com.bobby.valorant.player;

import com.bobby.valorant.network.SyncKnifeAnimationStatePacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public final class KnifeAnimationStateData {
    private static final String ROOT = "ValorantKnifeAnimationState";
    private static final String IS_ANIMATING = "IsAnimating";
    private static final String TICKS_REMAINING = "TicksRemaining";
    private static final String TOTAL_TICKS = "TotalTicks";
    private static final String ANIMATION_TYPE = "AnimationType"; // 0 = light attack, 1 = heavy attack

    public enum AnimationType {
        LIGHT_ATTACK,
        HEAVY_ATTACK
    }

    private KnifeAnimationStateData() {}

    public static boolean isAnimating(Player player) {
        CompoundTag tag = root(player);
        return tag.getBoolean(IS_ANIMATING).orElse(false);
    }

    public static void startAnimation(Player player, AnimationType type, int durationTicks) {
        CompoundTag tag = root(player);
        tag.putBoolean(IS_ANIMATING, true);
        tag.putInt(TICKS_REMAINING, durationTicks);
        tag.putInt(TOTAL_TICKS, durationTicks);
        tag.putInt(ANIMATION_TYPE, type.ordinal());

        // Sync to client if on server
        if (player instanceof ServerPlayer serverPlayer && !player.level().isClientSide) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncKnifeAnimationStatePacket(true, type, durationTicks, durationTicks));
        }
    }

    public static void cancelAnimation(Player player) {
        CompoundTag tag = root(player);
        if (tag.getBoolean(IS_ANIMATING).orElse(false)) {
            tag.putBoolean(IS_ANIMATING, false);
            tag.remove(TICKS_REMAINING);
            tag.remove(TOTAL_TICKS);
            tag.remove(ANIMATION_TYPE);

            // Sync to client if on server
            if (player instanceof ServerPlayer serverPlayer && !player.level().isClientSide) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncKnifeAnimationStatePacket(false, AnimationType.LIGHT_ATTACK, 0, 0));
            }
        }
    }

    public static void tick(ServerPlayer player) {
        CompoundTag tag = root(player);
        if (!tag.getBoolean(IS_ANIMATING).orElse(false)) {
            return;
        }

        int ticksRemaining = tag.getInt(TICKS_REMAINING).orElse(0) - 1;
        if (ticksRemaining <= 0) {
            // Animation complete
            tag.putBoolean(IS_ANIMATING, false);
            tag.remove(TICKS_REMAINING);
            tag.remove(TOTAL_TICKS);
            tag.remove(ANIMATION_TYPE);
            // Sync to client
            PacketDistributor.sendToPlayer(player, new SyncKnifeAnimationStatePacket(false, AnimationType.LIGHT_ATTACK, 0, 0));
        } else {
            // Still animating
            tag.putInt(TICKS_REMAINING, ticksRemaining);
            // Sync to client
            AnimationType type = AnimationType.values()[tag.getInt(ANIMATION_TYPE).orElse(0)];
            PacketDistributor.sendToPlayer(player, new SyncKnifeAnimationStatePacket(true, type, ticksRemaining, tag.getInt(TOTAL_TICKS).orElse(0)));
        }
    }

    public static void clientTick(Player player) {
        CompoundTag tag = root(player);
        if (!tag.getBoolean(IS_ANIMATING).orElse(false)) {
            return;
        }

        int ticksRemaining = tag.getInt(TICKS_REMAINING).orElse(0) - 1;
        if (ticksRemaining <= 0) {
            // Animation complete
            tag.putBoolean(IS_ANIMATING, false);
            tag.remove(TICKS_REMAINING);
            tag.remove(TOTAL_TICKS);
            tag.remove(ANIMATION_TYPE);
        } else {
            // Still animating
            tag.putInt(TICKS_REMAINING, ticksRemaining);
        }
    }

    public static float getAnimationProgress(Player player) {
        CompoundTag tag = root(player);
        if (!tag.getBoolean(IS_ANIMATING).orElse(false)) {
            return 0.0f;
        }
        int totalTicks = tag.getInt(TOTAL_TICKS).orElse(0);
        int remainingTicks = tag.getInt(TICKS_REMAINING).orElse(0);
        return (totalTicks - remainingTicks) / (float) totalTicks;
    }

    public static AnimationType getCurrentAnimationType(Player player) {
        CompoundTag tag = root(player);
        if (!tag.getBoolean(IS_ANIMATING).orElse(false)) {
            return AnimationType.LIGHT_ATTACK; // default
        }
        int typeOrdinal = tag.getInt(ANIMATION_TYPE).orElse(0);
        return AnimationType.values()[Math.max(0, Math.min(typeOrdinal, AnimationType.values().length - 1))];
    }

    public static int getTicksRemaining(Player player) {
        CompoundTag tag = root(player);
        return tag.getBoolean(IS_ANIMATING).orElse(false) ? tag.getInt(TICKS_REMAINING).orElse(0) : 0;
    }

    public static int getTotalTicks(Player player) {
        CompoundTag tag = root(player);
        return tag.getBoolean(IS_ANIMATING).orElse(false) ? tag.getInt(TOTAL_TICKS).orElse(0) : 0;
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
