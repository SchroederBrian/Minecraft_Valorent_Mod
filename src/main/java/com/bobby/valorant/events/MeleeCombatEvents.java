package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.MeleeWeapon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class MeleeCombatEvents {
    private MeleeCombatEvents() {}

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getSide() != LogicalSide.SERVER) return;
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        ItemStack main = sp.getMainHandItem();
        ItemStack off = sp.getOffhandItem();
        if (main.getItem() instanceof MeleeWeapon melee) {
            event.setCanceled(true);
            melee.performLightAttack(sp, InteractionHand.MAIN_HAND, main);
        } else if (off.getItem() instanceof MeleeWeapon melee) {
            event.setCanceled(true);
            melee.performLightAttack(sp, InteractionHand.OFF_HAND, off);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (sp.level().isClientSide) return;
        ItemStack main = sp.getMainHandItem();
        ItemStack off = sp.getOffhandItem();
        if (main.getItem() instanceof MeleeWeapon melee) {
            event.setCanceled(true);
            melee.performLightAttack(sp, InteractionHand.MAIN_HAND, main);
        } else if (off.getItem() instanceof MeleeWeapon melee) {
            event.setCanceled(true);
            melee.performLightAttack(sp, InteractionHand.OFF_HAND, off);
        }
    }
}
