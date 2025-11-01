package com.bobby.valorant.ability;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface AbilityEffect {
    void execute(ServerPlayer player, AbilityUseContext context);
}


