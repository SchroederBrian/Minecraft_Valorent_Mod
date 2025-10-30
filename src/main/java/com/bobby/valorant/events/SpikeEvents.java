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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class SpikeEvents {
    private SpikeEvents() {}

    @SubscribeEvent
    public static void onPlayerDeathDropSpike(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.is(ModItems.SPIKE.get())) {
                ItemEntity ent = new ItemEntity(sp.level(), sp.getX(), sp.getY() + 0.5, sp.getZ(), s.copyWithCount(1));
                sp.level().addFreshEntity(ent);
                sp.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        var server = sp.getServer();
        if (server == null) return;
        var sb = server.getScoreboard();
        var team = sb.getPlayersTeam(sp.getScoreboardName());
        boolean isAttackerTeam = team != null && "A".equals(team.getName());
        if (isAttackerTeam) return;
        // Defender holding Spike -> immediately drop and remove
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.is(ModItems.SPIKE.get())) {
                ItemEntity ent = new ItemEntity(sp.level(), sp.getX(), sp.getY() + 0.5, sp.getZ(), s.copyWithCount(1));
                sp.level().addFreshEntity(ent);
                sp.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }
}


