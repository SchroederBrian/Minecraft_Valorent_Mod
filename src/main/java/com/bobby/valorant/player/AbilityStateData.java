package com.bobby.valorant.player;

import com.bobby.valorant.ability.Ability;
import com.bobby.valorant.ability.Abilities;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public final class AbilityStateData {
    private static final String ROOT = "ValorantAbilities";
    private static final String CHARGES = "Charges";
    private static final String ULT_POINTS = "UltPoints";

    private AbilityStateData() {}

    public static int getCharges(Player player, Ability ability) {
        CompoundTag a = abilityTag(player, ability.id());
        return a.getIntOr(CHARGES, ability.baseCharges());
    }

    public static void setCharges(Player player, Ability ability, int charges) {
        int safe = Math.max(0, charges);
        CompoundTag a = abilityTag(player, ability.id());
        a.putInt(CHARGES, safe);
    }

    public static boolean tryConsume(Player player, Ability ability) {
        int cur = getCharges(player, ability);
        if (cur <= 0) return false;
        setCharges(player, ability, cur - 1);
        return true;
    }

    public static int getUltPoints(Player player) {
        return root(player).getIntOr(ULT_POINTS, 0);
    }

    public static void setUltPoints(Player player, int points) {
        root(player).putInt(ULT_POINTS, Math.max(0, points));
    }

    public static void initForAgent(Player player, Agent agent) {
        var set = Abilities.getForAgent(agent);
        resetAbility(player, Objects.requireNonNull(set.c()));
        resetAbility(player, Objects.requireNonNull(set.q()));
        resetAbility(player, Objects.requireNonNull(set.e()));
        // Ult points preserved across rounds elsewhere; do not reset here
    }

    public static void copy(Player original, Player clone) {
        CompoundTag originalRoot = root(original).copy();
        clone.getPersistentData().put(ROOT, originalRoot);
    }

    private static void resetAbility(Player player, Ability ability) {
        setCharges(player, ability, ability.baseCharges());
    }

    private static CompoundTag abilityTag(Player player, String abilityId) {
        CompoundTag r = root(player);
        return r.getCompound(abilityId).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            r.put(abilityId, created);
            return created;
        });
    }

    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        return persistent.getCompound(ROOT).orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            persistent.put(ROOT, created);
            return created;
        });
    }
}


