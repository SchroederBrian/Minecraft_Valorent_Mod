package com.bobby.valorant.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AbilityEquipData {
    private AbilityEquipData() {}

    private static final Map<UUID, ItemStack> SAVED = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SAVED_SLOT = new ConcurrentHashMap<>();

    public static void saveCurrent(Player player, ItemStack current) {
        if (current == null) current = ItemStack.EMPTY;
        SAVED.put(player.getUUID(), current.copy());
    }

    public static ItemStack takeSaved(Player player) {
        ItemStack stack = SAVED.remove(player.getUUID());
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public static void saveSelectedSlot(Player player, int slot) {
        SAVED_SLOT.put(player.getUUID(), slot);
    }

    public static Integer takeSavedSelectedSlot(Player player) {
        return SAVED_SLOT.remove(player.getUUID());
    }
}


