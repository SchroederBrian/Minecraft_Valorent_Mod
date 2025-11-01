package com.bobby.valorant.events;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.render.ReloadAnimationRenderer;
import com.bobby.valorant.player.ReloadStateData;
import com.bobby.valorant.world.item.ClassicPistolItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class HandRenderEvents {
    private HandRenderEvents() {}

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (!Config.COMMON.reloadAnimationEnabled.get()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        // Prüfe ob Spieler eine Classic-Pistole hält
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof ClassicPistolItem)) {
            return;
        }

        // Prüfe ob Reload aktiv ist
        if (!ReloadStateData.isReloading(player)) {
            return;
        }

        // Bestimme welche Hand gerendert wird
        InteractionHand hand = event.getHand();

        if (hand == InteractionHand.MAIN_HAND) {
            // Rechte Hand (Haupthand) - hält die Waffe
            ReloadAnimationRenderer.applyRightHandTransform(event.getPoseStack(), event.getPartialTick());
        } else if (hand == InteractionHand.OFF_HAND) {
            // Linke Hand (Nebenhand) - bewegt sich zum Magazin
            ReloadAnimationRenderer.applyLeftHandTransform(event.getPoseStack(), event.getPartialTick());

            // Rendere das Magazin in der linken Hand während der Animation
            ReloadAnimationRenderer.renderMagazineInLeftHand(event.getPoseStack(),
                                                           event.getMultiBufferSource(),
                                                           event.getPackedLight(),
                                                           event.getPartialTick());
        }
    }
}
