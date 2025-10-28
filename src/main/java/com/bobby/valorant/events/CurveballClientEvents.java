package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.ThrowCurveballPacket;
import com.bobby.valorant.world.item.CurveballItem;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.TriState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class CurveballClientEvents {
    private CurveballClientEvents() {}

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (shouldTrigger(event.getItemStack(), event.getEntity())) {
            ClientPacketDistributor.sendToServer(new ThrowCurveballPacket(CurveballItem.TurnDirection.LEFT));
            event.getEntity().swing(InteractionHand.MAIN_HAND, false);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!shouldTrigger(event.getItemStack(), event.getEntity())) {
            return;
        }
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
        event.setCanceled(true);
        stopClientBreaking();
        event.getEntity().swing(InteractionHand.MAIN_HAND, false);
    }

    private static boolean shouldTrigger(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof CurveballItem)) {
            return false;
        }
        return player.getMainHandItem() == stack;
    }

    private static void stopClientBreaking() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null) {
            minecraft.gameMode.stopDestroyBlock();
        }
    }
}

