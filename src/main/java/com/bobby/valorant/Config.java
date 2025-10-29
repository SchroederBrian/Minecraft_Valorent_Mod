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

        // HUD/Inventory settings
        public final ModConfigSpec.BooleanValue blockAllVanillaInventories;
        public final ModConfigSpec.BooleanValue hideVanillaHotbar;
        public final ModConfigSpec.BooleanValue showValorantHud;
        public final ModConfigSpec.DoubleValue hudScale;

        // Teams
        public final ModConfigSpec.IntValue maxTeamSize;

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

            // Team settings
            builder.push("teams");
            maxTeamSize = builder.comment("Maximum players per team (A and V).")
                    .defineInRange("maxTeamSize", 5, 1, 10);
            builder.pop();
        }
    }
}
