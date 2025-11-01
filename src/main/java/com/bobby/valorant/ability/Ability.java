package com.bobby.valorant.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Supplier;

public final class Ability {
    public enum Slot { C, Q, E, X }

    private final String id;
    private final Slot slot;
    private final Component displayName;
    private final Component description;
    private final Supplier<ItemStack> iconSupplier;
    private final AbilityEffect effect;
    private final int baseCharges;
    private final int cooldownSeconds;
    private final int ultCost;

    private Ability(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.slot = Objects.requireNonNull(builder.slot, "slot");
        this.displayName = Objects.requireNonNull(builder.displayName, "displayName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.iconSupplier = Objects.requireNonNull(builder.iconSupplier, "iconSupplier");
        this.effect = Objects.requireNonNull(builder.effect, "effect");
        this.baseCharges = builder.baseCharges;
        this.cooldownSeconds = builder.cooldownSeconds;
        this.ultCost = builder.ultCost;
    }

    public String id() { return id; }
    public Slot slot() { return slot; }
    public Component displayName() { return displayName; }
    public Component description() { return description; }
    public ItemStack icon() { return iconSupplier.get(); }
    public AbilityEffect effect() { return effect; }
    public int baseCharges() { return baseCharges; }
    public int cooldownSeconds() { return cooldownSeconds; }
    public int ultCost() { return ultCost; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String id;
        private Slot slot;
        private Component displayName = Component.literal("");
        private Component description = Component.empty();
        private Supplier<ItemStack> iconSupplier = () -> ItemStack.EMPTY;
        private AbilityEffect effect = (sp, ctx) -> {};
        private int baseCharges = 0;
        private int cooldownSeconds = 0;
        private int ultCost = 0;

        public Builder id(String id) { this.id = id; return this; }
        public Builder slot(Slot slot) { this.slot = slot; return this; }
        public Builder displayName(Component name) { this.displayName = name; return this; }
        public Builder description(Component desc) { this.description = desc; return this; }
        public Builder iconSupplier(Supplier<ItemStack> sup) { this.iconSupplier = sup; return this; }
        public Builder effect(AbilityEffect effect) { this.effect = effect; return this; }
        public Builder baseCharges(int charges) { this.baseCharges = charges; return this; }
        public Builder cooldownSeconds(int seconds) { this.cooldownSeconds = seconds; return this; }
        public Builder ultCost(int cost) { this.ultCost = cost; return this; }

        public Ability build() { return new Ability(this); }
    }
}


