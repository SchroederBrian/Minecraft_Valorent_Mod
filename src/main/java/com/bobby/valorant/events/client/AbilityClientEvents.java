package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.ability.Ability;
import com.bobby.valorant.client.ModKeyBindings;
import com.bobby.valorant.network.UseAbilityC2SPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class AbilityClientEvents {
    private AbilityClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;

        if (ModKeyBindings.USE_ABILITY_3.consumeClick()) {
            ClientPacketDistributor.sendToServer(new UseAbilityC2SPacket(Ability.Slot.C));
        }
        if (ModKeyBindings.USE_ABILITY_1.consumeClick()) {
            ClientPacketDistributor.sendToServer(new UseAbilityC2SPacket(Ability.Slot.Q));
        }
        if (ModKeyBindings.USE_ABILITY_2.consumeClick()) {
            ClientPacketDistributor.sendToServer(new UseAbilityC2SPacket(Ability.Slot.E));
        }
        if (ModKeyBindings.USE_ULTIMATE.consumeClick()) {
            ClientPacketDistributor.sendToServer(new UseAbilityC2SPacket(Ability.Slot.X));
        }
    }
}


