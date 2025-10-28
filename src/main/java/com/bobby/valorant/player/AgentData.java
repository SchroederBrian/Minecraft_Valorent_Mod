package com.bobby.valorant.player;

import com.bobby.valorant.Config;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class AgentData {
    private static final String ROOT = "ValorantAgent";
    private static final String SELECTED_AGENT = "SelectedAgent";
    
    private AgentData() {}
    
    public static Agent getSelectedAgent(Player player) {
        CompoundTag tag = root(player);
        String defaultAgentId = Config.COMMON.defaultAgent.get();
        String agentId = tag.getStringOr(SELECTED_AGENT, defaultAgentId);
        return Agent.byId(agentId);
    }
    
    public static void setSelectedAgent(Player player, Agent agent) {
        root(player).putString(SELECTED_AGENT, agent.getId());
    }
    
    public static boolean ensureInitialized(Player player) {
        CompoundTag tag = root(player);
        if (!tag.contains(SELECTED_AGENT)) {
            String defaultAgentId = Config.COMMON.defaultAgent.get();
            tag.putString(SELECTED_AGENT, defaultAgentId);
            return true;
        }
        return false;
    }
    
    public static void copy(Player original, Player clone) {
        CompoundTag originalRoot = root(original).copy();
        clone.getPersistentData().put(ROOT, originalRoot);
    }
    
    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        return persistent.getCompound(ROOT).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            persistent.put(ROOT, created);
            return created;
        });
    }
}