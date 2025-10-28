package com.bobby.valorant.events;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class HudHider {
    private HudHider() {}

    @SubscribeEvent
    public static void onPreOverlay(RenderGuiLayerEvent.Pre event) {
        if (!Config.COMMON.hideVanillaHotbar.get()) {
            return;
        }
        String name = String.valueOf(event.getLayer()).toLowerCase(java.util.Locale.ROOT);
        if (name.endsWith("hotbar") || name.contains(":hotbar")) {
            event.setCanceled(true);
        }
    }
}


