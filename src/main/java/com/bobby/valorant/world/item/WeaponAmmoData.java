package com.bobby.valorant.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class WeaponAmmoData {
    private WeaponAmmoData() {}

    private static final String TAG_AMMO = "valorant_ammo";
    private static final String TAG_RESERVE = "valorant_reserve";

    public static int getCurrentAmmo(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IWeapon)) {
            return 0;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }
        return customData.copyTag().getInt(TAG_AMMO).orElse(0);
    }

    public static void setCurrentAmmo(ItemStack stack, int ammo) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IWeapon)) {
            return;
        }
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, c -> c.update(tag -> tag.putInt(TAG_AMMO, ammo)));
    }

    public static int getReserveAmmo(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IWeapon)) {
            return 0;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }
        return customData.copyTag().getInt(TAG_RESERVE).orElse(0);
    }

    public static void setReserveAmmo(ItemStack stack, int ammo) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IWeapon)) {
            return;
        }
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, c -> c.update(tag -> tag.putInt(TAG_RESERVE, ammo)));
    }

    public static void decrementAmmo(ItemStack stack) {
        setCurrentAmmo(stack, Math.max(0, getCurrentAmmo(stack) - 1));
    }
}
