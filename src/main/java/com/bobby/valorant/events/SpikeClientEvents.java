package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.ModKeyBindings;
import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.network.DefuseSpikePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.bobby.valorant.network.PlantSpikePacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class SpikeClientEvents {
    private SpikeClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        var conn = mc.getConnection();
        if (player == null || conn == null) return;

        if (ModKeyBindings.DROP_SPIKE.consumeClick()) {
            dropSpikeFromInventory(player);
        }

        if (ModKeyBindings.EQUIP_SPIKE_OR_DEFUSE.consumeClick()) {
            // If defender near planted spike, start defuse; otherwise equip spike if owned
            boolean startedDefuse = tryStartDefuse();
            if (!startedDefuse) {
                int slot = findHotbarSlot(player, ModItems.SPIKE.get().getDefaultInstance());
                if (slot >= 0) conn.send(new ServerboundSetCarriedItemPacket(slot));
            }
        }
    }

    private static boolean tryStartDefuse() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return false;
        double r = 1.2D;
        AABB box = new AABB(player.getX() - r, player.getY() - r, player.getZ() - r,
                player.getX() + r, player.getY() + r, player.getZ() + r);
        boolean nearPlanted = !player.level().getEntitiesOfClass(ThrowableItemProjectile.class, box,
                tip -> tip.getItem().is(ModItems.PLANTEDSPIKE.get())).isEmpty();
        if (!nearPlanted) return false;
        ClientPacketDistributor.sendToServer(new DefuseSpikePacket(DefuseSpikePacket.Action.START));
        return true;
    }

    @SubscribeEvent
    public static void onLeftClick(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!event.isAttack()) return;
        ItemStack held = player.getMainHandItem();
        if (!held.is(ModItems.SPIKE.get())) return;
        // Start planting on attack interaction
        ClientPacketDistributor.sendToServer(new PlantSpikePacket(PlantSpikePacket.Action.START));
        event.setCanceled(true);
    }

    private static void dropSpikeFromInventory(LocalPlayer player) {
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(ModItems.SPIKE.get())) {
                player.drop(s.copyWithCount(1), true, false);
                player.getInventory().setItem(i, ItemStack.EMPTY);
                return;
            }
        }
    }

    private static int findHotbarSlot(LocalPlayer player, ItemStack match) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (ItemStack.isSameItemSameComponents(s, match)) return i;
        }
        return -1;
    }
}


