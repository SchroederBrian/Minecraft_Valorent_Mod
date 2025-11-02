package com.bobby.valorant.events;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.ReloadStateData;
import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.util.SoundManager;
import com.bobby.valorant.world.item.ClassicPistolItem;
import com.bobby.valorant.world.item.GhostPistolItem;
import com.bobby.valorant.world.item.IWeapon;
import com.bobby.valorant.world.item.KnifeItem;
import com.bobby.valorant.world.item.VandalRifleItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Valorant.MODID)
public final class WeaponSwitchEvents {
    private WeaponSwitchEvents() {}

    // Track the last selected weapon for each player to detect weapon switches
    private static final Map<net.minecraft.server.level.ServerPlayer, net.minecraft.world.item.Item> lastSelectedWeapons = new WeakHashMap<>();

    // Track which players have knife speed boost applied
    private static final Map<net.minecraft.server.level.ServerPlayer, Boolean> knifeSpeedBoostApplied = new WeakHashMap<>();

    // ResourceLocation for knife speed boost attribute modifier
    private static final ResourceLocation KNIFE_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "knife_speed_boost");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;

        // Get current selected slot and item
            var inventory = serverPlayer.getInventory();
            int selectedSlot;
            try {
                var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
                field.setAccessible(true);
                selectedSlot = field.getInt(inventory);
            } catch (Exception e) {
                selectedSlot = 0;
            }

            var currentItem = inventory.getItem(selectedSlot);
        var currentWeapon = currentItem.getItem();

        // Get previously selected weapon
        var lastWeapon = lastSelectedWeapons.get(serverPlayer);

        // Check if weapon has changed (and is actually a weapon)
        if (currentWeapon != lastWeapon && currentWeapon instanceof IWeapon) {
            // Play equip sound for the newly selected weapon
            playWeaponEquipSound(serverPlayer, currentWeapon);

            // Update the last selected weapon
            lastSelectedWeapons.put(serverPlayer, currentWeapon);
        } else if (lastWeapon != null && !(currentWeapon instanceof IWeapon)) {
            // Clear tracking if no weapon is selected
            lastSelectedWeapons.remove(serverPlayer);
        }

        // Handle knife speed boost
        handleKnifeSpeedBoost(serverPlayer, currentWeapon);

        // Check if player is reloading and has switched weapons
        if (ReloadStateData.isReloading(serverPlayer)) {
            // If the current selected item is not a weapon, cancel reload
            if (currentItem.isEmpty() || !(currentItem.getItem() instanceof IWeapon)) {
                ReloadStateData.cancelReload(serverPlayer);
            }
        }
    }

    private static void playWeaponEquipSound(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.item.Item weapon) {
        String weaponType = getWeaponTypeName(weapon);
        if (weaponType != null) {
            SoundManager.playWeaponEquipSound(player, weaponType);
        }
    }

    private static String getWeaponTypeName(net.minecraft.world.item.Item weapon) {
        if (weapon instanceof ClassicPistolItem) {
            return "classic";
        } else if (weapon instanceof GhostPistolItem) {
            return "ghost";
        } else if (weapon instanceof VandalRifleItem) {
            return "vandal";
        }
        return null;
    }

    private static void handleKnifeSpeedBoost(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.item.Item currentWeapon) {
        boolean isHoldingKnife = currentWeapon instanceof KnifeItem;
        boolean hasSpeedBoost = knifeSpeedBoostApplied.getOrDefault(player, false);

        if (isHoldingKnife && !hasSpeedBoost) {
            // Apply speed boost
            var movementSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeedAttr != null) {
                double multiplier = Config.COMMON.knifeSpeedMultiplier.get();
                double baseSpeed = movementSpeedAttr.getBaseValue();
                double bonusSpeed = baseSpeed * (multiplier - 1.0D);

                AttributeModifier modifier = new AttributeModifier(
                    KNIFE_SPEED_MODIFIER_ID,
                    bonusSpeed,
                    AttributeModifier.Operation.ADD_VALUE
                );

                movementSpeedAttr.addTransientModifier(modifier);
                knifeSpeedBoostApplied.put(player, true);
            }
        } else if (!isHoldingKnife && hasSpeedBoost) {
            // Remove speed boost
            var movementSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeedAttr != null) {
                // Create a dummy modifier with the same UUID to remove the existing one
                AttributeModifier dummyModifier = new AttributeModifier(
                    KNIFE_SPEED_MODIFIER_ID,
                    0.0D,
                    AttributeModifier.Operation.ADD_VALUE
                );
                movementSpeedAttr.removeModifier(dummyModifier);
                knifeSpeedBoostApplied.put(player, false);
            }
        }
    }
}
