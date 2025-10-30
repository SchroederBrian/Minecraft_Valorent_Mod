package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class InteractionAnimationEvents {
    private InteractionAnimationEvents() {}

    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        boolean holdingGun = (main.getItem() instanceof GunItem) || (off.getItem() instanceof GunItem);
        if (!holdingGun) return;

        // Prevent the vanilla swing/animation for guns
        // - Right-click: suppress and cancel (we don't use right-click for guns)
        // - Left-click: suppress swing but DO NOT cancel so attack still proceeds
        if (event.isUseItem()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        } else if (event.isAttack()) {
            event.setSwingHand(false);
        }
    }
}


