package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Valorant.MODID)
public final class PlayerMovementEvents {
    private PlayerMovementEvents() {}

    // Track previous sprinting state for each player
    private static final Map<ServerPlayer, Boolean> previousSprintingState = new WeakHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean wasSprinting = previousSprintingState.getOrDefault(player, false);
        boolean isSprinting = player.isSprinting();

        // Check if player just started sprinting
        if (!wasSprinting && isSprinting) {
            Valorant.LOGGER.debug("Player {} is attempting to sprint!", player.getName().getString());

            if (com.bobby.valorant.Config.COMMON.preventSprinting.get()) {
                // Prevent sprinting by setting the player back to walking
                player.setSprinting(false);
            }
        }

        // Update tracking state
        previousSprintingState.put(player, isSprinting);
    }
}
