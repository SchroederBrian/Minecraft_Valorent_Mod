package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.skysmoke.SkySmokeManager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Valorant.MODID)
public final class SkySmokeServerEvents {
    private SkySmokeServerEvents() {}

    // Track which players are currently in smoke to avoid spamming the effect
    private static final Map<ServerPlayer, Boolean> playerInSmoke = new WeakHashMap<>();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        SkySmokeManager.load(server);
        com.bobby.valorant.spawn.SkySmokeCalibration.loadTransforms(server);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        SkySmokeManager.syncFor(sp);
        com.bobby.valorant.spawn.SkySmokeCalibration.syncTransformsFor(sp);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        SkySmokeManager.syncFor(sp);
        com.bobby.valorant.spawn.SkySmokeCalibration.syncTransformsFor(sp);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        if (!com.bobby.valorant.Config.COMMON.skySmokeApplyBlindness.get()) return;

        double radius = com.bobby.valorant.Config.COMMON.skySmokeRadius.get();
        int blindTicks = com.bobby.valorant.Config.COMMON.skySmokeBlindnessTicks.get();
        if (radius <= 0 || blindTicks <= 0) return;

        var level = (net.minecraft.server.level.ServerLevel) sp.level();

        // Search for smoke stands with generous vertical range (Y-agnostic)
        double vRange = 256.0; // large enough to find stands that are deep/high
        AABB box = new AABB(
            sp.getX() - radius, sp.getY() - vRange, sp.getZ() - radius,
            sp.getX() + radius, sp.getY() + vRange, sp.getZ() + radius
        );

        java.util.List<ArmorStand> nearby =
            level.getEntitiesOfClass(ArmorStand.class, box, as -> {
                var head = as.getItemBySlot(EquipmentSlot.HEAD);
                return !head.isEmpty() && head.is(com.bobby.valorant.registry.ModItems.HAVEN_MAP.get());
            });

        boolean wasInSmoke = playerInSmoke.getOrDefault(sp, false);
        boolean isInSmoke = false;

        if (!nearby.isEmpty()) {
            double radiusSq = radius * radius;
            for (var stand : nearby) {
                double dx = stand.getX() - sp.getX();
                double dz = stand.getZ() - sp.getZ();
                double horizontalDistSq = dx * dx + dz * dz;
                if (horizontalDistSq <= radiusSq) {
                    isInSmoke = true;
                    break;
                }
            }
        }

        // Apply blindness when entering smoke, remove when leaving
        if (isInSmoke && !wasInSmoke) {
            sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.BLINDNESS,
                blindTicks,
                0,
                false,
                false,
                true
            ));
        } else if (!isInSmoke && wasInSmoke) {
            // Remove blindness when leaving smoke
            sp.removeEffect(net.minecraft.world.effect.MobEffects.BLINDNESS);
        }

        // Update tracking state
        playerInSmoke.put(sp, isInSmoke);
    }
}
