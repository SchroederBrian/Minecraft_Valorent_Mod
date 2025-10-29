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
    public static KeyMapping OPEN_SHOP;
    public static KeyMapping SELECT_RIFLE;
    public static KeyMapping SELECT_PISTOL;
    public static KeyMapping SELECT_KNIFE;
    
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

        OPEN_SHOP = new KeyMapping(
                "key.valorant.open_shop",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                com.mojang.blaze3d.platform.InputConstants.KEY_B,
                "category.valorant.valorant"
        );
        event.register(OPEN_SHOP);

        SELECT_RIFLE = new KeyMapping(
                "key.valorant.select_rifle",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                com.mojang.blaze3d.platform.InputConstants.KEY_1,
                "category.valorant.valorant"
        );
        event.register(SELECT_RIFLE);

        SELECT_PISTOL = new KeyMapping(
                "key.valorant.select_pistol",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                com.mojang.blaze3d.platform.InputConstants.KEY_2,
                "category.valorant.valorant"
        );
        event.register(SELECT_PISTOL);

        SELECT_KNIFE = new KeyMapping(
                "key.valorant.select_knife",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                com.mojang.blaze3d.platform.InputConstants.KEY_3,
                "category.valorant.valorant"
        );
        event.register(SELECT_KNIFE);
    }
}

