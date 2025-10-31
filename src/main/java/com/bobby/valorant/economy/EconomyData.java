package com.bobby.valorant.economy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;

public final class EconomyData {
    private EconomyData() {}

    private static final String ROOT = "ValorantEconomy";
    private static final String CREDITS = "Credits";
    private static final String LOSS_STREAK = "LossStreak";
    private static final String ROUND_ID = "RoundId";
    private static final String PURCHASES = "Purchases"; // comma-list of enum names

    public static int getCredits(Player p) {
        return root(p).getIntOr(CREDITS, 800); // start credits default
    }

    public static void setCredits(Player p, int credits) {
        root(p).putInt(CREDITS, Math.max(0, credits));
    }

    public static int getLossStreak(Player p) { return root(p).getIntOr(LOSS_STREAK, 0); }
    public static void setLossStreak(Player p, int v) { root(p).putInt(LOSS_STREAK, Math.max(0, v)); }

    public static int getRoundId(Player p) { return root(p).getIntOr(ROUND_ID, 0); }
    public static void setRoundId(Player p, int id) { root(p).putInt(ROUND_ID, id); }

    public static Set<String> getPurchases(Player p) {
        String s = root(p).getStringOr(PURCHASES, "");
        Set<String> set = new HashSet<>();
        if (!s.isEmpty()) {
            for (String part : s.split(",")) if (!part.isEmpty()) set.add(part);
        }
        return set;
    }

    public static void setPurchases(Player p, Set<String> items) {
        String s = String.join(",", items);
        root(p).putString(PURCHASES, s);
    }

    public static boolean tryBuy(ServerPlayer sp, ShopItem item, int currentRoundId, boolean isBuyPhase) {
        if (!isBuyPhase) return false;
        int credits = getCredits(sp);
        if (credits < item.price) return false;
        // slot rules: allow only one primary and one secondary; armor replaces
        if (!slotFreeOrReplace(sp, item)) return false;
        // pay
        setCredits(sp, credits - item.price);
        // grant
        grantItem(sp, item);
        // track refundable
        setRoundId(sp, currentRoundId);
        Set<String> p = getPurchases(sp);
        p.add(item.name());
        setPurchases(sp, p);
        // sync to client
        syncCredits(sp);
        return true;
    }

    public static boolean trySell(ServerPlayer sp, ShopItem item, int currentRoundId, boolean isBuyPhase) {
        if (!isBuyPhase) return false;
        if (getRoundId(sp) != currentRoundId) return false; // only this round refundable
        Set<String> p = getPurchases(sp);
        if (!p.remove(item.name())) return false;
        setPurchases(sp, p);
        setCredits(sp, getCredits(sp) + item.price);
        removeItem(sp, item);
        // sync to client
        syncCredits(sp);
        return true;
    }

    private static boolean slotFreeOrReplace(ServerPlayer sp, ShopItem item) {
        return switch (item.slot) {
            case SECONDARY -> !hasSecondaryNonDefault(sp); // allow replacing default pistol
            case PRIMARY -> !hasAny(sp, ShopItem.Slot.PRIMARY);
            case ARMOR -> true; // replaces, we handle in grantItem
            default -> true;
        };
    }

    private static boolean hasAny(ServerPlayer sp, ShopItem.Slot slot) {
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (matchesSlot(s, slot)) return true;
        }
        return false;
    }

    private static boolean matchesSlot(ItemStack s, ShopItem.Slot slot) {
        if (s.isEmpty()) return false;
        return switch (slot) {
            case SECONDARY -> s.is(Items.STONE_SWORD) || s.is(Items.IRON_SWORD) || s.is(com.bobby.valorant.registry.ModItems.GHOST.get());
            case PRIMARY -> s.is(Items.CROSSBOW) || s.is(Items.IRON_AXE) || s.is(Items.BOW) || s.is(Items.TRIDENT)
                    || s.is(com.bobby.valorant.registry.ModItems.VALOR_RIFLE.get());
            case ARMOR -> s.is(Items.LEATHER_CHESTPLATE) || s.is(Items.IRON_CHESTPLATE);
            default -> false;
        };
    }

    private static void grantItem(ServerPlayer sp, ShopItem item) {
        if (item == ShopItem.ARMOR_HEAVY) {
            removeItem(sp, ShopItem.ARMOR_LIGHT);
        }

        switch (item.slot) {
            case PRIMARY:
                // Remove any existing primary weapon before giving the new one.
                removeItemsOfSlot(sp, ShopItem.Slot.PRIMARY);
                sp.getInventory().setItem(0, item.giveStack());
                break;
            case SECONDARY:
                // Remove any existing secondary weapon, including the default pistol.
                removeItemsOfSlot(sp, ShopItem.Slot.SECONDARY);
                sp.getInventory().setItem(1, item.giveStack());
                break;
            default:
                // For other items like armor, add them to the inventory without specific slot placement.
                sp.getInventory().add(item.giveStack());
                break;
        }

        sp.containerMenu.broadcastChanges();
    }

    private static boolean hasSecondaryNonDefault(ServerPlayer sp) {
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            // default pistol is STONE_SWORD; anything else that counts as secondary blocks
            if (s.is(net.minecraft.world.item.Items.STONE_SWORD)) continue;
            if (matchesSlot(s, ShopItem.Slot.SECONDARY)) return true;
        }
        return false;
    }

    private static void removeItemsOfSlot(ServerPlayer sp, ShopItem.Slot slot) {
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (matchesSlot(s, slot)) {
                sp.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private static void removeItem(ServerPlayer sp, ShopItem item) {
        ItemStack target = item.giveStack();
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(s, target)) {
                sp.getInventory().setItem(i, ItemStack.EMPTY);
                break;
            }
        }
        sp.containerMenu.broadcastChanges();
    }

    public static void syncCredits(ServerPlayer sp) {
        int credits = getCredits(sp);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new com.bobby.valorant.network.SyncCreditsPacket(credits));
    }

    private static CompoundTag root(Player p) {
        CompoundTag tag = p.getPersistentData();
        return tag.getCompound(ROOT).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            tag.put(ROOT, created);
            return created;
        });
    }
}


