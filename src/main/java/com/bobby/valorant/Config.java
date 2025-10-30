package com.bobby.valorant;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    private Config() {}

    public static final class Common {
        public final ModConfigSpec.IntValue curveballMaxCharges;
        public final ModConfigSpec.IntValue curveballKillRechargeThreshold;
        public final ModConfigSpec.IntValue curveballThrowCooldownTicks;
        public final ModConfigSpec.DoubleValue curveballInitialVelocity;
        public final ModConfigSpec.DoubleValue curveballPreCurveDistance;
        public final ModConfigSpec.IntValue curveballCurveDurationTicks;
        public final ModConfigSpec.DoubleValue curveballCurveAngleDegrees;
        public final ModConfigSpec.IntValue curveballDetonationDelayTicks;
        public final ModConfigSpec.DoubleValue curveballFlashRadius;
        public final ModConfigSpec.DoubleValue curveballFlashConeAngleDegrees;
        public final ModConfigSpec.BooleanValue curveballAffectsThrower;
        
        // Flash timing controls
        public final ModConfigSpec.IntValue curveballFlashWindupTicks;
        public final ModConfigSpec.IntValue curveballFlashFullTicks;
        public final ModConfigSpec.IntValue curveballFlashFadeTicks;
        
        // Agent settings
        public final ModConfigSpec.BooleanValue agentSelectionEnabled;
        public final ModConfigSpec.IntValue agentSelectionKeyBinding;
        public final ModConfigSpec.ConfigValue<String> defaultAgent;
        public final ModConfigSpec.BooleanValue uniqueAgentsPerTeam;
        public final ModConfigSpec.BooleanValue mirrorPicksAcrossTeams;

        // HUD/Inventory settings
        public final ModConfigSpec.BooleanValue blockAllVanillaInventories;
        public final ModConfigSpec.BooleanValue hideVanillaHotbar;
        public final ModConfigSpec.BooleanValue showValorantHud;
        public final ModConfigSpec.DoubleValue hudScale;

        // Teams
        public final ModConfigSpec.IntValue maxTeamSize;

        // Weapons: Classic, Ghost, Valor Rifle
        public final ModConfigSpec.DoubleValue classicDamage;
        public final ModConfigSpec.DoubleValue classicRange;
        public final ModConfigSpec.DoubleValue classicSpreadDegrees;
        public final ModConfigSpec.IntValue classicCooldownTicks;
        public final ModConfigSpec.IntValue classicTracerParticles;
        public final ModConfigSpec.IntValue classicMuzzleParticles;

        public final ModConfigSpec.DoubleValue ghostDamage;
        public final ModConfigSpec.DoubleValue ghostRange;
        public final ModConfigSpec.DoubleValue ghostSpreadDegrees;
        public final ModConfigSpec.IntValue ghostCooldownTicks;
        public final ModConfigSpec.IntValue ghostTracerParticles;
        public final ModConfigSpec.IntValue ghostMuzzleParticles;

        public final ModConfigSpec.DoubleValue valorRifleDamage;
        public final ModConfigSpec.DoubleValue valorRifleRange;
        public final ModConfigSpec.DoubleValue valorRifleSpreadDegrees;
        public final ModConfigSpec.IntValue valorRifleCooldownTicks;
        public final ModConfigSpec.IntValue valorRifleTracerParticles;
        public final ModConfigSpec.IntValue valorRifleMuzzleParticles;

        Common(ModConfigSpec.Builder builder) {
            builder.push("curveball");

            curveballMaxCharges = builder.comment("Maximum Curveball charges a player can hold at once.")
                    .defineInRange("maxCharges", 2, 2, 2);

            curveballKillRechargeThreshold = builder.comment("Number of kills required to restore one Curveball charge.")
                    .defineInRange("killRechargeThreshold", 2, 1, 10);

            curveballThrowCooldownTicks = builder.comment("Cooldown in ticks applied after throwing a Curveball.")
                    .defineInRange("throwCooldownTicks", 20, 0, 20 * 30);

            curveballInitialVelocity = builder.comment("Initial forward velocity applied to the Curveball orb.")
                    .defineInRange("initialVelocity", 0.8D, 0.8D, 0.8D);

            curveballPreCurveDistance = builder.comment("Distance in blocks the orb travels before beginning its curve.")
                    .defineInRange("preCurveDistance", 2.5D, 2.5D, 2.5D);

            curveballCurveDurationTicks = builder.comment("Duration in ticks for the curved segment of the orb's flight.")
                    .defineInRange("curveDurationTicks", 6, 6, 6);

            curveballCurveAngleDegrees = builder.comment("Total turn angle in degrees during the curve segment (left/right based on input).")
                    .defineInRange("curveAngleDegrees", 90.0D, 10.0D, 180.0D);

            curveballDetonationDelayTicks = builder.comment("Ticks after completing the curve before the orb detonates.")
                    .defineInRange("detonationDelayTicks", 5, 0, 60);

            curveballFlashRadius = builder.comment("Maximum radius in blocks for Curveball to blind entities.")
                    .defineInRange("flashRadius", 32.0D, 1.0D, 64.0D);

            curveballFlashConeAngleDegrees = builder.comment("Field-of-view cone angle used to determine if entities are looking at the detonation (degrees).")
                    .defineInRange("flashConeAngleDegrees", 130.0D, 30.0D, 180.0D);

            curveballAffectsThrower = builder.comment("If true, the thrower can be blinded by their own Curveball when facing it.")
                    .define("affectsThrower", true);

            // New: Flash timing controls
            builder.comment("Flash timing: windup before flash, full-white hold, then slow fade").push("curveball.flashTiming");
            // 0.5s windup
            curveballFlashWindupTicks = builder
                    .comment("Windup delay after detonation before flash starts (ticks)")
                    .defineInRange("windupTicks", 10, 0, 20 * 5);
            // 1.5s full white
            curveballFlashFullTicks = builder
                    .comment("Duration of full-white screen with no fade (ticks)")
                    .defineInRange("fullTicks", 30, 1, 20 * 10);
            // Slow fade (default 2.0s)
            curveballFlashFadeTicks = builder
                    .comment("Fade-out duration after full-white completes (ticks)")
                    .defineInRange("fadeTicks", 60, 1, 20 * 10);
            builder.pop();

            builder.pop();
            
            // Agent configuration
            builder.push("agent");
            
            agentSelectionEnabled = builder.comment("Enable the agent selection menu system.")
                    .define("selectionEnabled", true);
            
            agentSelectionKeyBinding = builder.comment("Key binding code for opening agent selection menu.")
                    .defineInRange("selectionKeyBinding", 77, 0, 255); // 77 = M key
            
            defaultAgent = builder.comment("Default agent ID to use when player has no selection.")
                    .define("defaultAgent", "jett");

            uniqueAgentsPerTeam = builder.comment("If true, each agent can be locked by at most one player per team.")
                    .define("uniqueAgentsPerTeam", true);

            mirrorPicksAcrossTeams = builder.comment("If true, the same agent can be locked once per team (mirror picks allowed).")
                    .define("mirrorPicksAcrossTeams", true);
            
            builder.pop();

            // HUD/Inventory configuration
            builder.push("hud");
            blockAllVanillaInventories = builder.comment("Block all vanilla inventory/container screens (inventory, chests, crafting, etc.).")
                    .define("blockAllVanillaInventories", true);
            hideVanillaHotbar = builder.comment("Hide the vanilla hotbar to replace it with Valorant-style HUD.")
                    .define("hideVanillaHotbar", true);
            showValorantHud = builder.comment("Render the Valorant-style HUD overlay.")
                    .define("showValorantHud", true);
            hudScale = builder.comment("Global scale factor for the Valorant HUD (1.0 = 100%).")
                    .defineInRange("hudScale", 1.0D, 0.5D, 2.0D);
            builder.pop();

            // Weapons configuration
            builder.push("weapons");

            // Classic
            builder.push("classic");
            classicDamage = builder.comment("Damage per shot for Classic pistol.")
                    .defineInRange("damage", 6.0D, 0.0D, 100.0D);
            classicRange = builder.comment("Max range in blocks for Classic pistol.")
                    .defineInRange("range", 48.0D, 1.0D, 256.0D);
            classicSpreadDegrees = builder.comment("Random spread in degrees for Classic pistol.")
                    .defineInRange("spreadDegrees", 1.25D, 0.0D, 15.0D);
            classicCooldownTicks = builder.comment("Cooldown (fire rate) in ticks for Classic pistol.")
                    .defineInRange("cooldownTicks", 8, 0, 40);
            classicTracerParticles = builder.comment("Tracer particle steps for Classic pistol (visual only).")
                    .defineInRange("tracerSteps", 16, 1, 128);
            classicMuzzleParticles = builder.comment("Muzzle particle count for Classic pistol (visual only).")
                    .defineInRange("muzzleParticles", 4, 1, 32);
            builder.pop();

            // Ghost
            builder.push("ghost");
            ghostDamage = builder.comment("Damage per shot for Ghost pistol.")
                    .defineInRange("damage", 8.0D, 0.0D, 100.0D);
            ghostRange = builder.comment("Max range in blocks for Ghost pistol.")
                    .defineInRange("range", 64.0D, 1.0D, 256.0D);
            ghostSpreadDegrees = builder.comment("Random spread in degrees for Ghost pistol.")
                    .defineInRange("spreadDegrees", 0.9D, 0.0D, 15.0D);
            ghostCooldownTicks = builder.comment("Cooldown (fire rate) in ticks for Ghost pistol.")
                    .defineInRange("cooldownTicks", 7, 0, 40);
            ghostTracerParticles = builder.comment("Tracer particle steps for Ghost pistol (visual only).")
                    .defineInRange("tracerSteps", 18, 1, 128);
            ghostMuzzleParticles = builder.comment("Muzzle particle count for Ghost pistol (visual only).")
                    .defineInRange("muzzleParticles", 5, 1, 32);
            builder.pop();

            // Valor Rifle
            builder.push("valor_rifle");
            valorRifleDamage = builder.comment("Damage per shot for Valor Rifle.")
                    .defineInRange("damage", 5.0D, 0.0D, 100.0D);
            valorRifleRange = builder.comment("Max range in blocks for Valor Rifle.")
                    .defineInRange("range", 80.0D, 1.0D, 256.0D);
            valorRifleSpreadDegrees = builder.comment("Random spread in degrees for Valor Rifle.")
                    .defineInRange("spreadDegrees", 0.75D, 0.0D, 15.0D);
            valorRifleCooldownTicks = builder.comment("Cooldown (fire rate) in ticks for Valor Rifle.")
                    .defineInRange("cooldownTicks", 4, 0, 40);
            valorRifleTracerParticles = builder.comment("Tracer particle steps for Valor Rifle (visual only).")
                    .defineInRange("tracerSteps", 24, 1, 128);
            valorRifleMuzzleParticles = builder.comment("Muzzle particle count for Valor Rifle (visual only).")
                    .defineInRange("muzzleParticles", 6, 1, 32);
            builder.pop();

            builder.pop();

            // Team settings
            builder.push("teams");
            maxTeamSize = builder.comment("Maximum players per team (A and V).")
                    .defineInRange("maxTeamSize", 5, 1, 10);
            builder.pop();
        }
    }
}
