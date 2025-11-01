package com.bobby.valorant.client.lock;

import com.bobby.valorant.world.agent.Agent;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.Collections;
import java.util.HashMap;

public final class PlayerAgentState {
    private static volatile Map<UUID, Agent> PLAYER_AGENTS = Collections.emptyMap();
    private static final Object MUTEX = new Object();

    private PlayerAgentState() {}

    public static void update(Map<UUID, String> agentMap) {
        if (agentMap == null) {
            return;
        }
        Map<UUID, Agent> snapshot = new HashMap<>(agentMap.size());
        for (Map.Entry<UUID, String> entry : agentMap.entrySet()) {
            snapshot.put(entry.getKey(), Agent.byId(entry.getValue()));
        }
        Map<UUID, Agent> immutable = Collections.unmodifiableMap(snapshot);
        synchronized (MUTEX) {
            PLAYER_AGENTS = immutable; // atomic snapshot swap
        }
    }

    public static void put(UUID uuid, String agentId) {
        if (uuid == null || agentId == null) return;
        synchronized (MUTEX) {
            Map<UUID, Agent> current = PLAYER_AGENTS;
            Map<UUID, Agent> copy = new HashMap<>(current);
            copy.put(uuid, Agent.byId(agentId));
            PLAYER_AGENTS = Collections.unmodifiableMap(copy);
        }
    }

    public static Agent getAgentForPlayer(Player player) {
        Map<UUID, Agent> snapshot = PLAYER_AGENTS; // volatile read
        return snapshot.getOrDefault(player.getUUID(), Agent.UNSELECTED);
    }
}
