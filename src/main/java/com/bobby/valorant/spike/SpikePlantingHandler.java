package com.bobby.valorant.spike;

import com.bobby.valorant.registry.ModEntityTypes;
import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.round.RoundController;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side planting progress tracking. 4 seconds (80 ticks) hold to plant.
 */
public final class SpikePlantingHandler {
    private SpikePlantingHandler() {}

    private static final int PLANT_TICKS = 80;
    private static final Map<UUID, Integer> plantingTicks = new HashMap<>();

    public static void startPlanting(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        if (RoundController.get(level).phase() != RoundController.Phase.ROUND) return;
        var server = player.getServer(); if (server == null) return;
        var team = server.getScoreboard().getPlayersTeam(player.getScoreboardName());
        if (team == null || !"A".equals(team.getName())) return; // MVP: attackers = A
        plantingTicks.put(player.getUUID(), PLANT_TICKS);
    }

    public static void cancelPlanting(ServerPlayer player) {
        plantingTicks.remove(player.getUUID());
    }

    public static boolean isPlanting(ServerPlayer player) {
        return plantingTicks.containsKey(player.getUUID());
    }

    public static void tick(ServerLevel level) {
        if (plantingTicks.isEmpty()) return;
        plantingTicks.replaceAll((id, remaining) -> Math.max(0, remaining - 1));
        plantingTicks.entrySet().removeIf(entry -> {
            UUID id = entry.getKey();
            int remaining = entry.getValue();
            if (remaining > 0) return false;
            ServerPlayer sp = level.getServer().getPlayerList().getPlayer(id);
            if (sp == null) return true;
            if (!consumeSpike(sp)) return true;
            spawnPlanted(level, sp.position());
            RoundController.get(level).plantSpike();
            return true;
        });
    }

    private static boolean consumeSpike(ServerPlayer sp) {
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.is(ModItems.SPIKE.get())) {
                sp.getInventory().setItem(i, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    private static void spawnPlanted(ServerLevel level, Vec3 pos) {
        var planted = ModEntityTypes.PLANTED_SPIKE.get().create(level, EntitySpawnReason.TRIGGERED);
        if (planted == null) return;
        planted.setPos(pos.x, pos.y, pos.z);
        level.addFreshEntity(planted);
    }
}


