
package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.RemoveCurveballPacket;
import com.bobby.valorant.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public class HotbarLockEvents {

    private static final int LOCKED_SLOT = 0;

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack heldItem = mc.player.getMainHandItem();
            if (heldItem.is(ModItems.CURVEBALL.get())) {
                ClientPacketDistributor.sendToServer(new RemoveCurveballPacket());
            }
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        var inv = player.getInventory();
        try {
            var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            if (field.getInt(inv) != LOCKED_SLOT) {
                field.setInt(inv, LOCKED_SLOT);
                if (mc.getConnection() != null) {
                    mc.getConnection().send(new ServerboundSetCarriedItemPacket(LOCKED_SLOT));
                }
            }
        } catch (Exception e) {
            // Log error or handle exception
        }
    }
}
