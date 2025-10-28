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
        if (
                event.getLayer().equals(VanillaGuiLayers.HOTBAR)
                || event.getLayer().equals(VanillaGuiLayers.SELECTED_ITEM_NAME)
                || String.valueOf(event.getLayer()).contains("hotbar")
                || String.valueOf(event.getLayer()).contains("experience_bar")
                || String.valueOf(event.getLayer()).contains("jump")
                || String.valueOf(event.getLayer()).contains("offhand")
        ) {
            event.setCanceled(true);
        }
    }
}


