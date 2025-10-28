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
    public static KeyMapping USE_ABILITY_1;
    
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_AGENT_MENU = new KeyMapping(
            "key.valorant.open_agent_menu",
            com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
            com.mojang.blaze3d.platform.InputConstants.KEY_B,
            "category.valorant.valorant"
        );
        event.register(OPEN_AGENT_MENU);

        USE_ABILITY_1 = new KeyMapping(
                "key.valorant.use_ability_1",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                com.mojang.blaze3d.platform.InputConstants.KEY_Q,
                "category.valorant.valorant"
        );
        event.register(USE_ABILITY_1);
    }
}

