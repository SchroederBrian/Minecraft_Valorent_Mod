package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class WeaponEvents {
    private WeaponEvents() {}

    @SubscribeEvent
    public static void onDeathRemoveWeapons(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        // Remove purchased weapons; keep knife
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(ModItems.KNIFE.get())) continue;
            // Consider anything that matches primary/secondary set to be removable
            boolean isPrimary = s.is(ModItems.VALOR_RIFLE.get());
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

    private static void ensureKnife(ServerPlayer sp) {
        if (!sp.getInventory().contains(ModItems.KNIFE.get().getDefaultInstance())) {
            sp.getInventory().add(ModItems.KNIFE.get().getDefaultInstance());
            sp.containerMenu.broadcastChanges();
        }
    }
}


