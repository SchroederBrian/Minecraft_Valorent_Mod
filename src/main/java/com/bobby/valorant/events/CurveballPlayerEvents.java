package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.CurveballData;
import com.bobby.valorant.registry.ModItems;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class CurveballPlayerEvents {
    private CurveballPlayerEvents() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        CurveballData.ensureInitialized(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        Player original = event.getOriginal();
        Player clone = event.getEntity();
        CurveballData.copy(original, clone);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity entity = event.getSource().getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        if (!hasCurveball(player)) {
            return;
        }
        CurveballData.addKillProgress(player);
    }

    private static boolean hasCurveball(ServerPlayer player) {
        return player.getInventory().contains(ModItems.CURVEBALL.get().getDefaultInstance());
    }
}

