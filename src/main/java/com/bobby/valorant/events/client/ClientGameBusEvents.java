package com.bobby.valorant.events.client;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class ClientGameBusEvents {
    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (!Config.COMMON.hideVanillaHotbar.get()) {
            return;
        }

        if (event.getName().equals(VanillaGuiLayers.HOTBAR)
                || event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)
                || event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)
                || event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL)
                || event.getName().equals(VanillaGuiLayers.AIR_LEVEL)
                || event.getName().equals(VanillaGuiLayers.SELECTED_ITEM_NAME)) {
            event.setCanceled(true);
        }
    }
}
