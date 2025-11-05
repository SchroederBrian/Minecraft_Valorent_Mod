package com.bobby.valorant;

import org.slf4j.Logger;

import com.bobby.valorant.registry.ModCreativeTabs;
import com.bobby.valorant.registry.ModEntityTypes;
import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.registry.ModSounds;
import com.bobby.valorant.setup.ModNetworking;
import com.bobby.valorant.fancymenu.FancyMenuVarSync;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import com.bobby.valorant.util.ArmorStandRotator;

@Mod(Valorant.MODID)
public class Valorant {
    public static final String MODID = "valorant";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Valorant(IEventBus modEventBus, ModContainer modContainer) {
        // MOD-Bus (Lifecycle)
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onEntityAttributeCreation);
        modEventBus.addListener(ModNetworking::registerPayloadHandlers);

        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSounds.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);

        // GAME-Bus (Runtime)
        NeoForge.EVENT_BUS.addListener(Valorant::onServerStarted);
        NeoForge.EVENT_BUS.addListener(ArmorStandRotator::onServerTick);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // ggf. Netzwerk/Deferred Tasks etc.
    }

    // === GAME-Bus ===
    private static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "team add A \"Attackers\"");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "team modify A nametagVisibility never");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "team add V \"Defenders\"");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "team modify V nametagVisibility never");
            FancyMenuVarSync.updateAll(server);
        }
    }

    // === MOD-Bus ===
    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.DROPPED_WEAPON_STAND.get(), net.minecraft.world.entity.decoration.ArmorStand.createAttributes().build());
        event.put(ModEntityTypes.CORPSE.get(), net.minecraft.world.entity.decoration.ArmorStand.createAttributes().build());
    }
}
