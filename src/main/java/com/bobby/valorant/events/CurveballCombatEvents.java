package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.CurveballItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.util.TriState;

@EventBusSubscriber(modid = Valorant.MODID)
public final class CurveballCombatEvents {
    private CurveballCombatEvents() {}

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getSide() != LogicalSide.SERVER) {
            return;
        }
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack stack = serverPlayer.getMainHandItem();
        if (!(stack.getItem() instanceof CurveballItem curveball)) {
            return;
        }
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
        event.setCanceled(true);
        curveball.tryThrow(serverPlayer, InteractionHand.MAIN_HAND, stack, CurveballItem.TurnDirection.LEFT);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (serverPlayer.level().isClientSide) {
            return;
        }
        ItemStack stack = serverPlayer.getMainHandItem();
        if (!(stack.getItem() instanceof CurveballItem curveball)) {
            return;
        }
        event.setCanceled(true);
        curveball.tryThrow(serverPlayer, InteractionHand.MAIN_HAND, stack, CurveballItem.TurnDirection.LEFT);
    }
}

