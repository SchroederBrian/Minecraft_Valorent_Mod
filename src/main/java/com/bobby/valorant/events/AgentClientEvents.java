package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.AgentSelectionScreen;
import com.bobby.valorant.client.ModKeyBindings;
import net.minecraft.client.Minecraft;
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
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.screen == null) {
            minecraft.setScreen(new AgentSelectionScreen());
        }
    }

    // Legacy Phoenix-only ability handlers removed
}

