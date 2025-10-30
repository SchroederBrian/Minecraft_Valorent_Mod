package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.ShootGunPacket;
import com.bobby.valorant.world.item.GunItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class GunClientEvents {
    private GunClientEvents() {}

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof GunItem)) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        ClientPacketDistributor.sendToServer(new ShootGunPacket());
        // Do not swing the hand; InteractionAnimationEvents suppresses it
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof GunItem)) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        // Send packet to server to handle shooting
        ClientPacketDistributor.sendToServer(new ShootGunPacket());
        // We also cancel the event on the server in GunCombatEvents to prevent block breaking
    }
}


