package com.bobby.valorant.client;

import com.bobby.valorant.Valorant;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public class ModKeyBindings {
    public static KeyMapping OPEN_AGENT_MENU;
    
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_AGENT_MENU = new KeyMapping(
            "key.valorant.open_agent_menu",
            com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
            com.mojang.blaze3d.platform.InputConstants.KEY_M,
            "category.valorant.valorant"
        );
        event.register(OPEN_AGENT_MENU);
    }
}

