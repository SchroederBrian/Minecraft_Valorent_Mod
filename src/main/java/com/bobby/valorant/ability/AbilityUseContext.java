package com.bobby.valorant.ability;

import net.minecraft.server.level.ServerLevel;

public final class AbilityUseContext {
    private final ServerLevel level;

    private AbilityUseContext(ServerLevel level) {
        this.level = level;
    }

    public static AbilityUseContext of(ServerLevel level) {
        return new AbilityUseContext(level);
    }

    public ServerLevel level() { return level; }
}


