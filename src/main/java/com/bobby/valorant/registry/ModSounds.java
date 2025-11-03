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

    // Announcer sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNOUNCER_30_SECONDS_LEFT = register("announcer.30_seconds_left");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNOUNCER_10_SECONDS_LEFT = register("announcer.10_seconds_left");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNOUNCER_SPIKE_PLANTED_1 = register("announcer.spike_planted_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNOUNCER_SPIKE_PLANTED_2 = register("announcer.spike_planted_2");

    // Classic weapon sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_RELOAD = register("classic.reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_EQUIP = register("classic.equip");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_SINGLE_SHOT_1 = register("classic.single_shot_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_SINGLE_SHOT_2 = register("classic.single_shot_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_SINGLE_SHOT_3 = register("classic.single_shot_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_SINGLE_SHOT_4 = register("classic.single_shot_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_BURST_1 = register("classic.burst_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_BURST_2 = register("classic.burst_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_BURST_3 = register("classic.burst_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_BURST_4 = register("classic.burst_4");

    // Ghost weapon sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOST_RELOAD = register("ghost.reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOST_EQUIP_1 = register("ghost.equip_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOST_EQUIP_2 = register("ghost.equip_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOST_SHOT_1 = register("ghost.shot_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOST_SHOT_2 = register("ghost.shot_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOST_SHOT_3 = register("ghost.shot_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOST_SHOT_4 = register("ghost.shot_4");

    // Vandal weapon sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> VANDAL_RELOAD = register("vandal.reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANDAL_EQUIP = register("vandal.equip");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANDAL_SHOT_1 = register("vandal.shot_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANDAL_SHOT_2 = register("vandal.shot_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANDAL_SHOT_3 = register("vandal.shot_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANDAL_SHOT_4 = register("vandal.shot_4");

    // Spike sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIKE_PLANT = register("spike.plant");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIKE_DEFUSE = register("spike.defuse");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIKE_COUNTDOWN = register("spike.countdown");

    // UI sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> BUY_SUCCESS = register("ui.buy_success");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUY_FAILURE = register("ui.buy_failure");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_SPIKE_PLANTED = register("ui.spike_planted");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_SPIKE_DEFUSED = register("ui.spike_defused");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_ROUND_START_COUNTDOWN = register("ui.round_start_countdown");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_MATCH_VICTORY_1 = register("ui.match_victory_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_MATCH_VICTORY_2 = register("ui.match_victory_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_DEFENDERS_WIN = register("ui.defenders_win");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_ATTACKERS_WIN = register("ui.attackers_win");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_SELECT_AGENT = register("ui.select_agent");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_LOCK_IN = register("ui.lock_in");
    public static final DeferredHolder<SoundEvent, SoundEvent> UI_HOVER_OVER_AGENT = register("ui.hover_over_agent");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, path)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}

