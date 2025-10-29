package com.bobby.valorant.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class HudOverlay {
	private HudOverlay() {}

	public static void render(GuiGraphics guiGraphics) {
		if (!com.bobby.valorant.Config.COMMON.showValorantHud.get()) {
			return;
		}
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
		Player player = mc.player;
		if (player == null) {
			return;
		}

		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();

		// Money pill (top-left)
		int credits = com.bobby.valorant.economy.EconomyData.getCredits(player);
		String moneyText = "$" + credits;
		int pillX = 12;
		int pillY = 10;
		int pillW = mc.font.width(moneyText) + 18;
		int pillH = 18;
		guiGraphics.fill(pillX, pillY, pillX + pillW, pillY + pillH, 0x90181D22);
		guiGraphics.drawString(mc.font, moneyText, pillX + 8, pillY + 5, 0xFFB8E986, false);

		renderTopPlayerBar(guiGraphics, mc, player, screenWidth);
		renderHealthCard(guiGraphics, mc, player, screenHeight);
		renderAbilities(guiGraphics, player, screenWidth, screenHeight);
		renderWeaponInfo(guiGraphics, mc, player, screenWidth, screenHeight);
	}

	// New minimal health card (bottom-left)
	private static void renderHealthCard(GuiGraphics g, net.minecraft.client.Minecraft mc, Player player, int sh) {
		int cardWidth = 160;
		int cardHeight = 44;
		int x = 20;
		int y = sh - cardHeight - 20;

		float health = Math.max(0, player.getHealth());
		float maxHealth = Math.max(1.0F, player.getMaxHealth());
		float pct = Math.min(1.0F, health / maxHealth);
		int barWidth = (int) ((cardWidth - 20) * pct);

		int bgTop = argb(160, 12, 14, 16);
		int bgBottom = argb(160, 8, 9, 11);
		g.fillGradient(x, y, x + cardWidth, y + cardHeight, bgTop, bgBottom);

		// Left accent stripe colored by health
		int healthColor = healthColor(pct);
		g.fill(x, y, x + 6, y + cardHeight, healthColor);

		// Health bar
		int barX = x + 12;
		int barY = y + cardHeight - 12;
		g.fill(barX, barY, x + cardWidth - 8, barY + 6, argb(100, 255, 255, 255));
		g.fill(barX, barY, barX + barWidth, barY + 6, healthColor);

		// Labels and values
		g.drawString(mc.font, "HP", x + 12, y + 8, 0xFFFFFFFF, false);
		String hpStr = String.valueOf((int) health);
		g.drawString(mc.font, hpStr, x + 36, y + 8, 0xFFFFFFFF, true);

		// Armor pips (use armor value, up to 5 pips)
		int armor = player.getArmorValue();
		int pips = Math.min(5, (armor + 3) / 4); // 0..20 armor → 0..5 pips
		int pipStartX = x + cardWidth - 62;
		int pipY = y + 8;
		for (int i = 0; i < 5; i++) {
			int pipX = pipStartX + i * 10;
			int col = i < pips ? argb(240, 230, 230, 230) : argb(80, 230, 230, 230);
			g.fill(pipX, pipY, pipX + 8, pipY + 12, col);
		}
	}

	private static int healthColor(float pct) {
		// Interpolate from red (low) to green (high)
		int r1 = 231, g1 = 76, b1 = 60;   // red
		int r2 = 46,  g2 = 204, b2 = 113; // green
		int r = (int) (r1 + (r2 - r1) * pct);
		int g = (int) (g1 + (g2 - g1) * pct);
		int b = (int) (b1 + (b2 - b1) * pct);
		return argb(255, r, g, b);
	}

	private static int argb(int a, int r, int g, int b) {
		return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
	}

	private static void renderAbilities(GuiGraphics g, Player player, int sw, int sh) {
		int baseX = sw / 2 - 96;   // tighter width overall
		int baseY = sh - 30;       // slightly closer to bottom
		int slotW = 32;            // shrink slot width
		int slotH = 24;            // shrink slot height
		int gap = 6;               // tighter gap

		// Panel background (tighter padding)
		g.fill(baseX - 8, baseY - 6, baseX + 4 * (slotW + gap) - gap + 8, baseY + slotH + 16, 0x40000000);

		// Slot 1: C (placeholder)
		drawAbilitySlot(g, baseX + 0 * (slotW + gap), baseY, slotW, slotH,
				null, getKeyLabel("C", null), 1);

		// Slot 2: Q (Curveball item icon and real charges)
		ItemStack curveballIcon = new ItemStack(com.bobby.valorant.registry.ModItems.CURVEBALL.get());
		int cbCharges = com.bobby.valorant.player.CurveballData.getCharges(player);
		String qKey = getKeyLabel("Q", com.bobby.valorant.client.ModKeyBindings.USE_ABILITY_1);
		drawAbilitySlot(g, baseX + 1 * (slotW + gap), baseY, slotW, slotH,
				curveballIcon, qKey, Math.max(0, cbCharges));

		// Slot 3: E (placeholder)
		drawAbilitySlot(g, baseX + 2 * (slotW + gap), baseY, slotW, slotH,
				null, getKeyLabel("E", null), 1);

		// Slot 4: X (ultimate placeholder with 0/5 dots style later)
		drawAbilitySlot(g, baseX + 3 * (slotW + gap), baseY, slotW, slotH,
				null, getKeyLabel("X", null), 0);
	}

	private static String getKeyLabel(String fallback, net.minecraft.client.KeyMapping mapping) {
		if (mapping != null) {
			String text = mapping.getTranslatedKeyMessage().getString();
			if (text != null && !text.isEmpty()) return text;
		}
		return fallback;
	}

	private static void drawAbilitySlot(GuiGraphics g, int x, int y, int w, int h,
										ItemStack icon, String keyLabel, int charges) {
		// Slot background
		g.fill(x, y, x + w, y + h, 0x90000000);
		// Top key label
		g.drawString(net.minecraft.client.Minecraft.getInstance().font, keyLabel, x + 4, y + 4, 0xFFFFFFFF, false);
		// Icon
		if (icon != null && !icon.isEmpty()) {
			g.renderItem(icon, x + (w / 2) - 8, y + 8);
		} else {
			// faint placeholder
			g.fill(x + (w / 2) - 6, y + 10, x + (w / 2) + 6, y + 22, 0x40FFFFFF);
		}
		// Charges under the slot
		String cText = String.valueOf(Math.max(0, charges));
		g.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, cText, x + w / 2, y + h + 6, 0xFFFFFFFF);
	}

	private static void renderTopPlayerBar(GuiGraphics g, net.minecraft.client.Minecraft mc, Player local, int sw) {
		final net.minecraft.client.multiplayer.ClientLevel level = mc.level;
		if (level == null) return;

		java.util.List<? extends Player> all = level.players();
		java.util.List<Player> attackers = new java.util.ArrayList<>(); // Team A
		java.util.List<Player> defenders = new java.util.ArrayList<>(); // Team V

		for (Player p : all) {
			net.minecraft.world.scores.Team t = p.getTeam();
			if (t != null) {
				String name = t.getName();
				if ("A".equalsIgnoreCase(name)) attackers.add(p);
				else if ("V".equalsIgnoreCase(name)) defenders.add(p);
				else if (attackers.size() <= defenders.size()) attackers.add(p); else defenders.add(p);
			} else {
				if (attackers.size() <= defenders.size()) attackers.add(p); else defenders.add(p);
			}
		}

		int slots = 5;
		int slotW = 28; // tighter
		int slotH = 22; // tighter
		int gap = 4;    // tighter
		int timerW = 60; // tighter timer chip
		int y = 8;
		int cx = sw / 2;

		// Scores boxes (attackers = red, defenders = blue)
		int scoreBoxW = 24;
		int scoreBoxH = slotH + 2;
		int leftScore = com.bobby.valorant.round.RoundState.getLeftScore();
		int rightScore = com.bobby.valorant.round.RoundState.getRightScore();
		int redBG = 0x90E74C3C;
		int blueBG = 0x903498DB;
		g.fill(cx - (timerW / 2) - scoreBoxW - 6, y - 2, cx - (timerW / 2) - 6, y - 2 + scoreBoxH, redBG);
		g.fill(cx + (timerW / 2) + 6, y - 2, cx + (timerW / 2) + 6 + scoreBoxW, y - 2 + scoreBoxH, blueBG);
		g.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, String.valueOf(leftScore), cx - (timerW / 2) - scoreBoxW / 2 - 6, y + 5, 0xFFFFFFFF);
		g.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, String.valueOf(rightScore), cx + (timerW / 2) + scoreBoxW / 2 + 6, y + 5, 0xFFFFFFFF);

		// Center timer chip (round state)
		String timeLabel = com.bobby.valorant.round.RoundState.formattedTime();
		g.fill(cx - timerW / 2, y - 2, cx + timerW / 2, y + slotH + 2, 0x90000000);
		g.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, timeLabel, cx, y + 5, 0xFFFFFFFF);

		// Draw team slots
		int leftPanelW = slots * slotW + (slots - 1) * gap;
		int leftStart = cx - timerW / 2 - 6 - leftPanelW - scoreBoxW - 4;
		for (int i = 0; i < slots; i++) {
			int x = leftStart + i * (slotW + gap);
			Player p = i < attackers.size() ? attackers.get(i) : null;
			drawPlayerBadge(g, x, y, slotW, slotH, p, true);
		}

		int rightStart = cx + timerW / 2 + 6 + scoreBoxW + 4;
		for (int i = 0; i < slots; i++) {
			int x = rightStart + i * (slotW + gap);
			Player p = i < defenders.size() ? defenders.get(i) : null;
			drawPlayerBadge(g, x, y, slotW, slotH, p, false);
		}
	}

	private static void drawPlayerBadge(GuiGraphics g, int x, int y, int w, int h, Player p, boolean attacker) {
		boolean alive = p != null && !p.isDeadOrDying() && p.getHealth() > 0;
		int accent = attacker ? 0xFFE74C3C : 0xFF3498DB; // red for A, blue for V
		int stripe = alive ? (attacker ? 0x80E74C3C : 0x803498DB) : 0x807F8C8D;
		g.fill(x, y, x + w, y + h, 0x90000000);
		g.fill(x, y + h - 3, x + w, y + h, stripe);
		// Side accent
		g.fill(x, y, x + 3, y + h, accent);
		// Agent/player head portrait always
		if (p != null) {
			com.bobby.valorant.world.agent.Agent agent = com.bobby.valorant.player.AgentData.getSelectedAgent(p);
			ItemStack head = com.bobby.valorant.client.hud.AgentHeadIcons.get(agent);
			g.renderItem(head, x + w / 2 - 8, y + 4);
		}
	}

	private static void renderWeaponInfo(GuiGraphics guiGraphics, net.minecraft.client.Minecraft mc, Player player, int sw, int sh) {
		ItemStack heldItem = player.getMainHandItem();

		int bgWidth = 100;
		int bgHeight = 50;
		int bgX = sw - bgWidth - 30;
		int bgY = sh - bgHeight - 25;

		// Render semi-transparent background
		guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, 0x90000000);

		if (!heldItem.isEmpty()) {
			// Render weapon icon
			guiGraphics.renderItem(heldItem, bgX + 10, bgY + 15);

			// Render ammo
			String clipAmmo = "23"; // Placeholder
			String reserveAmmo = String.valueOf(heldItem.getCount());

			guiGraphics.drawString(mc.font, clipAmmo, bgX + 40, bgY + 10, 0xFFFFFF, true);
			guiGraphics.drawString(mc.font, reserveAmmo, bgX + 40, bgY + 30, 0xFFFFFF, true);
		}
	}
}


