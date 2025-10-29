package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.hud.HudOverlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class ClientModBusEvents {
    public static final ResourceLocation CUSTOM_HUD_ID = ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "custom_hud");

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, CUSTOM_HUD_ID, ClientModBusEvents::renderCustomHud);
    }

    private static void renderCustomHud(GuiGraphics gg, DeltaTracker deltaTracker) {
        HudOverlay.render(gg);
    }
}
