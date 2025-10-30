package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class RespawnClientEvents {
    private RespawnClientEvents() {}

    @SubscribeEvent
    public static void onOpenScreen(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof DeathScreen)) return;
        // Prevent the vanilla death screen from showing
        event.setNewScreen(null);
        event.setCanceled(true);

        // Immediately request respawn from server
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
        }
    }
}
