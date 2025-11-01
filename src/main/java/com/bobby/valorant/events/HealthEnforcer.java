package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class HealthEnforcer {
    private HealthEnforcer() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        var attr = sp.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            if (attr.getBaseValue() != 100.0D) {
                attr.setBaseValue(100.0D);
                // Do not forcibly heal; keep current health as-is
            }
        }
    }
}


