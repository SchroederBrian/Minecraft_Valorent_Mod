package com.bobby.valorant.registry;

import com.bobby.valorant.Valorant;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Valorant.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VALORANT_GEAR = TABS.register("valorant",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.valorant.valorant"))
                    .icon(() -> ModItems.CURVEBALL.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(ModItems.CURVEBALL.get().getDefaultInstance()))
                    .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}

