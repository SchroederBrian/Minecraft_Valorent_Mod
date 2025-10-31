package com.bobby.valorant.client.hud;

import com.bobby.valorant.player.FireballData;
import com.bobby.valorant.world.item.WeaponAmmoData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class HudOverlay {
	private static final ResourceLocation ABILITY_BLAZE = ResourceLocation.fromNamespaceAndPath("valorant", "textures/gui/abilities/blaze.png");
	private static final ResourceLocation ABILITY_CURVEBALL = ResourceLocation.fromNamespaceAndPath("valorant", "textures/gui/abilities/curveball.png");
	private static final ResourceLocation ABILITY_HOT_HANDS = ResourceLocation.fromNamespaceAndPath("valorant", "textures/gui/abilities/hot_hands.png");
	private static final ResourceLocation ABILITY_RUN_IT_BACK = ResourceLocation.fromNamespaceAndPath("valorant", "textures/gui/abilities/run_it_back.png");

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
		renderModernAbilitiesBar(guiGraphics, player, screenWidth, screenHeight);
		renderWeaponInfo(guiGraphics, mc, player, screenWidth, screenHeight);
		renderSpikeIndicator(guiGraphics, mc, player, screenWidth, screenHeight);

		// Render title overlay (last to draw on top)
		TitleOverlay.render(guiGraphics);
	}

	private static void renderSpikeIndicator(GuiGraphics guiGraphics, net.minecraft.client.Minecraft mc, Player player, int sw, int sh) {
		// Check if player has Spike in inventory
		boolean hasSpike = false;
		int spikeSlot = -1;
		int invSize = player.getInventory().getContainerSize();
		for (int i = 0; i < invSize; i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.is(com.bobby.valorant.registry.ModItems.SPIKE.get())) {
				hasSpike = true;
				spikeSlot = i;
				break;
			}
		}

		if (!hasSpike) {
			return;
		}

		// Draw spike indicator on the right side, middle of screen
		int panelWidth = 100;
		int panelHeight = 32;
		int x = sw - panelWidth - 20;
		int y = sh / 2 - panelHeight / 2;

		// Background panel (similar to money panel)
		guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, 0x90181D22);

		// Left accent stripe (spike colored - yellow/gold)
		guiGraphics.fill(x + 106, y, x + 100, y + panelHeight, argb(255, 255, 193, 7));

		// Spike icon
		ItemStack spikeStack = player.getInventory().getItem(spikeSlot);
		guiGraphics.renderItem(spikeStack, x + 8, y + 8);

		// Text label
		guiGraphics.drawString(mc.font, "You have the", x + 30, y + 5, 0xFFFFD700, false);

		// Status indicator
		guiGraphics.drawString(mc.font, "Spike", x + 30, y + 16, 0xFFB8E986, false);
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
		int slotH = 24; // tighter
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
			com.bobby.valorant.world.agent.Agent agent = com.bobby.valorant.client.lock.PlayerAgentState.getAgentForPlayer(p);
			ItemStack head = com.bobby.valorant.client.hud.AgentHeadIcons.get(agent);
			g.renderItem(head, x + w / 2 - 8, y + 4);
		}
	}

	private static void renderModernAbilitiesBar(GuiGraphics g, Player player, int sw, int sh) {
		int slotW = 32;
		int slotH = 32;
		int gap = 6;
		int totalSlots = 4;
		int panelWidth = totalSlots * slotW + (totalSlots - 1) * gap;
		int baseX = sw / 2 - panelWidth / 2;
		int baseY = sh - slotH - 42;

		// Background - a subtle gradient borrowing from health card style
		//int bgTop = argb(160, 12, 14, 16);
		//int bgBottom = argb(160, 8, 9, 11);
		//g.fillGradient(baseX - 12, baseY - 12, baseX + panelWidth + 12, baseY + slotH + 12, bgTop, bgBottom);

		// Define abilities
		int cbCharges = com.bobby.valorant.player.CurveballData.getCharges(player);
		int maxCbCharges = com.bobby.valorant.Config.COMMON.curveballMaxCharges.get();
		String qKey = getKeyLabel("Q", com.bobby.valorant.client.ModKeyBindings.USE_ABILITY_1);

		// Draw slots with new modern style
		// Slot 1: C (Blaze)
		drawModernAbilitySlot(g, baseX, baseY, slotW, slotH, ABILITY_BLAZE, "C", 1, 1, false, 0);

		// Slot 2: Q (Curveball)
		drawModernAbilitySlot(g, baseX + slotW + gap, baseY, slotW, slotH, ABILITY_CURVEBALL, qKey, cbCharges, maxCbCharges, false, 0);

		// Slot 3: E (Fireball/Hot Hands)
		int fbCharges = FireballData.getCharges(player);
		int maxFbCharges = com.bobby.valorant.Config.COMMON.fireballMaxCharges.get();
		String eKey = getKeyLabel("E", com.bobby.valorant.client.ModKeyBindings.USE_ABILITY_2);
		drawModernAbilitySlot(g, baseX + 2 * (slotW + gap), baseY, slotW, slotH, ABILITY_HOT_HANDS, eKey, fbCharges, maxFbCharges, false, 0);

		// Slot 4: X (Ultimate - Run it Back)
		// For now, let's assume ultimate points are tracked from 0 to 6
		int ultimatePoints = com.bobby.valorant.player.UltimateData.getPoints(player);
		drawModernAbilitySlot(g, baseX + 3 * (slotW + gap), baseY, slotW, slotH, ABILITY_RUN_IT_BACK, "X", ultimatePoints, 6, true, ultimatePoints);
	}

	private static String getKeyLabel(String fallback, net.minecraft.client.KeyMapping mapping) {
		if (mapping != null) {
			String text = mapping.getTranslatedKeyMessage().getString();
			if (text != null && !text.isEmpty()) return text;
		}
		return fallback;
	}

	private static void drawModernAbilitySlot(GuiGraphics g, int x, int y, int w, int h,
											  ResourceLocation iconTexture, String keyLabel, int charges, int maxCharges,
											  boolean isUltimate, int ultimatePoints) {
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();

		// Determine state colors
		boolean isAvailable = charges > 0 || (isUltimate && ultimatePoints >= maxCharges);
		int accentColor;
		if (isAvailable) {
			accentColor = charges == 1 && !isUltimate ? argb(255, 255, 220, 100) : argb(255, 100, 220, 255); // Yellowish for last charge, else cyan
		} else {
			accentColor = argb(255, 180, 80, 80); // Red when not available
		}

		// Main slot background
		g.fill(x, y, x + w, y + h, argb(180, 20, 22, 28));

		// Accent line at the bottom
		g.fill(x, y + h - 3, x + w, y + h, accentColor);

		// Icon
		if (iconTexture != null) {
			// Render the texture at 16x16 size
			g.blit(iconTexture, x + w / 2 - 8, y + h / 2 - 8, 0, 0, 16, 16, 16, 16);
		} else {
			// Placeholder shape for empty/ultimate
			if (isUltimate) { // Diamond for ult
				g.drawString(mc.font, "◆", x + w/2 - mc.font.width("◆")/2, y + h/2 - 4, argb(isAvailable ? 255:100, 255, 255, 255));
			} else { // Circle for basic
				g.drawCenteredString(mc.font, "●", x + w/2, y + h/2 - 4, argb(isAvailable ? 200:80, 255, 255, 255));
			}
		}

		// Key label in top-left
		g.drawString(mc.font, keyLabel, x + 4, y + 4, 0xFFFFFFFF);

		// Charges or Ultimate points
		if (isUltimate) {
			int dotSize = 2;
			int dotGap = 2;
			int totalDotWidth = maxCharges * dotSize + Math.max(0, (maxCharges - 1)) * dotGap;
			int dotStartX = x + w / 2 - totalDotWidth / 2;
			int dotY = y + h - dotSize - 8;
			for (int i = 0; i < maxCharges; i++) {
				int dotColor = i < ultimatePoints ? argb(255, 255, 255, 255) : argb(100, 255, 255, 255);
				g.fill(dotStartX + i * (dotSize + dotGap), dotY, dotStartX + i * (dotSize + dotGap) + dotSize, dotY + dotSize, dotColor);
			}
		} else {
			// Regular charges in bottom-right
			String cText = String.valueOf(charges);
			g.drawString(mc.font, cText, x + w - mc.font.width(cText) - 4, y + h - mc.font.lineHeight - 4, 0xFFFFFFFF);
		}
	}

	private static void renderWeaponInfo(GuiGraphics guiGraphics, net.minecraft.client.Minecraft mc, Player player, int sw, int sh) {
		ItemStack heldItem = player.getMainHandItem();
		if (heldItem.isEmpty() || !(heldItem.getItem() instanceof com.bobby.valorant.world.item.IWeapon weapon)) {
			return;
		}

		int cardWidth = 120;
		int cardHeight = 44;
		int x = sw - cardWidth - 20;
		int y = sh - cardHeight - 20;

		// Background - borrow from health card style
		int bgTop = argb(160, 12, 14, 16);
		int bgBottom = argb(160, 8, 9, 11);
		guiGraphics.fillGradient(x, y, x + cardWidth, y + cardHeight, bgTop, bgBottom);

		// Right accent stripe (consistent look)
		guiGraphics.fill(x + cardWidth - 6, y, x + cardWidth, y + cardHeight, argb(255, 100, 220, 255));

		// Ammo text
		String clipAmmo = String.valueOf(WeaponAmmoData.getCurrentAmmo(heldItem));
		String reserveAmmo = String.valueOf(WeaponAmmoData.getReserveAmmo(heldItem));
		String ammoText = clipAmmo + " / " + reserveAmmo;

		// Use a larger font for ammo count
		// Note: mc.font doesn't support scaling directly. For a true Valorant look, a custom font renderer or scaling would be needed.
		// For now, we'll draw it larger by rendering it multiple times or using a different font if available.
		// As a simple stand-in, let's just draw it big.
		guiGraphics.drawString(mc.font, clipAmmo, x + 20, y + 14, 0xFFFFFFFF, true);

		// Separator and reserve
		int clipWidth = mc.font.width(clipAmmo);
		guiGraphics.drawString(mc.font, "/", x + 20 + clipWidth + 4, y + 18, 0xFFAAAAAA, false);
		guiGraphics.drawString(mc.font, reserveAmmo, x + 20 + clipWidth + 12, y + 22, 0xFFAAAAAA, false);


		// Weapon icon on the right
		guiGraphics.renderItem(heldItem, x + cardWidth - 40, y + (cardHeight - 16) / 2);
	}
}


