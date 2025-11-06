package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.MeleeWeapon;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class AttackInputBlocker {

    private AttackInputBlocker() {}

    // Variante A: den ATTACK-KeyMapping selbst canceln (sicherster Weg)
    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Check if holding a custom weapon (knife or gun) - disable vanilla left-click for these
        var heldItem = mc.player.getMainHandItem().getItem();
        boolean holdingCustomWeapon = heldItem instanceof com.bobby.valorant.world.item.KnifeItem ||
                                     heldItem instanceof com.bobby.valorant.world.item.GunItem;

        if (!holdingCustomWeapon) return;

        // ATTACK-KeyMapping? => Event canceln für custom Waffen
        if (event.getKeyMapping() == mc.options.keyAttack) {
            event.setCanceled(true);
            // Animation wird von CustomLeftClickHandler gestartet
        }
    }

    // Left-click blocking is now handled by key mapping cancellation above
    // Mouse events are handled by CustomLeftClickHandler for animation triggering
}
