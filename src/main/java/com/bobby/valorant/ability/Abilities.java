package com.bobby.valorant.ability;

import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public final class Abilities {
    private static final Map<Agent, AbilitySet> BY_AGENT = new EnumMap<>(Agent.class);

    private Abilities() {}

    public static AbilitySet getForAgent(Agent agent) {
        AbilitySet set = BY_AGENT.get(agent);
        if (set == null) return BY_AGENT.getOrDefault(Agent.PHOENIX, defaultPhoenix());
        return set;
    }

    static {
        init();
    }

    public static void init() {
        // Phoenix
        Ability c_blaze = Ability.builder()
                .id("phoenix_c_blaze")
                .slot(Ability.Slot.C)
                .displayName(Component.literal("Blaze"))
                .description(Component.literal("Create a wall of fire."))
                .iconSupplier(() -> new ItemStack(ModItems.WALLSEGMENT.get()))
                .baseCharges(1)
                .cooldownSeconds(0)
                .effect(com.bobby.valorant.ability.effects.AbilityEffects::phoenixBlaze)
                .build();
        Ability q_hotHands = Ability.builder()
                .id("phoenix_q_hot_hands")
                .slot(Ability.Slot.Q)
                .displayName(Component.literal("Hot Hands"))
                .description(Component.literal("Throw a fireball."))
                .iconSupplier(() -> new ItemStack(ModItems.FIREBALL.get()))
                .baseCharges(1)
                .cooldownSeconds(0)
                .effect(com.bobby.valorant.ability.effects.AbilityEffects::phoenixHotHands)
                .build();
        Ability e_curveball = Ability.builder()
                .id("phoenix_e_curveball")
                .slot(Ability.Slot.E)
                .displayName(Component.literal("Curveball"))
                .description(Component.literal("Flash around corners."))
                .iconSupplier(() -> new ItemStack(ModItems.CURVEBALL.get()))
                .baseCharges(1)
                .cooldownSeconds(0)
                .effect(com.bobby.valorant.ability.effects.AbilityEffects::phoenixCurveball)
                .build();
        Ability x_runItBack = Ability.builder()
                .id("phoenix_x_run_it_back")
                .slot(Ability.Slot.X)
                .displayName(Component.literal("Run It Back"))
                .description(Component.literal("Mark position and revive on expire."))
                .iconSupplier(() -> new ItemStack(ModItems.RUN_IT_BACK.get()))
                .ultCost(6)
                .effect(com.bobby.valorant.ability.effects.AbilityEffects::phoenixRunItBack)
                .build();
        BY_AGENT.put(Agent.PHOENIX, new AbilitySet(c_blaze, q_hotHands, e_curveball, x_runItBack));

        // Brimstone (icons approximate with available items)
        Ability br_c = Ability.builder().id("brim_c_stim").slot(Ability.Slot.C)
                .displayName(Component.literal("Stim Beacon")).description(Component.literal("Buff allies."))
                .iconSupplier(() -> new ItemStack(ModItems.STIMBEACONHAND.get()))
                .baseCharges(1).effect(com.bobby.valorant.ability.effects.AbilityEffects::brimStimBeacon).build();
        Ability br_q = Ability.builder().id("brim_q_incendiary").slot(Ability.Slot.Q)
                .displayName(Component.literal("Incendiary")).description(Component.literal("Launch molly."))
                .baseCharges(1).effect((sp, ctx) -> {}).build();
        Ability br_e = Ability.builder().id("brim_e_sky_smoke").slot(Ability.Slot.E)
                .displayName(Component.literal("Sky Smoke")).description(Component.literal("Place smokes."))
                .baseCharges(3).effect((sp, ctx) -> {}).build();
        Ability br_x = Ability.builder().id("brim_x_orbital").slot(Ability.Slot.X)
                .displayName(Component.literal("Orbital Strike")).description(Component.literal("Call laser."))
                .iconSupplier(() -> ItemStack.EMPTY)
                .ultCost(8).effect((sp, ctx) -> {}).build();
        BY_AGENT.put(Agent.BRIMSTONE, new AbilitySet(br_c, br_q, br_e, br_x));

        // Jett (placeholder icons)
        Ability j_c = Ability.builder().id("jett_c_cloudburst").slot(Ability.Slot.C)
                .displayName(Component.literal("Cloudburst")).description(Component.literal("Smoke."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(2).effect((sp, ctx) -> {}).build();
        Ability j_q = Ability.builder().id("jett_q_updraft").slot(Ability.Slot.Q)
                .displayName(Component.literal("Updraft")).description(Component.literal("Upwards dash."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(1).effect((sp, ctx) -> {}).build();
        Ability j_e = Ability.builder().id("jett_e_tailwind").slot(Ability.Slot.E)
                .displayName(Component.literal("Tailwind")).description(Component.literal("Dash."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(1).effect((sp, ctx) -> {}).build();
        Ability j_x = Ability.builder().id("jett_x_blade_storm").slot(Ability.Slot.X)
                .displayName(Component.literal("Blade Storm")).description(Component.literal("Knives ult."))
                .iconSupplier(() -> new ItemStack(ModItems.KNIFE.get()))
                .ultCost(8).effect((sp, ctx) -> {}).build();
        BY_AGENT.put(Agent.JETT, new AbilitySet(j_c, j_q, j_e, j_x));

        // Omen (placeholder icons)
        Ability o_c = Ability.builder().id("omen_c_shrouded").slot(Ability.Slot.C)
                .displayName(Component.literal("Shrouded Step")).description(Component.literal("Short TP."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(2).effect((sp, ctx) -> {}).build();
        Ability o_q = Ability.builder().id("omen_q_paranoia").slot(Ability.Slot.Q)
                .displayName(Component.literal("Paranoia")).description(Component.literal("Cone blind."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(1).effect((sp, ctx) -> {}).build();
        Ability o_e = Ability.builder().id("omen_e_dark_cover").slot(Ability.Slot.E)
                .displayName(Component.literal("Dark Cover")).description(Component.literal("Smokes."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(2).effect((sp, ctx) -> {}).build();
        Ability o_x = Ability.builder().id("omen_x_from_shadows").slot(Ability.Slot.X)
                .displayName(Component.literal("From the Shadows")).description(Component.literal("Global TP."))
                .iconSupplier(() -> ItemStack.EMPTY).ultCost(7).effect((sp, ctx) -> {}).build();
        BY_AGENT.put(Agent.OMEN, new AbilitySet(o_c, o_q, o_e, o_x));

        // Raze
        Ability rz_c = Ability.builder().id("raze_c_boom_bot").slot(Ability.Slot.C)
                .displayName(Component.literal("Boom Bot")).description(Component.literal("Seek bot."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(1).effect((sp, ctx) -> {}).build();
        Ability rz_q = Ability.builder().id("raze_q_blast_pack").slot(Ability.Slot.Q)
                .displayName(Component.literal("Blast Pack")).description(Component.literal("Satchel."))
                .iconSupplier(() -> new ItemStack(ModItems.BLAST_PACK.get()))
                .baseCharges(2).effect(com.bobby.valorant.ability.effects.AbilityEffects::razeBlastPack).build();
        Ability rz_e = Ability.builder().id("raze_e_paint_shells").slot(Ability.Slot.E)
                .displayName(Component.literal("Paint Shells")).description(Component.literal("Grenade."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(1).effect((sp, ctx) -> {}).build();
        Ability rz_x = Ability.builder().id("raze_x_showstopper").slot(Ability.Slot.X)
                .displayName(Component.literal("Showstopper")).description(Component.literal("Rocket."))
                .iconSupplier(() -> ItemStack.EMPTY).ultCost(8).effect((sp, ctx) -> {}).build();
        BY_AGENT.put(Agent.RAZE, new AbilitySet(rz_c, rz_q, rz_e, rz_x));

        // Sage
        Ability sg_c = Ability.builder().id("sage_c_barrier").slot(Ability.Slot.C)
                .displayName(Component.literal("Barrier Orb")).description(Component.literal("Wall."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(1).effect((sp, ctx) -> {}).build();
        Ability sg_q = Ability.builder().id("sage_q_slow").slot(Ability.Slot.Q)
                .displayName(Component.literal("Slow Orb")).description(Component.literal("Slow field."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(2).effect((sp, ctx) -> {}).build();
        Ability sg_e = Ability.builder().id("sage_e_heal").slot(Ability.Slot.E)
                .displayName(Component.literal("Healing Orb")).description(Component.literal("Heal."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(1).effect((sp, ctx) -> {}).build();
        Ability sg_x = Ability.builder().id("sage_x_resurrect").slot(Ability.Slot.X)
                .displayName(Component.literal("Resurrection")).description(Component.literal("Revive."))
                .iconSupplier(() -> ItemStack.EMPTY).ultCost(7).effect((sp, ctx) -> {}).build();
        BY_AGENT.put(Agent.SAGE, new AbilitySet(sg_c, sg_q, sg_e, sg_x));

        // Sova
        Ability sv_c = Ability.builder().id("sova_c_drone").slot(Ability.Slot.C)
                .displayName(Component.literal("Owl Drone")).description(Component.literal("Drone."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(1).effect((sp, ctx) -> {}).build();
        Ability sv_q = Ability.builder().id("sova_q_shock").slot(Ability.Slot.Q)
                .displayName(Component.literal("Shock Bolt")).description(Component.literal("Shock arrow."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(2).effect((sp, ctx) -> {}).build();
        Ability sv_e = Ability.builder().id("sova_e_recon").slot(Ability.Slot.E)
                .displayName(Component.literal("Recon Bolt")).description(Component.literal("Scan arrow."))
                .iconSupplier(() -> ItemStack.EMPTY).baseCharges(1).effect((sp, ctx) -> {}).build();
        Ability sv_x = Ability.builder().id("sova_x_hunters_fury").slot(Ability.Slot.X)
                .displayName(Component.literal("Hunter's Fury")).description(Component.literal("Laser beams."))
                .iconSupplier(() -> ItemStack.EMPTY).ultCost(8).effect((sp, ctx) -> {}).build();
        BY_AGENT.put(Agent.SOVA, new AbilitySet(sv_c, sv_q, sv_e, sv_x));
    }

    private static AbilitySet defaultPhoenix() {
        return BY_AGENT.get(Agent.PHOENIX);
    }
}


