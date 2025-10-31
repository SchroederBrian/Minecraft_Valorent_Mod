package com.bobby.valorant.client.lock;

import com.bobby.valorant.world.agent.Agent;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerAgentState {
    private static final Map<UUID, Agent> PLAYER_AGENTS = new ConcurrentHashMap<>();

    private PlayerAgentState() {}

    public static synchronized void update(Map<UUID, String> agentMap) {
        PLAYER_AGENTS.clear();
        for (Map.Entry<UUID, String> entry : agentMap.entrySet()) {
            PLAYER_AGENTS.put(entry.getKey(), Agent.byId(entry.getValue()));
        }
    }

    public static Agent getAgentForPlayer(Player player) {
        return PLAYER_AGENTS.getOrDefault(player.getUUID(), Agent.UNSELECTED);
    }
}
