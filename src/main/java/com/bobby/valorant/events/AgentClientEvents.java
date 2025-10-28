package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.AgentSelectionScreen;
import com.bobby.valorant.client.ModKeyBindings;
import com.bobby.valorant.network.GiveCurveballPacket;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.player.CurveballData;
import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
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

        Agent selectedAgent = AgentData.getSelectedAgent(minecraft.player);
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

        // Check if player already has curveball in inventory
        Inventory inventory = minecraft.player.getInventory();
        boolean hasCurveball = false;
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModItems.CURVEBALL.get())) {
                hasCurveball = true;
                Valorant.LOGGER.info("[Q DEBUG] Found curveball in slot {}", i);
                break;
            }
        }
        
        if (!hasCurveball) {
            Valorant.LOGGER.info("[Q DEBUG] Player doesn't have curveball, giving one...");
            ClientPacketDistributor.sendToServer(new GiveCurveballPacket());
        } else {
            Valorant.LOGGER.info("[Q DEBUG] Player already has curveball");
        }
    }
}

