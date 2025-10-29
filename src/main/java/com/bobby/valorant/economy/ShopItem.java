package com.bobby.valorant.economy;

import com.bobby.valorant.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum ShopItem {
    // Prices use simple placeholders for MVP
    SIDEARM_P200(Category.SIDEARM, "Classic", 200, Slot.SECONDARY) { public ItemStack giveStack() { return new ItemStack(ModItems.CLASSIC.get()); }},
    SIDEARM_GHOST(Category.SIDEARM, "Ghost", 500, Slot.SECONDARY) { public ItemStack giveStack() { return new ItemStack(ModItems.GHOST.get()); }},

    SMG_STINGER(Category.SMG, "Stinger", 1200, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.CROSSBOW); }},
    SHOTGUN_BULLDOG(Category.SHOTGUN, "Bucky", 1500, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.IRON_AXE); }},
    RIFLE_VANDAL(Category.RIFLE, "Vandal", 2900, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.BOW); }},
    SNIPER_OP(Category.SNIPER, "Operator", 4500, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.TRIDENT); }},
    HEAVY_ARES(Category.HEAVY, "Ares", 1600, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.NETHERITE_AXE); }},
    RIFLE_VALOR(Category.RIFLE, "Valor Rifle", 3000, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(ModItems.VALOR_RIFLE.get()); }},

    ARMOR_LIGHT(Category.ARMOR, "Light Shields", 400, Slot.ARMOR) { public ItemStack giveStack() { return new ItemStack(Items.LEATHER_CHESTPLATE); }},
    ARMOR_HEAVY(Category.ARMOR, "Heavy Shields", 1000, Slot.ARMOR) { public ItemStack giveStack() { return new ItemStack(Items.IRON_CHESTPLATE); }},
    ;

    public enum Slot { PRIMARY, SECONDARY, ARMOR, UTILITY }
    public enum Category {
        SIDEARM("Sidearm"), SMG("SMGs"), RIFLE("Rifles"), SNIPER("Sniper"), HEAVY("Heavy"), SHOTGUN("Shotguns"), ARMOR("Armor"), UTILITY("Abilities");
        public final String label;
        Category(String label) { this.label = label; }
    }

    public final Category category;
    public final String displayName;
    public final int price;
    public final Slot slot;

    ShopItem(Category category, String displayName, int price, Slot slot) {
        this.category = category;
        this.displayName = displayName;
        this.price = price;
        this.slot = slot;
    }

    public abstract ItemStack giveStack();
}


