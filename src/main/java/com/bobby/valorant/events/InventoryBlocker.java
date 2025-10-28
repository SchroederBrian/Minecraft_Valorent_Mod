package com.bobby.valorant.events;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class InventoryBlocker {
    private InventoryBlocker() {}

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!Config.COMMON.blockAllVanillaInventories.get()) {
            return;
        }
        Screen screen = event.getScreen();
        if (screen instanceof AbstractContainerScreen<?>) {
            event.setCanceled(true);
        }
    }
}


