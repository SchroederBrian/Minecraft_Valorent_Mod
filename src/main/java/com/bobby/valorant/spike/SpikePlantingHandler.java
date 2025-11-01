package com.bobby.valorant.spike;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.round.RoundController;
import com.bobby.valorant.server.TitleMessages;
import com.bobby.valorant.player.AbilityEquipData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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
            com.bobby.valorant.util.SoundManager.playSpikePlantSound(level, sp.getX(), sp.getY(), sp.getZ());
            broadcastPlantedTitle(level);
            com.bobby.valorant.Config.COMMON.spikePlanted.set(true);
            RoundController.get(level).plantSpike();
            switchToWeapon(sp);
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

    public static void spawnPlanted(ServerLevel level, Vec3 pos) {
        // Use an invisible, invulnerable ArmorStand as a stationary holder for the spike item
        double yOffset = com.bobby.valorant.Config.COMMON.plantedSpikeYOffset.get();
        net.minecraft.world.entity.decoration.ArmorStand stand = new net.minecraft.world.entity.decoration.ArmorStand(level, pos.x, pos.y + yOffset, pos.z);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setNoGravity(true);
        stand.setSilent(true);
        // Keep it stationary and unobtrusive
        // Cosmetic: no baseplate/arms, keep default pose
        stand.setShowArms(false);
        stand.setNoBasePlate(true);
        // Display the planted spike item on its head
        stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, ModItems.PLANTEDSPIKE.get().getDefaultInstance());
        level.addFreshEntity(stand);
    }

    private static void broadcastPlantedTitle(ServerLevel level) {
        // Send title overlay to all players
        TitleMessages.show("SPIKE PLANTED", "Hold site until detonation", 0xFFFFD700, 0xFFFFFF00, 10, 1000, 10);

        // Server console/log line
        Valorant.LOGGER.info("Spike planted");
    }

    private static void switchToWeapon(ServerPlayer sp) {
        var inv = sp.getInventory();
        try {
            var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            int selectedSlot = field.getInt(inv);
            ItemStack heldStack = inv.getItem(selectedSlot);
            Valorant.LOGGER.info("[SPIKE PLANTING] Switching to weapon: slot={}, held={} (empty={})",
                    selectedSlot, heldStack.getItem(), heldStack.isEmpty());
            if (heldStack.isEmpty() || heldStack.is(ModItems.SPIKE.get())) {
                ItemStack restore = AbilityEquipData.takeSaved(sp);
                inv.setItem(selectedSlot, restore);
                inv.setChanged();
                sp.containerMenu.broadcastChanges();
                ItemStack now = inv.getItem(selectedSlot);
                Valorant.LOGGER.info("[SPIKE PLANTING] After switching to weapon now holding: {} (empty={})",
                        now.getItem(), now.isEmpty());
            }
        } catch (Exception e) {
            Valorant.LOGGER.error("[SPIKE PLANTING] Failed to switch to weapon: {}", e.toString());
        }
    }
}


