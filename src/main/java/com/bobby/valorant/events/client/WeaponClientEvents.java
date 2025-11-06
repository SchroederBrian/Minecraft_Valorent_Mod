package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.ModKeyBindings;
import com.bobby.valorant.registry.ModItems;
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

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class WeaponClientEvents {
    private WeaponClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        var conn = mc.getConnection();
        if (player == null || conn == null) return;
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        var conn = mc.getConnection();
        if (player == null || conn == null) return;
        ItemStack held = player.getMainHandItem();
        // If holding ability item, defer to ability logic (it restores previous weapon)
        if (held.is(com.bobby.valorant.registry.ModItems.CURVEBALL.get())) {
            return; // HotbarLockEvents will handle removal and restore
        }
        if (held.is(com.bobby.valorant.registry.ModItems.SKY_SMOKE_ITEM.get())) {
            return; // HotbarLockEvents will handle removal and restore
        }
        int current = getCurrentType(player);
        double dy = event.getScrollDeltaY();
        int next = (current + (dy > 0 ? 2 : 1)) % 3; // cycle opposite direction on positive delta
        int targetSlot = switch (next) {
            case 0 -> findHotbarSlot(player, ModItems.VANDAL_RIFLE.get().getDefaultInstance());
            case 1 -> findHotbarSlot(player, ModItems.GHOST.get().getDefaultInstance());
            default -> findHotbarSlot(player, ModItems.KNIFE.get().getDefaultInstance());
        };
        if (targetSlot >= 0) {
            conn.send(new ServerboundSetCarriedItemPacket(targetSlot));
            event.setCanceled(true);
        }
    }

    private static int getCurrentType(LocalPlayer player) {
        ItemStack s = player.getMainHandItem();
        if (s.is(ModItems.VANDAL_RIFLE.get())) return 0; // rifle
        if (s.is(ModItems.GHOST.get())) return 1; // pistol
        return 2; // knife/other
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


