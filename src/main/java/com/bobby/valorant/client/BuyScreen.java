package com.bobby.valorant.client;

import java.util.ArrayList;
import java.util.List;

import com.bobby.valorant.economy.EconomyData;
import com.bobby.valorant.economy.ShopItem;
import com.bobby.valorant.network.BuyRequestPacket;
import com.bobby.valorant.round.RoundState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.resources.ResourceLocation;
import com.bobby.valorant.Valorant;

public class BuyScreen extends Screen {
    private static final int PADDING = 14;
    private ShopItem.Category selectedCategory = ShopItem.Category.SIDEARM;
    private ShopItem selectedItem = ShopItem.SIDEARM_CLASSIC;
    private final List<ShopItem> filtered = new ArrayList<>();
    private long lastClickTimeMs = 0L;
    private int lastClickedItemIndex = -1;

    private int gridCols;
    private final int cellW = 100, cellH = 60;
    private static final ResourceLocation ARMOR_ICON = ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "textures/gui/bigarmor.png");

    public BuyScreen() { super(Component.literal("Shop")); }

    @Override
    protected void init() {
        super.init();
        filter();

        int catWidth = 140;
        int detailsWidth = 200;
        int spacing = 20;
        int cellSpacing = 12;

        int availableGridWidth = this.width - (PADDING + catWidth + spacing) - (detailsWidth + spacing + PADDING);
        this.gridCols = Math.max(1, availableGridWidth / (this.cellW + cellSpacing));
    }

    private void filter() {
        filtered.clear();
        for (ShopItem it : ShopItem.values()) if (it.category == selectedCategory) filtered.add(it);
        if (!filtered.contains(selectedItem) && !filtered.isEmpty()) selectedItem = filtered.get(0);
    }

    private void sendBuy(ShopItem item, boolean sell) {
        if (!RoundState.isBuyPhase()) return;
        ClientPacketDistributor.sendToServer(new BuyRequestPacket(item.name(), sell));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int cx = PADDING;
        int cy = PADDING + 40;
        int cw = 140, ch = 26;
        ShopItem.Category[] cats = ShopItem.Category.values();
        for (int i = 0; i < cats.length; i++) {
            int y = cy + i * (ch + 6);
            if (mx >= cx && mx <= cx + cw && my >= y && my <= y + ch) {
                selectedCategory = cats[i];
                filter();
                return true;
            }
        }

        int gridX = cx + cw + 20;
        int gridY = PADDING + 40;
        int cellSpacing = 12;
        for (int i = 0; i < filtered.size(); i++) {
            int gx = gridX + (i % this.gridCols) * (this.cellW + cellSpacing);
            int gy = gridY + (i / this.gridCols) * (this.cellH + cellSpacing);
            if (mx >= gx && mx <= gx + this.cellW && my >= gy && my <= gy + this.cellH) {
                long now = System.currentTimeMillis();
                if (i == lastClickedItemIndex && (now - lastClickTimeMs) <= 350) {
                    sendBuy(filtered.get(i), false);
                    lastClickedItemIndex = -1;
                    lastClickTimeMs = 0L;
                } else {
                    selectedItem = filtered.get(i);
                    lastClickedItemIndex = i;
                    lastClickTimeMs = now;
                }
                return true;
            }
        }

        int gridWidth = this.gridCols * (this.cellW + 12) - 12;
        int rightX = gridX + gridWidth + 16;
        int buyY = gridY + 180;
        if (mx >= rightX && mx <= rightX + 160 && my >= buyY && my <= buyY + 24) { sendBuy(selectedItem, false); return true; }
        if (mx >= rightX && mx <= rightX + 160 && my >= buyY + 30 && my <= buyY + 54) { sendBuy(selectedItem, true); return true; }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Simple dim background (no blur) to avoid conflicts with UI mods
        g.fill(0, 0, this.width, this.height, 0xD0101216);

        var font = Minecraft.getInstance().font;
        var player = Minecraft.getInstance().player;
        int credits = player == null ? 0 : EconomyData.getCredits(player);
        g.drawString(font, "Credits: " + credits, PADDING, PADDING, 0xFFE0E6EB, false);
        g.drawString(font, RoundState.isBuyPhase() ? "Buy Phase" : "Not Buy Phase", PADDING + 160, PADDING, 0xFF95A5A6, false);

        // Left categories panel
        int cx = PADDING;
        int cy = PADDING + 40;
        int cw = 140, ch = 26;
        drawPanel(g, cx - 6, cy - 10, cw + 12, this.height - cy - 20, 0x9020282E);
        ShopItem.Category[] cats = ShopItem.Category.values();
        for (int i = 0; i < cats.length; i++) {
            int y = cy + i * (ch + 6);
            boolean sel = cats[i] == selectedCategory;
            drawChip(g, cx, y, cw, ch, sel);
            g.drawString(font, cats[i].label, cx + 10, y + 8, sel ? 0xFF101418 : 0xFFE0E6EB, false);
        }

        // Center items grid
        int gridX = cx + cw + 20;
        int gridY = PADDING + 40;
        int cellSpacing = 12;
        int gridWidth = this.gridCols * (this.cellW + cellSpacing) - cellSpacing;
        drawPanel(g, gridX - 10, gridY - 12, gridWidth + 20, this.height - gridY - 20, 0x90181D22);
        for (int i = 0; i < filtered.size(); i++) {
            ShopItem it = filtered.get(i);
            int gx = gridX + (i % this.gridCols) * (this.cellW + cellSpacing);
            int gy = gridY + (i / this.gridCols) * (this.cellH + cellSpacing);
            boolean sel = it == selectedItem;
            drawCard(g, gx, gy, this.cellW, this.cellH, sel);
            if (it.category == ShopItem.Category.ARMOR) {
                // Draw custom armor icon
                g.blit(ARMOR_ICON, gx + 8, gy + 6, 0, 0, 32, 32, 32, 32);
            } else {
                ItemStack icon = it.giveStack();
                g.renderItem(icon, gx + 8, gy + 10);
            }
            g.drawString(font, it.displayName, gx + 32, gy + 12, 0xFFFFFFFF, false);
            g.drawString(font, "$" + it.price, gx + 32, gy + 30, 0xFFB8E986, false);
        }

        // Right detail panel
        int rightX = gridX + gridWidth + 16;
        drawPanel(g, rightX, gridY - 12, 200, this.height - gridY - 20, 0x90181D22);
        g.drawString(font, selectedItem.displayName, rightX + 12, gridY, 0xFFFFFFFF, false);
        g.drawString(font, "Price: $" + selectedItem.price, rightX + 12, gridY + 18, 0xFFB8E986, false);
        if (selectedItem.category == ShopItem.Category.ARMOR) {
            g.blit(ARMOR_ICON, rightX + 12, gridY + 36, 0, 0, 48, 48, 48, 48);
        } else {
            g.renderItem(selectedItem.giveStack(), rightX + 12, gridY + 36);
        }

        // Buy / Sell buttons (stylized)
        int buyY = gridY + 180;
        drawButton(g, rightX, buyY, 160, 24, 0xFF2ECC71, "BUY");
        drawButton(g, rightX, buyY + 30, 160, 24, 0xFF7F8C8D, "SELL");
        super.render(g, mouseX, mouseY, partialTick);
    }

    private static void drawPanel(GuiGraphics g, int x, int y, int w, int h, int bg) {
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 2, 0x40FFFFFF);
        g.fill(x, y + h - 2, x + w, y + h, 0x20000000);
    }

    private static void drawChip(GuiGraphics g, int x, int y, int w, int h, boolean selected) {
        int bg = selected ? 0xFF2ECC71 : 0xFF2A3137;
        g.fill(x, y, x + w, y + h, bg);
    }

    private static void drawCard(GuiGraphics g, int x, int y, int w, int h, boolean selected) {
        int bg = selected ? 0xFF22313C : 0xFF1B232A;
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, 0x30FFFFFF);
    }

    private static void drawButton(GuiGraphics g, int x, int y, int w, int h, int color, String label) {
        g.fill(x, y, x + w, y + h, color);
        var font = Minecraft.getInstance().font;
        g.drawCenteredString(font, Component.literal(label), x + w / 2, y + 7, 0xFFFaFaFa);
    }
}

