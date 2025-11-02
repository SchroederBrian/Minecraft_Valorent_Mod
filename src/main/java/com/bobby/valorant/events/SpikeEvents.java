package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.registry.ModItems;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class SpikeEvents {
    private SpikeEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        var server = sp.getServer();
        if (server == null) return;
        var sb = server.getScoreboard();
        var team = sb.getPlayersTeam(sp.getScoreboardName());
        boolean isAttackerTeam = team != null && "A".equals(team.getName());

        // Server-side movement lock while planting or defusing (camera free)
        boolean planting = com.bobby.valorant.spike.SpikePlantingHandler.isPlanting(sp);
        boolean defusing = com.bobby.valorant.spike.SpikeDefusingHandler.isDefusing(sp);
        if (planting && com.bobby.valorant.Config.COMMON.lockMovementWhilePlanting.get()) {
            var start = com.bobby.valorant.spike.SpikePlantingHandler.getPlantStartPos(sp);
            if (start != null) {
                sp.setPos(start.x, sp.getY(), start.z);
            }
            var vel = sp.getDeltaMovement();
            sp.setDeltaMovement(0.0D, vel.y, 0.0D);
            sp.setSprinting(false);
        } else if (defusing && com.bobby.valorant.Config.COMMON.lockMovementWhileDefusing.get()) {
            var start = com.bobby.valorant.spike.SpikeDefusingHandler.getDefuseStartPos(sp);
            if (start != null) {
                sp.setPos(start.x, sp.getY(), start.z);
            }
            var vel = sp.getDeltaMovement();
            sp.setDeltaMovement(0.0D, vel.y, 0.0D);
            sp.setSprinting(false);
        }

        int size = sp.getInventory().getContainerSize();
        if (isAttackerTeam) {
            // Attackers must not have Defuser -> remove
            for (int i = 0; i < size; i++) {
                ItemStack s = sp.getInventory().getItem(i);
                if (s.is(ModItems.DEFUSER.get())) {
                    sp.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
            return;
        }

        // Defenders should not have defusers unless actively defusing
        if (!com.bobby.valorant.spike.SpikeDefusingHandler.isDefusing(sp)) {
            for (int i = 0; i < size; i++) {
                ItemStack s = sp.getInventory().getItem(i);
                if (s.is(ModItems.DEFUSER.get())) {
                    sp.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }
}


