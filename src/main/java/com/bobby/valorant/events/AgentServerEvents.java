package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.SyncAgentLocksPacket;
import com.bobby.valorant.fancymenu.FancyMenuVarSync;
import com.bobby.valorant.network.SyncAgentS2CPacket;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.player.AbilityStateData;
import com.bobby.valorant.round.AgentLockManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Valorant.MODID)
public final class AgentServerEvents {
    private AgentServerEvents() {}

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            AgentLockManager.get(sp.getServer()).onTeamChanged(sp);
            // Broadcast updated locks to all
            broadcastLocks(sp);
            FancyMenuVarSync.onPlayerLeave(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            // Ensure AgentData exists for this player
            AgentData.ensureInitialized(sp);
            // Initialize ability state defaults for the player's agent and sync
            com.bobby.valorant.world.agent.Agent selectedAgent = AgentData.getSelectedAgent(sp);
            AbilityStateData.initForAgent(sp, selectedAgent);
            var set = com.bobby.valorant.ability.Abilities.getForAgent(selectedAgent);
            int c = AbilityStateData.getCharges(sp, set.c());
            int q = AbilityStateData.getCharges(sp, set.q());
            int e = AbilityStateData.getCharges(sp, set.e());
            int x = AbilityStateData.getUltPoints(sp);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new com.bobby.valorant.network.SyncAbilityStateS2CPacket(c, q, e, x));

            // Send current locks to the joining player
            var manager = AgentLockManager.get(sp.getServer());
            Map<UUID, String> playerAgentMap = new HashMap<>();
            manager.getPlayerToLocked().forEach((uuid, agent) -> playerAgentMap.put(uuid, agent.getId()));
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new SyncAgentLocksPacket(playerAgentMap));

            // Send all existing players' selected agents to the joining player
            var server = sp.getServer();
            if (server != null) {
                server.getPlayerList().getPlayers().forEach(other -> {
                    String agentId = AgentData.getSelectedAgent(other).getId();
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new SyncAgentS2CPacket(other.getUUID(), agentId));
                });
                // Broadcast the joining player's selection to everyone else
                String joinAgentId = AgentData.getSelectedAgent(sp).getId();
                SyncAgentS2CPacket joinSync = new SyncAgentS2CPacket(sp.getUUID(), joinAgentId);
                server.getPlayerList().getPlayers().forEach(p -> {
                    if (p == sp) return;
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, joinSync);
                });
            }
            FancyMenuVarSync.updateAll(sp.getServer());
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer tracker)) return;
        if (!(event.getTarget() instanceof ServerPlayer tracked)) return;
        String agentId = AgentData.getSelectedAgent(tracked).getId();
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(tracker, new SyncAgentS2CPacket(tracked.getUUID(), agentId));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        AgentData.copy(event.getOriginal(), event.getEntity());
        AbilityStateData.copy(event.getOriginal(), event.getEntity());
    }

    private static void broadcastLocks(ServerPlayer anyPlayer) {
        var server = anyPlayer.getServer();
        if (server == null) return;
        var manager = AgentLockManager.get(server);
        Map<UUID, String> playerAgentMap = new HashMap<>();
        manager.getPlayerToLocked().forEach((uuid, agent) -> playerAgentMap.put(uuid, agent.getId()));
        SyncAgentLocksPacket sync = new SyncAgentLocksPacket(playerAgentMap);
        server.getPlayerList().getPlayers().forEach(p ->
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, sync)
        );
    }
}


