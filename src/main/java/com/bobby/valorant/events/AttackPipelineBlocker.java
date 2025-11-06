package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.MeleeWeapon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class AttackPipelineBlocker {

    private AttackPipelineBlocker() {}

    // Block-Abbau per Linksklick blockieren (wichtig: auf dem CLIENT canceln!)
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        // Nur auf Clientseite verhindert man das Senden der Pakete:
        if (event.getLevel().isClientSide()) {
            event.setCanceled(true);
        }
    }

    // Entity-Angriff unterbinden (beide Seiten ok; serverseitig ist maßgeblich)
    // Aber erlaube Angriffe mit Melee-Waffen (werden von MeleeCombatEvents behandelt)
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        // Erlaube Angriffe mit Melee-Waffen - diese werden von MeleeCombatEvents behandelt
        if (!(event.getEntity().getMainHandItem().getItem() instanceof MeleeWeapon)) {
            event.setCanceled(true);
        }
    }
}
