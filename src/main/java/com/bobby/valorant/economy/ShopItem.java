package com.bobby.valorant.economy;

import com.bobby.valorant.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum ShopItem {
    // Sidearms
    SIDEARM_CLASSIC(Category.SIDEARM, "Classic", 0, Slot.SECONDARY) { public ItemStack giveStack() { return new ItemStack(ModItems.CLASSIC.get()); }},
    SIDEARM_SHORTY(Category.SIDEARM, "Shorty", 300, Slot.SECONDARY) { public ItemStack giveStack() { return new ItemStack(Items.WOODEN_SWORD); }},
    SIDEARM_FRENZY(Category.SIDEARM, "Frenzy", 450, Slot.SECONDARY) { public ItemStack giveStack() { return new ItemStack(Items.GOLDEN_SWORD); }},
    SIDEARM_GHOST(Category.SIDEARM, "Ghost", 500, Slot.SECONDARY) { public ItemStack giveStack() { return new ItemStack(ModItems.GHOST.get()); }},
    SIDEARM_SHERIFF(Category.SIDEARM, "Sheriff", 800, Slot.SECONDARY) { public ItemStack giveStack() { return new ItemStack(Items.IRON_SWORD); }},

    // SMGs
    SMG_STINGER(Category.SMG, "Stinger", 1100, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.CROSSBOW); }},
    SMG_SPECTRE(Category.SMG, "Spectre", 1600, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.CROSSBOW); }},

    // Shotguns
    SHOTGUN_BUCKY(Category.SHOTGUN, "Bucky", 850, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.IRON_AXE); }},
    SHOTGUN_JUDGE(Category.SHOTGUN, "Judge", 1850, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.DIAMOND_AXE); }},

    // Rifles
    RIFLE_BULLDOG(Category.RIFLE, "Bulldog", 2050, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.BOW); }},
    RIFLE_GUARDIAN(Category.RIFLE, "Guardian", 2400, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.BOW); }},
    RIFLE_PHANTOM(Category.RIFLE, "Phantom", 2900, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.BOW); }},
    RIFLE_VANDAL(Category.RIFLE, "Vandal", 2900, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(ModItems.VANDAL_RIFLE.get()); }},

    // Snipers
    SNIPER_MARSHAL(Category.SNIPER, "Marshal", 950, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.TRIDENT); }},
    SNIPER_OUTLAW(Category.SNIPER, "Outlaw", 2400, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.TRIDENT); }},
    SNIPER_OPERATOR(Category.SNIPER, "Operator", 4500, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.TRIDENT); }},

    // Heavy
    HEAVY_ARES(Category.HEAVY, "Ares", 1600, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.NETHERITE_AXE); }},
    HEAVY_ODIN(Category.HEAVY, "Odin", 3200, Slot.PRIMARY) { public ItemStack giveStack() { return new ItemStack(Items.NETHERITE_AXE); }},

    // Armor
    ARMOR_LIGHT(Category.ARMOR, "Light Shields", 400, Slot.ARMOR) { public ItemStack giveStack() { return new ItemStack(ModItems.SMALLARMOR.get()); }},
    ARMOR_HEAVY(Category.ARMOR, "Heavy Shields", 1000, Slot.ARMOR) { public ItemStack giveStack() { return new ItemStack(ModItems.BIGARMOR.get()); }},
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


