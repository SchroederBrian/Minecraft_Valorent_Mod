package com.bobby.valorant.spike;

import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.round.RoundController;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side defusing progress tracking. 7 seconds (140 ticks) hold to defuse.
 */
public final class SpikeDefusingHandler {
    private SpikeDefusingHandler() {}

    private static final int DEFUSE_TICKS = 140;
    private static final double DEFUSE_RADIUS = 1.0D;
    private static final Map<UUID, Integer> defuseTicks = new HashMap<>();

    public static void startDefuse(ServerPlayer player) {
        defuseTicks.put(player.getUUID(), DEFUSE_TICKS);
        // Auto-equip defuser if present; otherwise give temp defuser visual (not persisted)
        if (!player.getInventory().contains(ModItems.DEFUSER.get().getDefaultInstance())) {
            player.getInventory().add(ModItems.DEFUSER.get().getDefaultInstance());
        }
    }

    public static void cancelDefuse(ServerPlayer player) {
        defuseTicks.remove(player.getUUID());
    }

    public static boolean isDefusing(ServerPlayer player) {
        return defuseTicks.containsKey(player.getUUID());
    }

    public static void tick(ServerLevel level) {
        if (defuseTicks.isEmpty()) return;
        defuseTicks.replaceAll((id, remaining) -> Math.max(0, remaining - 1));
        defuseTicks.entrySet().removeIf(entry -> {
            UUID id = entry.getKey();
            int remaining = entry.getValue();
            if (remaining > 0) return false;
            ServerPlayer sp = level.getServer().getPlayerList().getPlayer(id);
            if (sp == null) return true;
            // Require within radius of planted spike entity
            Entity planted = findNearestPlanted(level, sp.position(), DEFUSE_RADIUS);
            if (planted == null) return true;
            planted.discard();
            RoundController.get(level).defuseSpikeFull();
            // Optionally remove defuser from player
            removeDefuser(sp);
            return true;
        });
    }

    private static Entity findNearestPlanted(ServerLevel level, Vec3 pos, double radius) {
        AABB box = new AABB(pos.x - radius, pos.y - radius, pos.z - radius, pos.x + radius, pos.y + radius, pos.z + radius);
        for (Entity e : level.getEntities(null, box)) {
            if (e instanceof ThrowableItemProjectile tip && tip.getItem().is(ModItems.PLANTEDSPIKE.get())) {
                return e;
            }
        }
        return null;
    }

    private static void removeDefuser(ServerPlayer sp) {
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.is(ModItems.DEFUSER.get())) {
                sp.getInventory().setItem(i, ItemStack.EMPTY);
                return;
            }
        }
    }
}


