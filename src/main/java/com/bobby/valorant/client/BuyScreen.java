package com.bobby.valorant.client;

import com.bobby.valorant.economy.ShopItem;
import com.bobby.valorant.network.BuyRequestPacket;
import com.bobby.valorant.round.RoundState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class BuyScreen extends Screen {
    private static final int PADDING = 8;
    private int scrollY = 0;

    public BuyScreen() {
        super(Component.literal("Buy Menu"));
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();
        if (minecraft == null) return;
        int y = PADDING + 24; // leave space for header
        int x = PADDING;
        ShopItem.Category current = null;
        for (ShopItem item : ShopItem.values()) {
            if (item.category != current) {
                current = item.category;
                // Category label as disabled button for simple layout
                Button title = Button.builder(Component.literal("== " + current.label + " =="), b -> {})
                        .pos(x, y).size(220, 16).build();
                title.active = false;
                addRenderableWidget(title);
                y += 20;
            }
            String label = item.displayName + " - " + item.price;
            Button buy = Button.builder(Component.literal("Buy: " + label), b -> sendBuy(item))
                    .pos(x, y).size(180, 18).build();
            addRenderableWidget(buy);

            Button sell = Button.builder(Component.literal("Sell"), b -> sendSell(item))
                    .pos(x + 184, y).size(56, 18).build();
            addRenderableWidget(sell);
            y += 22;
        }
    }

    private void sendBuy(ShopItem item) {
        if (!RoundState.isBuyPhase()) return;
        ClientPacketDistributor.sendToServer(new BuyRequestPacket(item.name(), false));
    }

    private void sendSell(ShopItem item) {
        if (!RoundState.isBuyPhase()) return;
        ClientPacketDistributor.sendToServer(new BuyRequestPacket(item.name(), true));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int credits = getClientCredits();
        graphics.drawString(font, "CREDITS: " + credits, PADDING, PADDING, 0xFFFFFF, false);
        graphics.drawString(font, RoundState.isBuyPhase() ? "BUY PHASE" : "NOT BUY PHASE", PADDING + 140, PADDING, 0xAAAAAA, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private int getClientCredits() {
        var player = Minecraft.getInstance().player;
        if (player == null) return 0;
        var root = player.getPersistentData();
        var econ = root.getCompound("ValorantEconomy");
        return econ.getIntOr("Credits", 0);
    }
}

