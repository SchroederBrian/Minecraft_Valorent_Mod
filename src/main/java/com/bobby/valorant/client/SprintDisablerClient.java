package com.bobby.valorant.client;

import com.bobby.valorant.Valorant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public class SprintDisablerClient {

    // 2a) Each client tick: unpress sprint key so "hold to sprint" can't stick.
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Options opt = mc.options;
        if (opt != null && opt.keySprint != null) {
            // force key up each tick
            opt.keySprint.setDown(false);
        }
        // Also ensure local sprint flag is off (prevents brief HUD flicker)
        LocalPlayer lp = mc.player;
        if (lp != null && lp.isSprinting()) {
            lp.setSprinting(false);
        }
        // Optional: reset the "double-tap sprint" trigger by briefly nudging forward input.
        // This makes the double-tap window never accumulate.
        if (opt != null && opt.keyUp != null) {
            // Note: resetClickCount might not exist in this version, removing for now
            // opt.keyUp.resetClickCount();
        }
    }

    // 2b) When the sprint key is pressed, consume it so vanilla never toggles sprint.
    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null) return;
        var sprintKey = mc.options.keySprint;
        if (sprintKey == null) return;

        // If this physical key event corresponds to the Sprint key mapping, eat it.
        if (sprintKey.matches(event.getKey(), event.getScanCode())) {
            // And make sure the mapping is up = false
            sprintKey.setDown(false);
        }
    }
}
