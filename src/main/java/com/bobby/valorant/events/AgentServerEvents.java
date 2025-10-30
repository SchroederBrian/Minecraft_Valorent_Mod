package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.SyncAgentLocksPacket;
import com.bobby.valorant.round.AgentLockManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;

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
            var lockedA = new ArrayList<String>();
            var lockedV = new ArrayList<String>();
            manager.getLockedA().forEach(a -> lockedA.add(a.getId()));
            manager.getLockedV().forEach(a -> lockedV.add(a.getId()));
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new SyncAgentLocksPacket(lockedA, lockedV));
        }
    }

    private static void broadcastLocks(ServerPlayer anyPlayer) {
        var server = anyPlayer.getServer();
        var manager = AgentLockManager.get(server);
        var lockedA = new ArrayList<String>();
        var lockedV = new ArrayList<String>();
        manager.getLockedA().forEach(a -> lockedA.add(a.getId()));
        manager.getLockedV().forEach(a -> lockedV.add(a.getId()));
        SyncAgentLocksPacket sync = new SyncAgentLocksPacket(lockedA, lockedV);
        server.getPlayerList().getPlayers().forEach(p ->
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, sync)
        );
    }
}


