package com.bobby.valorant.spike;

import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.round.RoundController;
import com.bobby.valorant.server.TitleMessages;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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
    private static final Map<UUID, Integer> defuseCooldown = new HashMap<>(); // ignore START while > 0

    public static void startDefuse(ServerPlayer player) {
        // Ignore if in short cooldown window after a cancel
        Integer cd = defuseCooldown.get(player.getUUID());
        if (cd != null && cd > 0) {
            return;
        }
        // Ignore if already defusing
        if (isDefusing(player)) {
            return;
        }
        // Check if player is on defender team (V)
        var server = player.getServer();
        if (server == null) return;
        var team = server.getScoreboard().getPlayersTeam(player.getScoreboardName());
        if (team == null || !"V".equals(team.getName())) return;

        // Check if spike is already defused
        var roundController = RoundController.get((ServerLevel) player.level());
        if (roundController.isSpikeDefused()) {
            System.out.println("[SpikeDefusing] Spike already defused, cannot start defusing again");
            return; // Spike already defused, cannot defuse again
        }

        defuseTicks.put(player.getUUID(), DEFUSE_TICKS);
        // Give defuser in hotbar slot 4 (index 3)
        player.getInventory().setItem(3, ModItems.DEFUSER.get().getDefaultInstance());
    }

    public static void cancelDefuse(ServerPlayer player) {
        defuseTicks.remove(player.getUUID());
        defuseCooldown.put(player.getUUID(), 12); // small cooldown to prevent immediate re-start from queued inputs
        // Remove defuser when canceling defuse
        removeDefuser(player);
    }

    public static boolean isDefusing(ServerPlayer player) {
        return defuseTicks.containsKey(player.getUUID());
    }

    public static void tick(ServerLevel level) {
        // Tick cooldowns
        if (!defuseCooldown.isEmpty()) {
            defuseCooldown.replaceAll((id, remaining) -> Math.max(0, remaining - 1));
            defuseCooldown.entrySet().removeIf(e -> e.getValue() <= 0);
        }
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
            if (planted == null) {
                System.out.println("[SpikeDefusing] No planted spike found near player " + sp.getName().getString());
                return true;
            }
            System.out.println("[SpikeDefusing] Found planted spike, discarding it and calling defuseSpikeFull");
            planted.discard();
            RoundController.get(level).defuseSpikeFull();

            // Show defuse success title
            TitleMessages.broadcast(level, "SPIKE DEFUSED", "Defenders win the round");

            // Remove defuser from player
            removeDefuser(sp);
            return true;
        });
    }

    private static Entity findNearestPlanted(ServerLevel level, Vec3 pos, double radius) {
        AABB box = new AABB(pos.x - radius, pos.y - radius, pos.z - radius, pos.x + radius, pos.y + radius, pos.z + radius);
        // Check for ArmorStand with planted spike item in head slot
        for (Entity e : level.getEntities(null, box)) {
            if (e instanceof net.minecraft.world.entity.decoration.ArmorStand stand &&
                stand.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(ModItems.PLANTEDSPIKE.get())) {
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


