package com.bobby.valorant.events;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.render.ReloadAnimationRenderer;
import com.bobby.valorant.client.render.KnifeAnimationRenderer;
import com.bobby.valorant.client.render.ShootAnimationRenderer;
import com.bobby.valorant.player.ReloadStateData;
import com.bobby.valorant.player.KnifeAnimationStateData;
import com.bobby.valorant.world.item.ClassicPistolItem;
import com.bobby.valorant.world.item.KnifeItem;
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
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        ItemStack heldItem = player.getMainHandItem();
        InteractionHand hand = event.getHand();

        // Handle weapon animations - for both knives and guns
        if (heldItem.getItem() instanceof KnifeItem) {
            // Knife: apply custom knife animations
            KnifeAnimationRenderer.applyKnifeAnimationTransform(event.getPoseStack(), event.getPartialTick());
            return; // Knife animations handled, don't process further

        } else if (heldItem.getItem() instanceof com.bobby.valorant.world.item.GunItem) {
            // Handle reload animations for guns first (higher priority)
            if (Config.COMMON.reloadAnimationEnabled.get() &&
                heldItem.getItem() instanceof ClassicPistolItem &&
                ReloadStateData.isReloading(player)) {

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
                return; // Reload animation handled, don't process shoot animation
            }

            // Apply shoot animation for guns (when not reloading)
            ShootAnimationRenderer.applyShootTransform(
                event.getPoseStack(), event.getPartialTick(), hand, heldItem);
            return; // Gun handling complete
        }
    }
}
