package com.bobby.valorant;

import org.slf4j.Logger;

import com.bobby.valorant.registry.ModCreativeTabs;
import com.bobby.valorant.registry.ModEntityTypes;
import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.registry.ModSounds;
import com.bobby.valorant.setup.ModNetworking;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Valorant.MODID)
public class Valorant {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "valorant";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Valorant(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModNetworking::registerPayloadHandlers);

        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSounds.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }
}
