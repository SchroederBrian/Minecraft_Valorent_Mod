package com.bobby.valorant;

import com.bobby.valorant.registry.ModEntityTypes;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Valorant.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public class ValorantClient {
    public ValorantClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntityTypes.CURVEBALL_ORB.get(), ThrownItemRenderer::new);
            EntityRenderers.register(ModEntityTypes.FIREBALL.get(), ThrownItemRenderer::new);
            EntityRenderers.register(ModEntityTypes.PLANTED_SPIKE.get(), ThrownItemRenderer::new);
            EntityRenderers.register(ModEntityTypes.DROPPED_WEAPON_STAND.get(), ArmorStandRenderer::new);

            // Optionally unbind vanilla inventory key
            if (Config.COMMON.blockAllVanillaInventories.get()) {
                KeyMapping inv = Minecraft.getInstance().options.keyInventory;
                inv.setKey(InputConstants.UNKNOWN);
                KeyMapping.resetMapping();
            }
        });
    }
}
