package com.bobby.valorant.skysmoke;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import net.minecraft.core.BlockPos;

public record SkySmokeArea(
    String id,
    Type type,
    Mode mode,
    int y,
    boolean enabled,
    List<BlockPos> vertices
) {
    public enum Type {
        ALLOWED, BLOCKED
    }

    public enum Mode {
        GROUND, FIXED_Y
    }

    public boolean isInside(double x, double z) {
        return isPointInPolygon(x, z, vertices);
    }

    private static boolean isPointInPolygon(double x, double z, List<BlockPos> vertices) {
        boolean inside = false;
        int n = vertices.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = vertices.get(i).getX();
            double zi = vertices.get(i).getZ();
            double xj = vertices.get(j).getX();
            double zj = vertices.get(j).getZ();

            boolean intersect = ((zi > z) != (zj > z)) &&
                    (x < (xj - xi) * (z - zi) / (zj - zi + 1e-9) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }

    public record SkySmokeDimensionAreas(
        Map<String, SkySmokeArea> allowed,
        Map<String, SkySmokeArea> blocked
    ) {
        public boolean hasAllowed() {
            return !allowed.isEmpty();
        }

        public boolean hasBlocked() {
            return !blocked.isEmpty();
        }

        public boolean isInsideAnyAllowed(double x, double z) {
            return allowed.values().stream()
                    .filter(SkySmokeArea::enabled)
                    .anyMatch(area -> area.isInside(x, z));
        }

        public boolean isInsideAnyBlocked(double x, double z) {
            return blocked.values().stream()
                    .filter(SkySmokeArea::enabled)
                    .anyMatch(area -> area.isInside(x, z));
        }

        public OptionalDouble fixedYAt(double x, double z) {
            // Return the Y value if we're inside exactly one FIXED_Y allowed area
            return allowed.values().stream()
                    .filter(area -> area.enabled() && area.mode() == Mode.FIXED_Y && area.isInside(x, z))
                    .mapToDouble(SkySmokeArea::y)
                    .findFirst();
        }

        public Optional<SkySmokeArea> findContainingAllowedArea(double x, double z) {
            return allowed.values().stream()
                    .filter(SkySmokeArea::enabled)
                    .filter(area -> area.isInside(x, z))
                    .findFirst();
        }
    }
}
