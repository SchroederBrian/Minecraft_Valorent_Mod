package com.bobby.valorant.round;

import com.bobby.valorant.Config;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Tracks agent lock-ins per team on the server. Enforces unique-per-team when enabled in config.
 */
public final class AgentLockManager {
    private static final WeakHashMap<MinecraftServer, AgentLockManager> INSTANCES = new WeakHashMap<>();

    public static AgentLockManager get(MinecraftServer server) {
        return INSTANCES.computeIfAbsent(server, s -> new AgentLockManager());
    }

    private final Set<Agent> lockedA = EnumSet.noneOf(Agent.class);
    private final Set<Agent> lockedV = EnumSet.noneOf(Agent.class);
    private final Map<UUID, Agent> playerToLocked = new HashMap<>();

    private AgentLockManager() {}

    public synchronized boolean tryLock(ServerPlayer player, Agent agent) {
        if (player.getServer() == null) return false;
        String teamId = getTeamId(player);
        boolean uniquePerTeam = Config.COMMON.uniqueAgentsPerTeam.get();
        if (uniquePerTeam && teamId != null) {
            if (isLockedForTeam(teamId, agent) && playerToLocked.get(player.getUUID()) != agent) {
                return false;
            }
        }

        // Remove previous lock if switching agent
        unlock(player);

        getTeamSet(teamId).add(agent);
        playerToLocked.put(player.getUUID(), agent);
        return true;
    }

    public synchronized void unlock(ServerPlayer player) {
        String teamId = getTeamId(player);
        Agent prev = playerToLocked.remove(player.getUUID());
        if (prev != null) {
            getTeamSet(teamId).remove(prev);
        }
    }

    public synchronized void onTeamChanged(ServerPlayer player) {
        // Remove any lock regardless of old team; re-lock must be explicit
        Agent prev = playerToLocked.remove(player.getUUID());
        if (prev != null) {
            lockedA.remove(prev);
            lockedV.remove(prev);
        }
    }

    public synchronized Set<Agent> getLocked(String teamId) {
        return Set.copyOf(getTeamSet(teamId));
    }

    public synchronized Set<Agent> getLockedA() { return Set.copyOf(lockedA); }
    public synchronized Set<Agent> getLockedV() { return Set.copyOf(lockedV); }

    private boolean isLockedForTeam(String teamId, Agent agent) {
        return getTeamSet(teamId).contains(agent);
    }

    private Set<Agent> getTeamSet(String teamId) {
        if ("A".equalsIgnoreCase(teamId)) return lockedA;
        return lockedV; // default to V when unknown
    }

    private static String getTeamId(ServerPlayer player) {
        if (player.getServer() == null) return null;
        Scoreboard sb = player.getServer().getScoreboard();
        PlayerTeam team = sb.getPlayersTeam(player.getScoreboardName());
        return team != null ? team.getName() : null;
    }
}


