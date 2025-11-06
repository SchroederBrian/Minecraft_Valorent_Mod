package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class ArmRenderSuppressor {

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Check if holding a custom weapon (knife or gun) - suppress vanilla arm for these
        var heldItem = mc.player.getMainHandItem().getItem();
        boolean holdingKnife = heldItem instanceof com.bobby.valorant.world.item.KnifeItem;
        boolean holdingGun = heldItem instanceof com.bobby.valorant.world.item.GunItem;

        if (holdingKnife) {
            // For knives, always suppress vanilla arm since they use custom animations
            // This prevents any vanilla swing animations from interfering
            event.setCanceled(true);
        } else if (holdingGun) {
            // For guns, always suppress vanilla arm (they use custom animations)
            event.setCanceled(true);
        }
    }
}
