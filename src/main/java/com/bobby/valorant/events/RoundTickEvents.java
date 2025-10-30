package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.round.RoundController;
import com.bobby.valorant.spike.SpikePlantingHandler;
import com.bobby.valorant.spike.SpikeDefusingHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class RoundTickEvents {
    private RoundTickEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null) return;
        // Drive per-level controllers
        for (var level : event.getServer().getAllLevels()) {
            RoundController.get(level).tick();
            // Planting progress
            if (!level.isClientSide()) SpikePlantingHandler.tick(level);
            if (!level.isClientSide()) SpikeDefusingHandler.tick(level);
        }
    }
}


