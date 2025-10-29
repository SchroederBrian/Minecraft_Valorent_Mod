package com.bobby.valorant.client;

import com.bobby.valorant.client.hud.AgentHeadIcons;
import com.bobby.valorant.client.hud.HudOverlay;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AgentSelectionScreen extends Screen {

    private Agent selectedAgent;
    private Agent hoveredAgent;
    private Button lockInButton;

    public AgentSelectionScreen() {
        super(Component.translatable("screen.valorant.agent_selection"));
        if (minecraft != null && minecraft.player != null) {
            this.selectedAgent = AgentData.getSelectedAgent(minecraft.player);
        } else {
            this.selectedAgent = Agent.JETT; // Fallback
        }
        this.hoveredAgent = selectedAgent;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(this.lockInButton = Button.builder(Component.literal("LOCK IN"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 40, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dark, semi-transparent background
        g.fill(0, 0, this.width, this.height, 0xD0000000);
        g.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        renderAgentGrid(g, mouseX, mouseY);
        renderAgentDetails(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderAgentGrid(GuiGraphics g, int mouseX, int mouseY) {
        List<Agent> agents = List.of(Agent.values());
        int gridWidth = agents.size() * 60;
        int startX = this.width / 2 - gridWidth / 2;
        int y = this.height / 2 - 30;
        hoveredAgent = null;

        for (int i = 0; i < agents.size(); i++) {
            Agent agent = agents.get(i);
            int x = startX + i * 60;
            boolean isSelected = agent == selectedAgent;
            boolean isHovered = mouseX >= x && mouseX < x + 50 && mouseY >= y && mouseY < y + 50;

            if (isHovered) {
                hoveredAgent = agent;
            }

            // Draw portrait box
            int borderColor = isSelected ? 0xFFFFFFFF : (isHovered ? 0xFFCCCCCC : 0xFF555555);
            g.fill(x, y, x + 50, y + 50, isSelected ? 0x80FFFFFF : 0x80000000);
            g.renderOutline(x, y, 50, 50, borderColor);

            // Draw agent head
            ItemStack head = AgentHeadIcons.get(agent);
            g.renderItem(head, x + 17, y + 17);
        }
    }

    private void renderAgentDetails(GuiGraphics g) {
        Agent agentToShow = (hoveredAgent != null) ? hoveredAgent : selectedAgent;

        if (agentToShow != null) {
            // Big agent name
            g.drawCenteredString(this.font, agentToShow.getDisplayName().getVisualOrderText(), this.width / 2, this.height - 80, 0xFFFFFF);
            // Agent role
            g.drawCenteredString(this.font, agentToShow.getRole(), this.width / 2, this.height - 68, 0xAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            List<Agent> agents = List.of(Agent.values());
            int gridWidth = agents.size() * 60;
            int startX = this.width / 2 - gridWidth / 2;
            int y = this.height / 2 - 30;

            for (int i = 0; i < agents.size(); i++) {
                Agent agent = agents.get(i);
                int x = startX + i * 60;
                if (mouseX >= x && mouseX < x + 50 && mouseY >= y && mouseY < y + 50) {
                    this.selectedAgent = agent;
                    if (this.minecraft != null && this.minecraft.player != null) {
                        AgentData.setSelectedAgent(this.minecraft.player, agent);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int argb(int a, int r, int g, int b) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }
}

