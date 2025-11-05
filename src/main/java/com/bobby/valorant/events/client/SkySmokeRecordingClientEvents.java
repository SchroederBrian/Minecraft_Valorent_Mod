package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.ModKeyBindings;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class SkySmokeRecordingClientEvents {
    private SkySmokeRecordingClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Check for Sky Smoke recording add point keybinding
        if (ModKeyBindings.SKY_SMOKE_RECORD_POINT.consumeClick()) {
            // Send command to server to add a point if recording is active
            String command = "/valorant area record add";
            mc.player.connection.sendCommand(command.substring(1)); // Remove the leading slash
        }
    }   
}
