package com.bobby.valorant.events;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import com.bobby.valorant.spawn.client.SpawnAreaClientState;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class SpawnAreaClientRenderer {
    private SpawnAreaClientRenderer() {}

    private static long tickCounter = 0L;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        int interval = Config.COMMON.spawnAreaParticleTickInterval.get();
        if ((tickCounter++ % Math.max(1, interval)) != 0) return;

        ResourceLocation currentDim = mc.level.dimension().location();
        if (SpawnAreaClientState.dimension == null || !SpawnAreaClientState.dimension.equals(currentDim)) return;
        String spawnType = Config.COMMON.spawnAreaParticleType.get();
        int spawnColor = Config.COMMON.spawnAreaParticleColor.get();
        ParticleOptions particle = buildParticle(spawnType, spawnColor);
        double spacing = Config.COMMON.spawnAreaParticleSpacing.get();

        System.out.println("[SpawnRenderer] Spawn particle type: " + spawnType + ", color: " + Integer.toHexString(spawnColor) + ", built: " + particle.getClass().getSimpleName());

        renderPerimeter(mc, particle, spacing, SpawnAreaClientState.vertsA, SpawnAreaClientState.yA, 0xFF00FF);
        renderPerimeter(mc, particle, spacing, SpawnAreaClientState.vertsV, SpawnAreaClientState.yV, 0x00FFFF);

        // Bomb sites
        String siteType = Config.COMMON.bombSiteParticleType.get();
        int siteColor = Config.COMMON.bombSiteParticleColor.get();
        ParticleOptions siteParticle = buildParticle(siteType, siteColor);
        System.out.println("[SpawnRenderer] Bomb site particle type: " + siteType + ", color: " + Integer.toHexString(siteColor) + ", built: " + siteParticle.getClass().getSimpleName());

        renderPerimeter(mc, siteParticle, spacing, SpawnAreaClientState.vertsSiteA, SpawnAreaClientState.ySiteA, 0xFF0000);
        renderPerimeter(mc, siteParticle, spacing, SpawnAreaClientState.vertsSiteB, SpawnAreaClientState.ySiteB, 0x00FF00);
        renderPerimeter(mc, siteParticle, spacing, SpawnAreaClientState.vertsSiteC, SpawnAreaClientState.ySiteC, 0x0000FF);
    }

    private static void renderPerimeter(Minecraft mc, ParticleOptions particle, double spacing, java.util.List<BlockPos> verts, int y, int color) {
        if (verts == null || verts.size() < 2) return;
        for (int i = 0, n = verts.size(); i < n; i++) {
            BlockPos a = verts.get(i);
            BlockPos b = verts.get((i + 1) % n);
            double ax = a.getX() + 0.5D, az = a.getZ() + 0.5D;
            double bx = b.getX() + 0.5D, bz = b.getZ() + 0.5D;
            double dx = bx - ax, dz = bz - az;
            double len = Math.sqrt(dx * dx + dz * dz);
            int steps = Math.max(1, (int) Math.floor(len / spacing));
            double stepx = dx / steps;
            double stepz = dz / steps;
            for (int s = 0; s <= steps; s++) {
                double x = ax + stepx * s;
                double z = az + stepz * s;
                mc.level.addParticle(particle, x, y + 0.01D, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static ParticleOptions buildParticle(String id, int rgb) {
        String key = id == null ? "" : id.trim().toUpperCase();
        switch (key) {
            case "FLAME": return ParticleTypes.FLAME;
            case "GLOW": return ParticleTypes.GLOW;
            case "ASH": return ParticleTypes.ASH;
            case "HAPPY_VILLAGER": return ParticleTypes.HAPPY_VILLAGER;
            case "CLOUD": return ParticleTypes.CLOUD;
            case "DUST": {
                return new DustParticleOptions(rgb, 1.0f);
            }
            default: return ParticleTypes.GLOW;
        }
    }
}


