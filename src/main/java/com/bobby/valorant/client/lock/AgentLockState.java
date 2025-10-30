package com.bobby.valorant.client.lock;

import com.bobby.valorant.world.agent.Agent;

import java.util.EnumSet;
import java.util.Set;

public final class AgentLockState {
    private static final Set<Agent> LOCKED_A = EnumSet.noneOf(Agent.class);
    private static final Set<Agent> LOCKED_V = EnumSet.noneOf(Agent.class);

    private AgentLockState() {}

    public static synchronized void update(Set<Agent> a, Set<Agent> v) {
        LOCKED_A.clear();
        LOCKED_A.addAll(a);
        LOCKED_V.clear();
        LOCKED_V.addAll(v);
    }

    public static synchronized void updateFromIds(Iterable<String> aIds, Iterable<String> vIds) {
        LOCKED_A.clear();
        for (String id : aIds) LOCKED_A.add(Agent.byId(id));
        LOCKED_V.clear();
        for (String id : vIds) LOCKED_V.add(Agent.byId(id));
    }

    public static synchronized Set<Agent> getLockedForTeamId(String teamId) {
        return Set.copyOf("A".equalsIgnoreCase(teamId) ? LOCKED_A : LOCKED_V);
    }
}


