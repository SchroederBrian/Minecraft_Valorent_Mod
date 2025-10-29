package com.bobby.valorant.client;

import com.bobby.valorant.economy.EconomyData;
import com.bobby.valorant.economy.ShopItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BuyScreen extends Screen {
    private static final int PADDING = 14;
    private ShopItem.Category selectedCategory = ShopItem.Category.SIDEARM;
    private ShopItem selectedItem = ShopItem.SIDEARM_P200;
    private final List<ShopItem> filtered = new ArrayList<>();

    public BuyScreen() { super(Component.literal("Shop")); }

    @Override
    protected void init() {
        filter();
    }

    private void filter() {
        filtered.clear();
        for (ShopItem it : ShopItem.values()) if (it.category == selectedCategory) filtered.add(it);
        if (!filtered.contains(selectedItem) && !filtered.isEmpty()) selectedItem = filtered.get(0);
    }

    private void sendBuy(ShopItem item, boolean sell) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;
        String cmd = sell ? ("valorant round sell " + item.name()) : ("valorant round buy " + item.name());
        player.connection.sendCommand(cmd);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Left categories
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
        // Center items grid
        int gridX = cx + cw + 20;
        int gridY = PADDING + 40;
        int cellW = 170, cellH = 60, cols = 3;
        for (int i = 0; i < filtered.size(); i++) {
            int gx = gridX + (i % cols) * (cellW + 12);
            int gy = gridY + (i / cols) * (cellH + 12);
            if (mx >= gx && mx <= gx + cellW && my >= gy && my <= gy + cellH) {
                selectedItem = filtered.get(i);
                return true;
            }
        }
        // Right panel buy/sell buttons
        int rightX = gridX + cols * (cellW + 12) + 16;
        int buyY = gridY + 180;
        if (mx >= rightX && mx <= rightX + 160 && my >= buyY && my <= buyY + 24) { sendBuy(selectedItem, false); return true; }
        if (mx >= rightX && mx <= rightX + 160 && my >= buyY + 30 && my <= buyY + 54) { sendBuy(selectedItem, true); return true; }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Modern dim background
        g.fill(0, 0, this.width, this.height, 0xD0101216);

        var font = Minecraft.getInstance().font;
        var player = Minecraft.getInstance().player;
        int credits = player == null ? 0 : EconomyData.getCredits(player);
        g.drawString(font, "Credits: " + credits, PADDING, PADDING, 0xFFE0E6EB, false);
        g.drawString(font, "Buy Phase — leaving spawn locks purchases", PADDING + 160, PADDING, 0xFF95A5A6, false);

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
        int cellW = 170, cellH = 60, cols = 3;
        drawPanel(g, gridX - 10, gridY - 12, cols * (cellW + 12) - 12 + 20, this.height - gridY - 20, 0x90181D22);
        for (int i = 0; i < filtered.size(); i++) {
            ShopItem it = filtered.get(i);
            int gx = gridX + (i % cols) * (cellW + 12);
            int gy = gridY + (i / cols) * (cellH + 12);
            boolean sel = it == selectedItem;
            drawCard(g, gx, gy, cellW, cellH, sel);
            // icon
            ItemStack icon = it.giveStack();
            g.renderItem(icon, gx + 8, gy + 10);
            // text
            g.drawString(font, it.displayName, gx + 32, gy + 12, 0xFFFFFFFF, false);
            g.drawString(font, "$" + it.price, gx + 32, gy + 30, 0xFFB8E986, false);
        }

        // Right detail panel
        int rightX = gridX + cols * (cellW + 12) + 16;
        drawPanel(g, rightX, gridY - 12, 200, this.height - gridY - 20, 0x90181D22);
        g.drawString(font, selectedItem.displayName, rightX + 12, gridY, 0xFFFFFFFF, false);
        g.drawString(font, "Price: $" + selectedItem.price, rightX + 12, gridY + 18, 0xFFB8E986, false);
        g.renderItem(selectedItem.giveStack(), rightX + 12, gridY + 36);

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
        g.drawCenteredString(font, Component.literal(label), x + w / 2, y + 7, 0xFF101418);
    }
}


