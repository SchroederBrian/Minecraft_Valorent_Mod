package com.bobby.valorant.ability.effects;

import com.bobby.valorant.ability.AbilityUseContext;
import net.minecraft.server.level.ServerPlayer;

public final class AbilityEffects {
    private AbilityEffects() {}

    public static void phoenixBlaze(ServerPlayer player, AbilityUseContext ctx) {
        equipSelected(player, new net.minecraft.world.item.ItemStack(com.bobby.valorant.registry.ModItems.WALLSEGMENT.get()));
    }

    public static void phoenixHotHands(ServerPlayer player, AbilityUseContext ctx) {
        equipSelected(player, new net.minecraft.world.item.ItemStack(com.bobby.valorant.registry.ModItems.FIREBALL.get()));
    }

    public static void phoenixCurveball(ServerPlayer player, AbilityUseContext ctx) {
        equipSelected(player, new net.minecraft.world.item.ItemStack(com.bobby.valorant.registry.ModItems.CURVEBALL.get()));
    }

    public static void phoenixRunItBack(ServerPlayer player, AbilityUseContext ctx) {
        // TODO: mark position and handle respawn
    }

    public static void razeBlastPack(ServerPlayer player, AbilityUseContext ctx) {
        equipSelected(player, new net.minecraft.world.item.ItemStack(com.bobby.valorant.registry.ModItems.BLAST_PACK.get()));
    }

    public static void brimStimBeacon(ServerPlayer player, AbilityUseContext ctx) {
        equipSelected(player, new net.minecraft.world.item.ItemStack(com.bobby.valorant.registry.ModItems.STIMBEACONHAND.get()));
    }

    private static void equipSelected(ServerPlayer player, net.minecraft.world.item.ItemStack toEquip) {
        var inv = player.getInventory();
        try {
            var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            int slot = field.getInt(inv);
            com.bobby.valorant.player.AbilityEquipData.saveSelectedSlot(player, slot);
            net.minecraft.world.item.ItemStack current = inv.getItem(slot).copy();
            com.bobby.valorant.player.AbilityEquipData.saveCurrent(player, current);
            inv.setItem(slot, toEquip);
            inv.setChanged();
        } catch (Exception e) {
            com.bobby.valorant.player.AbilityEquipData.saveSelectedSlot(player, 0);
            net.minecraft.world.item.ItemStack current = inv.getItem(0).copy();
            com.bobby.valorant.player.AbilityEquipData.saveCurrent(player, current);
            inv.setItem(0, toEquip);
            inv.setChanged();
        }
    }
}


