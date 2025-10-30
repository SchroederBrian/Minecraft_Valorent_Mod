package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.AgentSelectionScreen;
import com.bobby.valorant.client.ModKeyBindings;
import com.bobby.valorant.network.EquipCurveballPacket;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.player.CurveballData;
import com.bobby.valorant.player.FireballData;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class AgentClientEvents {
    private AgentClientEvents() {}
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ModKeyBindings.OPEN_AGENT_MENU.consumeClick()) {
            openAgentMenu();
            return; // Prevent trying to handle both keybinds in the same tick
        }
        
        if (ModKeyBindings.USE_ABILITY_1.consumeClick()) {
            Valorant.LOGGER.info("[Q DEBUG] Q key pressed!");
            handleAbility1();
        }

        if (ModKeyBindings.USE_ABILITY_2.consumeClick()) {
            Valorant.LOGGER.info("[E DEBUG] E key pressed!");
            handleAbility2();
        }
    }

    private static void openAgentMenu() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.screen == null) {
            minecraft.setScreen(new AgentSelectionScreen());
        }
    }

    private static void handleAbility1() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            Valorant.LOGGER.info("[Q DEBUG] Player or level is null");
            return;
        }

        var selectedAgent = AgentData.getSelectedAgent(minecraft.player);
        Valorant.LOGGER.info("[Q DEBUG] Selected agent: {}", selectedAgent);
        
        if (selectedAgent != Agent.PHOENIX) {
            Valorant.LOGGER.info("[Q DEBUG] Not Phoenix agent, aborting");
            return;
        }

        int charges = CurveballData.getCharges(minecraft.player);
        Valorant.LOGGER.info("[Q DEBUG] Curveball charges available: {}", charges);
        
        if (charges <= 0) {
            Valorant.LOGGER.info("[Q DEBUG] No charges available, aborting");
            return;
        }

        // Ask server to equip curveball into the currently selected slot
        ClientPacketDistributor.sendToServer(new EquipCurveballPacket());
    }

    private static void handleAbility2() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        var selectedAgent = AgentData.getSelectedAgent(minecraft.player);
        if (selectedAgent != Agent.PHOENIX) {
            return;
        }

        int charges = FireballData.getCharges(minecraft.player);
        if (charges <= 0) {
            return;
        }

        ClientPacketDistributor.sendToServer(new com.bobby.valorant.network.EquipFireballPacket());
    }
}

