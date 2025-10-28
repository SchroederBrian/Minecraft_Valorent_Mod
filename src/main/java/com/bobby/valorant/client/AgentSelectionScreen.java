package com.bobby.valorant.client;

import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public class AgentSelectionScreen extends Screen {
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 5;
    private static final int COLUMNS = 4;
    
    private Agent selectedAgent;
    
    public AgentSelectionScreen() {
        super(Component.translatable("screen.valorant.agent_selection"));
        
        // Get current selection - safe null check
        if (minecraft != null && minecraft.player != null) {
            selectedAgent = AgentData.getSelectedAgent(minecraft.player);
        } else {
            selectedAgent = Agent.JETT;
        }
    }
    
    @Override
    protected void init() {
        super.init();
        
        int startX = this.width / 2 - (COLUMNS * (BUTTON_WIDTH + BUTTON_SPACING)) / 2;
        int startY = this.height / 2 - 80;
        
        int row = 0;
        int col = 0;
        
        // Create buttons for each agent
        for (Agent agent : Agent.values()) {
            int x = startX + col * (BUTTON_WIDTH + BUTTON_SPACING);
            int y = startY + row * (BUTTON_HEIGHT + BUTTON_SPACING);
            
            Agent finalAgent = agent;
            Button button = Button.builder(
                agent.getDisplayName(),
                b -> selectAgent(finalAgent)
            )
            .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
            
            this.addRenderableWidget(button);
            
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
        
        // Add close button
        this.addRenderableWidget(
            Button.builder(
                Component.translatable("gui.valorant.close"),
                b -> this.onClose()
            )
            .bounds(this.width / 2 - 50, this.height / 2 + 100, 100, 20)
            .build()
        );
    }
    
    private void selectAgent(Agent agent) {
        selectedAgent = agent;
        if (minecraft != null && minecraft.player != null) {
            AgentData.setSelectedAgent(minecraft.player, agent);
        }
        
        // Close the screen after selection
        this.onClose();
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render solid dark background instead of blurred to avoid "Can only blur once per frame" error
        guiGraphics.fill(0, 0, this.width, this.height, 0xC0101010);
        
        // Render title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 30, 0xFFFFFF);
        
        // Render current selection info
        if (selectedAgent != null) {
            Component selectionText = Component.translatable("gui.valorant.selected_agent", selectedAgent.getDisplayName());
            guiGraphics.drawCenteredString(this.font, selectionText, this.width / 2, 60, 0xCCCCCC);
            
            Component roleText = Component.translatable("gui.valorant.role", Component.literal(selectedAgent.getRole()));
            guiGraphics.drawCenteredString(this.font, roleText, this.width / 2, 72, 0xAAAAAA);
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

