package com.bobby.valorant.client;

import java.util.List;
import java.util.Map;

import com.bobby.valorant.Config;
import com.bobby.valorant.skysmoke.SkySmokeArea;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;

public final class SkySmokeAreaClientRenderer {
    private SkySmokeAreaClientRenderer() {}

    private static int tickCounter = 0;

    public static void render(ClientLevel level, float partialTick) {
        tickCounter++;
        int tickInterval = Config.COMMON.skySmokeAreasParticleTickInterval.get();
        if (tickCounter % tickInterval != 0) return;

        // Render completed zones
        var areas = SkySmokeAreaClient.getAreasForDimension(level.dimension().location());
        if (areas != null) {
            // Render allowed zones
            renderZoneType(level, areas.allowed(), true);

            // Render blocked zones
            renderZoneType(level, areas.blocked(), false);
        }

        // Render recording points if enabled
        if (Config.COMMON.skySmokeAreasShowRecordingParticles.get()) {
            renderRecordingPoints(level);
        }
    }

    private static void renderZoneType(ClientLevel level, Map<String, SkySmokeArea> zones, boolean isAllowed) {
        int particleColor = isAllowed ?
            Config.COMMON.skySmokeAreasAllowedParticleColor.get() :
            Config.COMMON.skySmokeAreasBlockedParticleColor.get();

        double spacing = Config.COMMON.skySmokeAreasParticleSpacing.get();

        for (SkySmokeArea zone : zones.values()) {
            if (!zone.enabled()) continue;
            renderZone(level, zone, particleColor, spacing);
        }
    }

    private static void renderZone(ClientLevel level, SkySmokeArea zone, int particleColor, double spacing) {
        List<BlockPos> vertices = zone.vertices();
        if (vertices.size() < 3) return;

        // Sample points along the perimeter
        double totalLength = calculatePerimeterLength(vertices);
        int numSamples = Math.max(4, (int) Math.ceil(totalLength / spacing));

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / numSamples;
            BlockPos pos = interpolateAlongPerimeter(vertices, t);

            // Use exact interpolated Y coordinate from saved vertices
            double y = pos.getY() + 0.1;

            // Spawn particle at exact saved coordinates
            spawnColoredParticle(level, pos.getX() + 0.5, y, pos.getZ() + 0.5, particleColor);
        }
    }

    private static double calculatePerimeterLength(List<BlockPos> vertices) {
        double length = 0;
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            BlockPos a = vertices.get(i);
            BlockPos b = vertices.get((i + 1) % n);
            length += Math.sqrt((a.getX() - b.getX()) * (a.getX() - b.getX()) +
                               (a.getZ() - b.getZ()) * (a.getZ() - b.getZ()));
        }
        return length;
    }

    private static BlockPos interpolateAlongPerimeter(List<BlockPos> vertices, double t) {
        int n = vertices.size();
        double targetLength = t * calculatePerimeterLength(vertices);

        double currentLength = 0;
        for (int i = 0; i < n; i++) {
            BlockPos a = vertices.get(i);
            BlockPos b = vertices.get((i + 1) % n);

            double segmentLength = Math.sqrt((a.getX() - b.getX()) * (a.getX() - b.getX()) +
                                           (a.getZ() - b.getZ()) * (a.getZ() - b.getZ()));

            if (currentLength + segmentLength >= targetLength) {
                double segmentT = (targetLength - currentLength) / segmentLength;
                int x = Mth.floor(a.getX() + segmentT * (b.getX() - a.getX()));
                int y = Mth.floor(a.getY() + segmentT * (b.getY() - a.getY()));
                int z = Mth.floor(a.getZ() + segmentT * (b.getZ() - a.getZ()));
                return new BlockPos(x, y, z);
            }

            currentLength += segmentLength;
        }

        return vertices.get(0); // Fallback
    }

    private static void spawnColoredParticle(ClientLevel level, double x, double y, double z, int color) {
        // Extract RGB components from the color int
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Convert to 0-1 range
        float red = r / 255.0f;
        float green = g / 255.0f;
        float blue = b / 255.0f;

        // Use DUST particle with custom color
        var particle = new net.minecraft.core.particles.DustParticleOptions(color, 1.0f);

        level.addParticle(particle, x, y, z, 0, 0, 0);
    }

    private static void renderRecordingPoints(ClientLevel level) {
        // Get the current player's recording data (if any)
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        var recordingData = SkySmokeRecordingClient.getRecordingData(mc.player.getUUID());
        if (recordingData == null || recordingData.points().isEmpty()) return;

        int recordingColor = Config.COMMON.skySmokeAreasRecordingParticleColor.get();

        for (BlockPos point : recordingData.points()) {
            // Spawn recording particles at each point
            double x = point.getX() + 0.5;
            double y = point.getY() + 0.5;
            double z = point.getZ() + 0.5;

            spawnColoredParticle(level, x, y, z, recordingColor);
        }
    }
}
