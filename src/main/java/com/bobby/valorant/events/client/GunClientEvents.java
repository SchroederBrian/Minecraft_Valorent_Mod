package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class GunClientEvents {
    private GunClientEvents() {}

    // Gun shooting is now handled by CustomLeftClickHandler
    // This class is kept for future gun-specific client events if needed
}


