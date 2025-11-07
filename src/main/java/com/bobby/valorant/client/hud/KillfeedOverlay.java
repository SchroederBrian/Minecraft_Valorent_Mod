package com.bobby.valorant.client.hud;

import java.util.ArrayDeque;
import java.util.Deque;

import com.bobby.valorant.Config;
import com.bobby.valorant.world.agent.Agent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class KillfeedOverlay {
    private KillfeedOverlay() {}

    private static final Deque<Entry> entries = new ArrayDeque<>();

    public static void push(String killerName, String victimName, ItemStack weaponIcon) {
        push(killerName, victimName, weaponIcon, Agent.UNSELECTED, Agent.UNSELECTED);
    }

    public static void push(String killerName, String victimName, ItemStack weaponIcon, Agent killerAgent, Agent victimAgent) {
        long now = System.currentTimeMillis();
        entries.addFirst(new Entry(killerName, victimName, weaponIcon, killerAgent, victimAgent, now));
        // Trim to max messages immediately
        int max = Config.COMMON.killfeedMaxMessages.get();
        while (entries.size() > Math.max(1, max)) {
            entries.removeLast();
        }
    }

    public static void render(GuiGraphics g) {
        if (!Config.COMMON.showValorantHud.get() || !Config.COMMON.killfeedEnabled.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Expire old entries
        long now = System.currentTimeMillis();
        long ttlMs = Math.max(1, Config.COMMON.killfeedDurationTicks.get()) * 50L;
        entries.removeIf(e -> now - e.createdMs >= ttlMs);

        if (entries.isEmpty()) return;

        // Layout constants
        double scale = Math.max(0.5D, Config.COMMON.hudScale.get());
        int rightMargin = (int) Math.round(Config.COMMON.killfeedOffsetRight.get() * scale);
        int offsetDown = (int) Math.round(Config.COMMON.killfeedOffsetDown.get() * scale);
        int lineSpacing = (int) Math.round(Config.COMMON.killfeedLineSpacing.get() * scale);
        int rowH = (int) Math.round(18 * scale);
        int padX = (int) Math.round(6 * scale);
        int iconSize = (int) Math.round(16 * scale);
        int gap = (int) Math.round(4 * scale);

        int killerBg = Config.COMMON.killfeedKillerBgColor.get();
        int victimBg = Config.COMMON.killfeedVictimBgColor.get();
        int textCol = Config.COMMON.killfeedTextColor.get();

        int sw = mc.getWindow().getGuiScaledWidth();
        int baseTop = 8 + 24 + offsetDown; // below top player bar
        int y = baseTop;

        int count = 0;
        for (Entry e : entries) {
            if (count >= Math.max(1, Config.COMMON.killfeedMaxMessages.get())) break;

            String killer = e.killer;
            String victim = e.victim;
            int kTextW = mc.font.width(killer);
            int vTextW = mc.font.width(victim);

            int kChipW = (int) Math.round(kTextW * scale) + padX * 2;
            int iconW = iconSize + padX * 2;
            int vChipW = (int) Math.round(vTextW * scale) + padX * 2;
            int kAgentW = iconSize + gap; // Agent icon + gap
            int vAgentW = iconSize + gap; // Agent icon + gap
            int rowW = kAgentW + kChipW + gap + iconW + gap + vChipW + vAgentW;

            int left = sw - rightMargin - rowW;

            // Killer agent icon
            int kAgentX = left;
            int kAgentY = y + (rowH - 16) / 2;
            ItemStack killerAgentIcon = AgentHeadIcons.get(e.killerAgent);
            if (!killerAgentIcon.isEmpty()) {
                g.renderItem(killerAgentIcon, kAgentX, kAgentY);
            }

            // Killer chip
            int kChipX = left + kAgentW;
            g.fill(kChipX, y, kChipX + kChipW, y + rowH, killerBg);
            int kTextX = kChipX + padX + (int) Math.round((scale - 1.0) * 2); // small nudge for scale
            int kTextY = y + (rowH - mc.font.lineHeight) / 2;
            g.drawString(mc.font, killer, kTextX, kTextY, textCol, false);

            // Icon chip
            int iconChipX = kChipX + kChipW + gap;
            g.fill(iconChipX, y, iconChipX + iconW, y + rowH, killerBg);
            int iconX = iconChipX + padX + Math.max(0, (iconW - padX * 2 - 16) / 2);
            int iconY = y + (rowH - 16) / 2;
            if (!e.weaponIcon.isEmpty()) {
                g.renderItem(e.weaponIcon, iconX, iconY);
            }

            // Victim chip
            int vChipX = iconChipX + iconW + gap;
            g.fill(vChipX, y, vChipX + vChipW, y + rowH, victimBg);
            int vTextX = vChipX + padX;
            int vTextY = y + (rowH - mc.font.lineHeight) / 2;
            g.drawString(mc.font, victim, vTextX, vTextY, textCol, false);

            // Victim agent icon
            int vAgentX = vChipX + vChipW + gap;
            int vAgentY = y + (rowH - 16) / 2;
            ItemStack victimAgentIcon = AgentHeadIcons.get(e.victimAgent);
            if (!victimAgentIcon.isEmpty()) {
                g.renderItem(victimAgentIcon, vAgentX, vAgentY);
            }

            y += rowH + lineSpacing;
            count++;
        }
    }

    public static void pushId(String killerName, String victimName, String weaponItemId) {
        pushId(killerName, victimName, weaponItemId, Agent.UNSELECTED, Agent.UNSELECTED);
    }

    public static void pushId(String killerName, String victimName, String weaponItemId, Agent killerAgent, Agent victimAgent) {
        ItemStack icon = resolveIcon(weaponItemId);
        push(killerName, victimName, icon, killerAgent, victimAgent);
    }

    private static ItemStack resolveIcon(String weaponItemId) {
        if (weaponItemId == null || weaponItemId.isEmpty()) return ItemStack.EMPTY;
        ResourceLocation rl = ResourceLocation.tryParse(weaponItemId);
        String path = rl != null ? rl.getPath() : weaponItemId;

        // Mod weapons
        if ("vandal".equals(path)) return com.bobby.valorant.registry.ModItems.VANDAL_RIFLE.get().getDefaultInstance();
        if ("ghost".equals(path)) return com.bobby.valorant.registry.ModItems.GHOST.get().getDefaultInstance();
        if ("classic".equals(path)) return com.bobby.valorant.registry.ModItems.CLASSIC.get().getDefaultInstance();
        if ("knife".equals(path)) return com.bobby.valorant.registry.ModItems.KNIFE.get().getDefaultInstance();
        if ("spike".equals(path)) return com.bobby.valorant.registry.ModItems.SPIKE.get().getDefaultInstance();
        if ("defuser".equals(path)) return com.bobby.valorant.registry.ModItems.DEFUSER.get().getDefaultInstance();
        if ("fireball".equals(path)) return com.bobby.valorant.registry.ModItems.FIREBALL.get().getDefaultInstance();
        if ("curveball".equals(path)) return com.bobby.valorant.registry.ModItems.CURVEBALL.get().getDefaultInstance();
        if ("sheriff".equals(path)) return com.bobby.valorant.registry.ModItems.SHERIFF.get().getDefaultInstance();
        if ("frenzy".equals(path)) return com.bobby.valorant.registry.ModItems.FRENZY.get().getDefaultInstance();

        return ItemStack.EMPTY;
    }

    private record Entry(String killer, String victim, ItemStack weaponIcon, Agent killerAgent, Agent victimAgent, long createdMs) {}
}


