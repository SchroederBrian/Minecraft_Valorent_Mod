package com.bobby.valorant.client.hud;

import com.bobby.valorant.Config;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.player.CurveballData;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.systems.RenderSystem;

public final class HudOverlay {
    private HudOverlay() {}

    public static void render(GuiGraphics guiGraphics) {
        if (!Config.COMMON.showValorantHud.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        double scale = Config.COMMON.hudScale.get();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale((float) scale, (float) scale);

        int sw = (int) (screenWidth / scale);
        int sh = (int) (screenHeight / scale);

        // Health and Shield (use armor as shield proxy)
        int baseY = sh - 28;
        drawBar(guiGraphics, 12, baseY, 120, 8, 0xAA1E2328); // background
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int healthWidth = (int) (120 * (health / Math.max(1.0F, maxHealth)));
        drawBar(guiGraphics, 12, baseY, healthWidth, 8, 0xFF2ECC71);
        guiGraphics.drawString(mc.font, (int) health + "/" + (int) maxHealth, 14, baseY - 10, 0xFFFFFF, false);

        int armor = player.getArmorValue();
        drawBar(guiGraphics, 12, baseY + 10, 120, 6, 0xAA1E2328);
        int shieldWidth = Math.min(120, armor * 6);
        drawBar(guiGraphics, 12, baseY + 10, shieldWidth, 6, 0xFF3498DB);

        // Ability row (Curveball only for now)
        int abilityX = sw / 2 - 120;
        int abilityY = sh - 28;
        int slotW = 36;
        int charges = CurveballData.getCharges(player);
        int color = charges > 0 ? 0xFF95A5A6 : 0xFF3A3F44;
        drawSlot(guiGraphics, abilityX, abilityY, slotW, 24, color);
        guiGraphics.drawString(mc.font, Component.literal("Q"), abilityX + 4, abilityY + 4, 0xFFFFFFFF, false);
        guiGraphics.drawString(mc.font, Component.literal(String.valueOf(Math.max(0, charges))), abilityX + 20, abilityY + 4, 0xFFFFFFFF, false);

        // Weapon panel (basic)
        ItemStack held = player.getMainHandItem();
        String weaponName = held.isEmpty() ? "Unarmed" : held.getHoverName().getString();
        String ammo = held.isEmpty() ? "" : String.valueOf(held.getCount());
        String weaponText = ammo.isEmpty() ? weaponName : weaponName + "  " + ammo;
        guiGraphics.drawCenteredString(mc.font, Component.literal(weaponText), sw / 2, sh - 46, 0xFFFFFFFF);

        // Agent indicator
        Agent agent = AgentData.getSelectedAgent(player);
        // Right-aligned agent name
        String agentName = agent.getDisplayName().getString();
        int nameWidth = mc.font.width(agentName);
        guiGraphics.drawString(mc.font, agentName, sw - 12 - nameWidth, sh - 26, 0xFFFFFFFF, false);

        guiGraphics.pose().popMatrix();
    }

    private static void drawBar(GuiGraphics g, int x, int y, int w, int h, int argb) {
        g.fill(x, y, x + w, y + h, argb);
    }

    private static void drawSlot(GuiGraphics g, int x, int y, int w, int h, int argb) {
        g.fill(x, y, x + w, y + h, argb);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xAA1E2328);
    }
}


