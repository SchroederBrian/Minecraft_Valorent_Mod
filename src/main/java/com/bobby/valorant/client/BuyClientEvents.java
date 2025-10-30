package com.bobby.valorant.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.round.RoundState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import com.mojang.blaze3d.platform.InputConstants;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class BuyClientEvents {
    private BuyClientEvents() {}

    private static final KeyMapping OPEN_BUY = new KeyMapping(
            "key.valorant.open_buy",
            InputConstants.KEY_B,
            "key.categories.inventory");

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_BUY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        while (OPEN_BUY.consumeClick()) {
            if (RoundState.isBuyPhase()) {
                mc.setScreen(new BuyScreen());
            }
        }
    }
}


