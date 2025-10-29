package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.BuyScreen;
import com.bobby.valorant.client.ModKeyBindings;
import com.bobby.valorant.round.RoundState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class ShopClientEvents {
    private ShopClientEvents() {}

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (ModKeyBindings.OPEN_SHOP != null && ModKeyBindings.OPEN_SHOP.consumeClick()) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            var player = mc.player;
            if (player == null) return;
            if (RoundState.isBuyPhase()) {
                mc.setScreen(new BuyScreen());
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("Shop only in Buy Phase"), true);
            }
        }
    }
}


