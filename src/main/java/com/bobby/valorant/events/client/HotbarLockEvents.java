
package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.RemoveCurveballPacket;
import com.bobby.valorant.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public class HotbarLockEvents {

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack heldItem = mc.player.getMainHandItem();
            if (heldItem.is(ModItems.CURVEBALL.get())) {
                ClientPacketDistributor.sendToServer(new RemoveCurveballPacket());
                event.setCanceled(true);
            } else if (heldItem.is(ModItems.WALLSEGMENT.get())) {
                ClientPacketDistributor.sendToServer(new com.bobby.valorant.network.RemoveFireWallPacket());
                event.setCanceled(true);
            } else if (heldItem.is(ModItems.FIREBALL.get())) {
                ClientPacketDistributor.sendToServer(new com.bobby.valorant.network.RemoveFireballPacket());
                event.setCanceled(true);
            } else if (heldItem.is(ModItems.BLAST_PACK.get())) {
                ClientPacketDistributor.sendToServer(new com.bobby.valorant.network.RemoveBlastPackPacket());
                event.setCanceled(true);
            } else if (heldItem.is(ModItems.STIMBEACONHAND.get())) {
                ClientPacketDistributor.sendToServer(new com.bobby.valorant.network.RemoveStimBeaconPacket());
                event.setCanceled(true);
            }
        }
    }

    // No longer force-lock the hotbar slot; weapon cycling handled elsewhere
}
