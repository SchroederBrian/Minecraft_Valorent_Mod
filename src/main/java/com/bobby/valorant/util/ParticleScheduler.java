package com.bobby.valorant.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.bobby.valorant.Valorant;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class ParticleScheduler {
    private ParticleScheduler() {}

    private static final CopyOnWriteArrayList<Task> TASKS = new CopyOnWriteArrayList<>();

    public static void spawnRepeating(ServerLevel level,
                                      ParticleOptions particle,
                                      double x, double y, double z,
                                      int count,
                                      double dx, double dy, double dz,
                                      double speed,
                                      int durationTicks) {
        if (level == null || particle == null || durationTicks <= 0) return;
        TASKS.add(new Task(level, particle, x, y, z, count, dx, dy, dz, speed, durationTicks));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (TASKS.isEmpty()) return;
        List<Task> toRemove = new ArrayList<>();
        for (Task t : TASKS) {
            t.level.sendParticles(t.particle, t.x, t.y, t.z, t.count, t.dx, t.dy, t.dz, t.speed);
            t.remaining--;
            if (t.remaining <= 0) toRemove.add(t);
        }
        if (!toRemove.isEmpty()) TASKS.removeAll(toRemove);
    }

    private static final class Task {
        final ServerLevel level;
        final ParticleOptions particle;
        final double x, y, z;
        final int count;
        final double dx, dy, dz;
        final double speed;
        int remaining;

        Task(ServerLevel level, ParticleOptions particle,
             double x, double y, double z,
             int count,
             double dx, double dy, double dz,
             double speed,
             int durationTicks) {
            this.level = level;
            this.particle = particle;
            this.x = x;
            this.y = y;
            this.z = z;
            this.count = Math.max(1, count);
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.speed = speed;
            this.remaining = durationTicks;
        }
    }
}


