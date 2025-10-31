package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.SyncAgentLocksPacket;
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
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            // Send current locks to the joining player
            var manager = AgentLockManager.get(sp.getServer());
            Map<UUID, String> playerAgentMap = new HashMap<>();
            manager.getPlayerToLocked().forEach((uuid, agent) -> playerAgentMap.put(uuid, agent.getId()));
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new SyncAgentLocksPacket(playerAgentMap));
        }
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


