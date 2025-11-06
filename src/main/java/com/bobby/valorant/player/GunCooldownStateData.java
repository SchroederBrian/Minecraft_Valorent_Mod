package com.bobby.valorant.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class GunCooldownStateData {
    private static final String ROOT = "ValorantGunCooldownState";
    private static final String LAST_SHOT_TIME = "LastShotTime";
    private static final String COOLDOWN_TICKS = "CooldownTicks";

    private GunCooldownStateData() {}

    public static boolean isOnCooldown(Player player) {
        CompoundTag tag = root(player);
        long lastShotTime = tag.getLong(LAST_SHOT_TIME).orElse(0L);
        int cooldownTicks = tag.getInt(COOLDOWN_TICKS).orElse(0);

        if (cooldownTicks <= 0) return false;

        long currentTime = player.level().getGameTime();
        return (currentTime - lastShotTime) < cooldownTicks;
    }

    public static void startCooldown(Player player, int cooldownTicks) {
        CompoundTag tag = root(player);
        tag.putLong(LAST_SHOT_TIME, player.level().getGameTime());
        tag.putInt(COOLDOWN_TICKS, cooldownTicks);
    }

    public static int getRemainingCooldownTicks(Player player) {
        CompoundTag tag = root(player);
        long lastShotTime = tag.getLong(LAST_SHOT_TIME).orElse(0L);
        int cooldownTicks = tag.getInt(COOLDOWN_TICKS).orElse(0);

        if (cooldownTicks <= 0) return 0;

        long currentTime = player.level().getGameTime();
        long elapsed = currentTime - lastShotTime;

        if (elapsed >= cooldownTicks) return 0;
        return (int) (cooldownTicks - elapsed);
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
