package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.ModKeyBindings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class AgentClientEvents {
    private AgentClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ModKeyBindings.OPEN_AGENT_MENU.consumeClick()) {
            openAgentMenu();
            return; // Prevent trying to handle both keybinds in the same tick
        }
        // Ability key handling moved to AbilityClientEvents (agent-agnostic)
    }

    private static void openAgentMenu() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;
        // "buyscreen" = deine Custom-GUI-ID aus FancyMenu
        mc.player.connection.sendCommand("openguiscreen buyscreen");
    }

    // Legacy Phoenix-only ability handlers removed
}

