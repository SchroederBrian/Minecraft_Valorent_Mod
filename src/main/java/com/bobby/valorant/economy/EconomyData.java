package com.bobby.valorant.economy;

import com.bobby.valorant.ability.Abilities;
import com.bobby.valorant.ability.Ability;
import com.bobby.valorant.player.AbilityStateData;
import com.bobby.valorant.player.AgentData;

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
    private static final String ABILITY_PURCHASES = "AbilityPurchases"; // format: id=count,id2=count

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

    public static boolean tryBuyAbility(ServerPlayer sp, Ability ability, int price, int maxCharges, int currentRoundId, boolean isBuyPhase) {
        if (!isBuyPhase) return false;
        if (price < 0) return false;
        int credits = getCredits(sp);
        if (credits < price) return false;
        int cur = AbilityStateData.getCharges(sp, ability);
        if (cur >= maxCharges) return false;
        if (price > 0) setCredits(sp, credits - price);
        AbilityStateData.setCharges(sp, ability, cur + 1);
        // track refundable ability purchases this round
        setRoundId(sp, currentRoundId);
        java.util.Map<String, Integer> map = getAbilityPurchases(sp);
        map.put(ability.id(), map.getOrDefault(ability.id(), 0) + 1);
        setAbilityPurchases(sp, map);
        // sync credits and ability state snapshot
        syncCredits(sp);
        syncAbilityState(sp);
        return true;
    }

    public static boolean trySellAbility(ServerPlayer sp, Ability ability, int price, int currentRoundId, boolean isBuyPhase) {
        if (!isBuyPhase) return false;
        if (getRoundId(sp) != currentRoundId) return false;
        java.util.Map<String, Integer> map = getAbilityPurchases(sp);
        Integer count = map.get(ability.id());
        if (count == null || count <= 0) return false;
        int cur = AbilityStateData.getCharges(sp, ability);
        if (cur <= 0) return false;
        AbilityStateData.setCharges(sp, ability, Math.max(0, cur - 1));
        if (price > 0) setCredits(sp, getCredits(sp) + price);
        if (count == 1) map.remove(ability.id()); else map.put(ability.id(), count - 1);
        setAbilityPurchases(sp, map);
        syncCredits(sp);
        syncAbilityState(sp);
        return true;
    }

    private static void syncAbilityState(ServerPlayer sp) {
        var agent = AgentData.getSelectedAgent(sp);
        var set = Abilities.getForAgent(agent);
        int c = set.c() != null ? AbilityStateData.getCharges(sp, set.c()) : 0;
        int q = set.q() != null ? AbilityStateData.getCharges(sp, set.q()) : 0;
        int e = set.e() != null ? AbilityStateData.getCharges(sp, set.e()) : 0;
        int x = AbilityStateData.getUltPoints(sp);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new com.bobby.valorant.network.SyncAbilityStateS2CPacket(c, q, e, x));
    }

    private static java.util.Map<String, Integer> getAbilityPurchases(Player p) {
        String s = root(p).getStringOr(ABILITY_PURCHASES, "");
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        if (!s.isEmpty()) {
            String[] parts = s.split(",");
            for (String part : parts) {
                if (part.isEmpty()) continue;
                int eq = part.indexOf('=');
                if (eq <= 0) continue;
                String id = part.substring(0, eq);
                try {
                    int count = Integer.parseInt(part.substring(eq + 1));
                    if (count > 0) map.put(id, count);
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }

    private static void setAbilityPurchases(Player p, java.util.Map<String, Integer> map) {
        if (map.isEmpty()) {
            root(p).putString(ABILITY_PURCHASES, "");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, Integer> e : map.entrySet()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(e.getKey()).append('=').append(Math.max(0, e.getValue()));
        }
        root(p).putString(ABILITY_PURCHASES, sb.toString());
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
            case SECONDARY -> s.is(com.bobby.valorant.registry.ModItems.CLASSIC.get()) || s.is(com.bobby.valorant.registry.ModItems.GHOST.get()) || s.is(Items.IRON_SWORD);
            case PRIMARY -> s.is(Items.CROSSBOW) || s.is(Items.IRON_AXE) || s.is(Items.BOW) || s.is(Items.TRIDENT)
                    || s.is(com.bobby.valorant.registry.ModItems.VANDAL_RIFLE.get());
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


