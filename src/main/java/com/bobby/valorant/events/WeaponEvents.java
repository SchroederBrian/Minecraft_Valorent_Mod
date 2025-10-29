package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class WeaponEvents {
    private WeaponEvents() {}

    @SubscribeEvent
    public static void onDeathRemoveWeapons(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        // Find first primary (rifle-like) to drop manually
        ItemStack toDrop = ItemStack.EMPTY;
        int dropSlot = -1;
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            if (isPrimaryWeapon(s)) { toDrop = s.copy(); dropSlot = i; break; }
        }
        if (!toDrop.isEmpty()) {
            ItemEntity ent = new ItemEntity(sp.level(), sp.getX(), sp.getY() + 0.5, sp.getZ(), toDrop.copyWithCount(1));
            sp.level().addFreshEntity(ent);
            // remove from inventory so it doesn't also persist
            if (dropSlot >= 0) sp.getInventory().setItem(dropSlot, ItemStack.EMPTY);
        }
        // Remove purchased weapons; keep knife
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(ModItems.KNIFE.get())) continue;
            // Consider anything that matches primary/secondary set to be removable
            boolean isPrimary = isPrimaryWeapon(s);
            boolean isSecondary = s.is(ModItems.GHOST.get());
            if (isPrimary || isSecondary) {
                sp.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
        // Ensure knife present
        ensureKnife(sp);
    }

    @SubscribeEvent
    public static void onCloneGiveKnife(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        Player p = event.getEntity();
        if (p instanceof ServerPlayer sp) {
            ensureKnife(sp);
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            // Remove all default drops; we already spawned the rifle in onDeathRemoveWeapons
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onXp(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            event.setDroppedExperience(0);
        }
    }

    private static void ensureKnife(ServerPlayer sp) {
        if (!sp.getInventory().contains(ModItems.KNIFE.get().getDefaultInstance())) {
            sp.getInventory().add(ModItems.KNIFE.get().getDefaultInstance());
            sp.containerMenu.broadcastChanges();
        }
    }

    private static boolean isPrimaryWeapon(ItemStack s) {
        return s.is(ModItems.VALOR_RIFLE.get()) ||
                s.is(net.minecraft.world.item.Items.BOW) ||
                s.is(net.minecraft.world.item.Items.TRIDENT) ||
                s.is(net.minecraft.world.item.Items.IRON_AXE) ||
                s.is(net.minecraft.world.item.Items.NETHERITE_AXE);
    }
}


