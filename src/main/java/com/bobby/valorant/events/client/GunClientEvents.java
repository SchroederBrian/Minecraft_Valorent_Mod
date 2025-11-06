package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.ShootGunPacket;
import com.bobby.valorant.world.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class GunClientEvents {
    private GunClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null) return;

        // Only act if attack key is held
        if (!mc.options.keyAttack.isDown()) return;

        ItemStack held = mc.player.getMainHandItem();
        if (!(held.getItem() instanceof GunItem gun)) return;

        // Only automatic weapons should repeat while held
        if (!gun.isAutomatic()) return;

        // Respect reload and cooldown states
        if (com.bobby.valorant.player.ReloadStateData.isReloading(mc.player)) return;
        if (com.bobby.valorant.player.GunCooldownStateData.isOnCooldown(mc.player)) return;

        // Optional: check magazine to avoid useless packets
        if (com.bobby.valorant.world.item.WeaponAmmoData.getCurrentAmmo(held) <= 0) return;

        // Send fire request and start client cooldown prediction using automatic fire rate
        ClientPacketDistributor.sendToServer(new ShootGunPacket());
        int automaticFireRateTicks = gun.getAutomaticFireRateTicks();
        com.bobby.valorant.player.GunCooldownStateData.startCooldown(mc.player, automaticFireRateTicks);
    }
}

