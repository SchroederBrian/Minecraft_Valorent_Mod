package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.economy.EconomyData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.minecraft.server.level.ServerPlayer;

@EventBusSubscriber(modid = Valorant.MODID)
public final class EconomyEvents {
    private EconomyEvents() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource() == null) return;
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            // +200 credits for killer (MVP rule)
            int credits = EconomyData.getCredits(killer);
            EconomyData.setCredits(killer, credits + 200);
        }
    }
}


