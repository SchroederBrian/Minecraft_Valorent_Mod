package com.bobby.valorant.client;

import com.bobby.valorant.Valorant;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class ModKeyBindings {
    private ModKeyBindings() {}

    // Agent menu
    public static final KeyMapping OPEN_AGENT_MENU = new KeyMapping(
            "key.valorant.open_agent_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.categories.valorant"
    );

    // Ability 1 (Q)
    public static final KeyMapping USE_ABILITY_1 = new KeyMapping(
            "key.valorant.use_ability_1",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Q,
            "key.categories.valorant"
    );

    // Ability 2 (E)
    public static final KeyMapping USE_ABILITY_2 = new KeyMapping(
            "key.valorant.use_ability_2",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            "key.categories.valorant"
    );

    // Ability 3 (c) - Fire Wall
    public static final KeyMapping USE_ABILITY_3 = new KeyMapping(
            "key.valorant.use_ability_3",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.valorant"
    );

    public static final KeyMapping RELOAD_WEAPON = new KeyMapping(
            "key.valorant.reload_weapon",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.valorant"
    );

    // Shop (B)
    public static final KeyMapping OPEN_SHOP = new KeyMapping(
            "key.valorant.open_shop",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.valorant"
    );

    // Buy screen (H) - FancyMenu integration
    public static final KeyMapping OPEN_BUY_FANCY = new KeyMapping(
            "key.valorant.open_buy_fancy",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.valorant"
    );

    // Quick weapon selects (Z/X/C by default)
    public static final KeyMapping SELECT_RIFLE = new KeyMapping(
            "key.valorant.select_rifle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.categories.valorant"
    );
    public static final KeyMapping SELECT_PISTOL = new KeyMapping(
            "key.valorant.select_pistol",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.valorant"
    );
    public static final KeyMapping SELECT_KNIFE = new KeyMapping(
            "key.valorant.select_knife",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.valorant"
    );

    // Spike/Defuser keys
    public static final KeyMapping DROP_SPIKE = new KeyMapping(
            "key.valorant.drop_spike",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.valorant"
    );
    public static final KeyMapping EQUIP_SPIKE_OR_DEFUSE = new KeyMapping(
            "key.valorant.equip_spike_or_defuse",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_4,
            "key.categories.valorant"
    );

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_AGENT_MENU);
        event.register(USE_ABILITY_1);
        event.register(USE_ABILITY_2);
        event.register(USE_ABILITY_3);
        event.register(OPEN_SHOP);
        event.register(OPEN_BUY_FANCY);
        event.register(SELECT_RIFLE);
        event.register(SELECT_PISTOL);
        event.register(SELECT_KNIFE);
        event.register(DROP_SPIKE);
        event.register(EQUIP_SPIKE_OR_DEFUSE);
    }
}

