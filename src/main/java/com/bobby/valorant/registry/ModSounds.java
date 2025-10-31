package com.bobby.valorant.registry;

import com.bobby.valorant.Valorant;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Valorant.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> CURVEBALL_THROW = register("curveball_throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> CURVEBALL_DETONATE = register("curveball_detonate");

    // Weapon sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_RELOAD = register("classic.reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOST_RELOAD = register("ghost.reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANDAL_RELOAD = register("vandal.reload");

    // Spike sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIKE_PLANT = register("spike.plant");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIKE_DEFUSE = register("spike.defuse");

    // UI sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> BUY_SUCCESS = register("ui.buy_success");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUY_FAILURE = register("ui.buy_failure");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, path)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}

