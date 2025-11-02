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

    // Ultimate (X)
    public static final KeyMapping USE_ULTIMATE = new KeyMapping(
            "key.valorant.use_ultimate",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
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

    // Spike/Defuser keys
    public static final KeyMapping EQUIP_SPIKE_OR_DEFUSE = new KeyMapping(
            "key.valorant.equip_spike_or_defuse",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_4,
            "key.categories.valorant"
    );

    // Drop/Pickup weapon key
    public static final KeyMapping DROP_PICKUP_WEAPON = new KeyMapping(
            "key.valorant.drop_pickup_weapon",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.valorant"
    );

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_AGENT_MENU);
        event.register(USE_ABILITY_1);
        event.register(USE_ABILITY_2);
        event.register(USE_ABILITY_3);
        event.register(USE_ULTIMATE);
        event.register(RELOAD_WEAPON);
        event.register(OPEN_SHOP);
        event.register(EQUIP_SPIKE_OR_DEFUSE);
        event.register(DROP_PICKUP_WEAPON);
    }
}

