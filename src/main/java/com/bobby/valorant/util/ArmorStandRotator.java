package com.bobby.valorant.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.bobby.valorant.Valorant;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class ArmorStandRotator {
    private ArmorStandRotator() {}

    private static final CopyOnWriteArrayList<RotationTask> ROTATION_TASKS = new CopyOnWriteArrayList<>();

    // Track which players are currently in range of each armor stand to avoid spamming blindness
    private static final Map<ArmorStand, Map<Player, Boolean>> playersInRange = new WeakHashMap<>();

    public static void addRotatingArmorStand(ArmorStand armorStand, int durationTicks) {
        if (armorStand == null || durationTicks <= 0) return;
        ROTATION_TASKS.add(new RotationTask(armorStand, durationTicks, 0, 0));
    }

    public static void addRotatingArmorStandWithBlindness(ArmorStand armorStand, int durationTicks, float radius, int blindnessTicks) {
        if (armorStand == null || durationTicks <= 0) return;
        ROTATION_TASKS.add(new RotationTask(armorStand, durationTicks, radius, blindnessTicks));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ROTATION_TASKS.isEmpty()) return;

        List<RotationTask> toRemove = new ArrayList<>();
        for (RotationTask task : ROTATION_TASKS) {
            if (task.armorStand.isAlive()) {
                // Rotate 3.6 degrees per tick (full rotation every ~100 ticks)
                float rotation = -(task.ticksElapsed * 3.6f) % 360.0f;
                task.armorStand.setYRot(rotation);
                task.armorStand.setYHeadRot(rotation);

                // Apply blindness effect to players in range
                if (task.blindnessTicks > 0 && task.radius > 0) {
                    applyBlindnessToNearbyPlayers(task.armorStand, task.radius, task.blindnessTicks);
                } else if (task.ticksElapsed == 0) {
                    Valorant.LOGGER.debug(
                        "[SkySmoke] Blindness disabled for this task (radius={}, ticks={})",
                        task.radius, task.blindnessTicks
                    );
                }

                task.ticksElapsed++;
                task.remainingTicks--;

                if (task.remainingTicks <= 0) {
                    task.armorStand.discard();
                    toRemove.add(task);
                }
            } else {
                toRemove.add(task);
            }
        }
        if (!toRemove.isEmpty()) ROTATION_TASKS.removeAll(toRemove);
    }

    private static void applyBlindnessToNearbyPlayers(ArmorStand armorStand, float radius, int blindnessTicks) {
        var level = armorStand.level();
        var smokePos = armorStand.position();

        Valorant.LOGGER.debug("[SkySmoke] Checking players near smoke at ({},{},{}) with radius {}",
                smokePos.x(), smokePos.y(), smokePos.z(), radius);

        // Get or create tracking map for this armor stand
        Map<Player, Boolean> standPlayersInRange = playersInRange.computeIfAbsent(armorStand, k -> new WeakHashMap<>());

        double radiusSq = radius * radius;

        for (var player : level.players()) {
            if (player instanceof Player serverPlayer) {
                double dx = smokePos.x() - serverPlayer.getX();
                double dz = smokePos.z() - serverPlayer.getZ();
                double horizontalDistSq = dx * dx + dz * dz;
                double horizontalDist = Math.sqrt(horizontalDistSq);

                boolean wasInRange = standPlayersInRange.getOrDefault(serverPlayer, false);
                boolean isInRange = horizontalDistSq <= radiusSq;

                Valorant.LOGGER.debug("[SkySmoke] Player {} at ({},{},{}) - horizontal distance: {}, in range: {} (was: {})",
                    serverPlayer.getName().getString(),
                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    String.format("%.2f", horizontalDist),
                    isInRange, wasInRange);

                // Apply blindness when entering range, remove when leaving
                if (isInRange && !wasInRange) {
                    Valorant.LOGGER.info("[SkySmoke] Applying {} ticks of blindness to player {} (entering smoke)",
                        blindnessTicks, serverPlayer.getName().getString());
                    serverPlayer.addEffect(new MobEffectInstance(
                        MobEffects.BLINDNESS, blindnessTicks, 0, false, false, true
                    ));
                } else if (!isInRange && wasInRange) {
                    // Remove blindness when leaving range
                    Valorant.LOGGER.info("[SkySmoke] Removing blindness from player {} (leaving smoke)",
                        serverPlayer.getName().getString());
                    serverPlayer.removeEffect(MobEffects.BLINDNESS);
                }

                // Update tracking state
                standPlayersInRange.put(serverPlayer, isInRange);
            }
        }

        // Clean up tracking map for removed players (optional optimization)
        standPlayersInRange.entrySet().removeIf(entry -> !level.players().contains(entry.getKey()));
    }

    private static final class RotationTask {
        final ArmorStand armorStand;
        int remainingTicks;
        int ticksElapsed;
        final float radius;
        final int blindnessTicks;

        RotationTask(ArmorStand armorStand, int durationTicks, float radius, int blindnessTicks) {
            this.armorStand = armorStand;
            this.remainingTicks = durationTicks;
            this.ticksElapsed = 0;
            this.radius = radius;
            this.blindnessTicks = blindnessTicks;
        }
    }
}
