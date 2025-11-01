package com.bobby.valorant.client;

import com.bobby.valorant.client.hud.AgentHeadIcons;
import com.bobby.valorant.client.lock.AgentLockState;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;

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
            this.selectedAgent = Agent.UNSELECTED; // Fallback
        }
        this.hoveredAgent = selectedAgent;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(this.lockInButton = Button.builder(Component.literal("LOCK IN"), b -> onLockIn())
                .bounds(this.width / 2 - 50, this.height - 40, 100, 20)
                .build());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.minecraft != null && this.minecraft.player != null) {
            Agent lockedAgent = com.bobby.valorant.client.lock.PlayerAgentState.getAgentForPlayer(this.minecraft.player);
            if (lockedAgent != Agent.UNSELECTED && this.selectedAgent != lockedAgent) {
                this.selectedAgent = lockedAgent;
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dark, semi-transparent background
        g.fill(0, 0, this.width, this.height, 0xD0000000);
        if (this.font != null) {
            g.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        }

        renderAgentGrid(g, mouseX, mouseY);
        renderAgentDetails(g);

        // Enable/disable lock-in based on availability
        if (selectedAgent != null) {
            boolean lockedForTeam = isLockedForMyTeam(selectedAgent);
            if (this.lockInButton != null) this.lockInButton.active = !lockedForTeam;
        } else if (this.lockInButton != null) {
            this.lockInButton.active = false;
        }

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
            if (agent == Agent.UNSELECTED) continue;
            int x = startX + i * 60;
            boolean isSelected = agent == selectedAgent;
            boolean isHovered = mouseX >= x && mouseX < x + 50 && mouseY >= y && mouseY < y + 50;
            boolean isLocked = isLockedForMyTeam(agent);

            if (isHovered && !isLocked) {
                hoveredAgent = agent;
            }

            // Draw portrait box
            int borderColor = isLocked ? 0xFF444444 : (isSelected ? 0xFFFFFFFF : (isHovered ? 0xFFCCCCCC : 0xFF555555));
            int fill = isLocked ? 0x60000000 : (isSelected ? 0x80FFFFFF : 0x80000000);
            g.fill(x, y, x + 50, y + 50, fill);
            g.renderOutline(x, y, 50, 50, borderColor);

            // Draw agent head
            ItemStack head = AgentHeadIcons.get(agent);
            g.renderItem(head, x + 17, y + 17);

            if (isLocked) {
                // Distinct red line across center when locked by teammate
                g.fill(x + 2, y + 23, x + 48, y + 27, 0xFFAA0000);
            }
        }
    }

    private void renderAgentDetails(GuiGraphics g) {
        Agent agentToShow = (hoveredAgent != null) ? hoveredAgent : selectedAgent;

        if (agentToShow != null && this.font != null) {
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
                if (agent == Agent.UNSELECTED) continue;
                int x = startX + i * 60;
                if (mouseX >= x && mouseX < x + 50 && mouseY >= y && mouseY < y + 50) {
                    if (isLockedForMyTeam(agent)) {
                        return true; // ignore clicks on locked agents
                    }
                    this.selectedAgent = agent; // Only select locally; lock on button press
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

    private void onLockIn() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        if (selectedAgent == null || selectedAgent == Agent.UNSELECTED) return;
        if (isLockedForMyTeam(selectedAgent)) return;
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
                new com.bobby.valorant.network.LockAgentRequestPacket(selectedAgent.getId())
        );
        // Keep screen open to observe other locks; optionally close:
        // this.onClose();
    }

    private boolean isLockedForMyTeam(Agent agent) {
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.player.level() == null || agent == null) return false;
        var sb = this.minecraft.player.level().getScoreboard();
        PlayerTeam team = sb.getPlayersTeam(this.minecraft.player.getScoreboardName());
        String teamId = team != null ? team.getName() : null;
        if (teamId == null) return false;
        return AgentLockState.getLockedForTeamId(teamId).contains(agent);
    }
}

