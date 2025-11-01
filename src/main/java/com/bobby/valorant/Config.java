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
        public final ModConfigSpec.ConfigValue<Integer> curveballMaxCharges;
        public final ModConfigSpec.IntValue curveballKillRechargeThreshold;
        public final ModConfigSpec.IntValue curveballThrowCooldownTicks;
        public final ModConfigSpec.ConfigValue<Double> curveballInitialVelocity;
        public final ModConfigSpec.ConfigValue<Double> curveballPreCurveDistance;
        public final ModConfigSpec.ConfigValue<Integer> curveballCurveDurationTicks;
        public final ModConfigSpec.DoubleValue curveballCurveAngleDegrees;
        public final ModConfigSpec.IntValue curveballDetonationDelayTicks;
        public final ModConfigSpec.DoubleValue curveballFlashRadius;
        public final ModConfigSpec.DoubleValue curveballFlashConeAngleDegrees;
        public final ModConfigSpec.BooleanValue curveballAffectsThrower;
        
        // Flash timing controls
        public final ModConfigSpec.IntValue curveballFlashWindupTicks;
        public final ModConfigSpec.IntValue curveballFlashFullTicks;
        public final ModConfigSpec.ConfigValue<Integer> curveballFlashFadeTicks;
        
        // Fireball settings
        public final ModConfigSpec.IntValue fireballMaxCharges;
        public final ModConfigSpec.IntValue fireballKillRechargeThreshold;
        public final ModConfigSpec.IntValue fireballThrowCooldownTicks;
        public final ModConfigSpec.DoubleValue fireballInitialVelocity;
        public final ModConfigSpec.DoubleValue fireballDamage;
        public final ModConfigSpec.DoubleValue fireballExplosionRadius;
        // Fireball molly cloud
        public final ModConfigSpec.DoubleValue fireballMollyRadius;
        public final ModConfigSpec.IntValue fireballMollyDurationTicks;
        public final ModConfigSpec.IntValue fireballMollyWitherAmplifier;
        public final ModConfigSpec.IntValue fireballMollyReapplicationDelay;
        public final ModConfigSpec.DoubleValue fireballMollyDamagePerTick;
        public final ModConfigSpec.IntValue fireballMollyTickInterval;
        public final ModConfigSpec.ConfigValue<String> fireballMollyPrimaryParticle;
        public final ModConfigSpec.ConfigValue<String> fireballMollySecondaryParticle;

        // Fire Wall settings
        public final ModConfigSpec.IntValue firewallMaxCharges;
        public final ModConfigSpec.IntValue firewallKillRechargeThreshold;
        public final ModConfigSpec.IntValue firewallThrowCooldownTicks;
        public final ModConfigSpec.DoubleValue firewallMaxRange;
        public final ModConfigSpec.DoubleValue firewallGrowthSpeed;
        public final ModConfigSpec.DoubleValue firewallSegmentSpacing;
        public final ModConfigSpec.DoubleValue firewallCurveSensitivity;
        public final ModConfigSpec.IntValue firewallDurationTicks;
        public final ModConfigSpec.DoubleValue firewallDamagePerTick;
        public final ModConfigSpec.ConfigValue<String> firewallParticleType;
        public final ModConfigSpec.DoubleValue firewallYOffset;
        public final ModConfigSpec.DoubleValue firewallRotationOffsetDegrees;

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

        // Title overlay settings
        public final ModConfigSpec.IntValue titleFadeInTicks;
        public final ModConfigSpec.IntValue titleStayTicks;
        public final ModConfigSpec.IntValue titleFadeOutTicks;

        // Teams
        public final ModConfigSpec.IntValue maxTeamSize;

        // Spike runtime signal
        public final ModConfigSpec.BooleanValue spikePlanted;
        public final ModConfigSpec.DoubleValue plantedSpikeYOffset;

        // Weapons:
        public final ModConfigSpec.DoubleValue classicDamage;
        public final ModConfigSpec.DoubleValue classicRange;
        public final ModConfigSpec.DoubleValue classicSpreadDegrees;
        public final ModConfigSpec.IntValue classicCooldownTicks;
        public final ModConfigSpec.IntValue classicTracerParticles;
        public final ModConfigSpec.IntValue classicMuzzleParticles;
        public final ModConfigSpec.IntValue classicMagazineSize;
        public final ModConfigSpec.IntValue classicMaxReserveAmmo;
        public final ModConfigSpec.IntValue classicReloadTimeTicks;

        public final ModConfigSpec.DoubleValue ghostDamage;
        public final ModConfigSpec.DoubleValue ghostRange;
        public final ModConfigSpec.DoubleValue ghostSpreadDegrees;
        public final ModConfigSpec.IntValue ghostCooldownTicks;
        public final ModConfigSpec.IntValue ghostTracerParticles;
        public final ModConfigSpec.IntValue ghostMuzzleParticles;
        public final ModConfigSpec.IntValue ghostMagazineSize;
        public final ModConfigSpec.IntValue ghostMaxReserveAmmo;
        public final ModConfigSpec.IntValue ghostReloadTimeTicks;

        public final ModConfigSpec.DoubleValue vandalRifleDamage;
        public final ModConfigSpec.DoubleValue vandalRifleRange;
        public final ModConfigSpec.DoubleValue vandalRifleSpreadDegrees;
        public final ModConfigSpec.IntValue vandalRifleCooldownTicks;
        public final ModConfigSpec.IntValue vandalRifleTracerParticles;
        public final ModConfigSpec.IntValue vandalRifleMuzzleParticles;
        public final ModConfigSpec.IntValue vandalRifleMagazineSize;
        public final ModConfigSpec.IntValue vandalRifleMaxReserveAmmo;
        public final ModConfigSpec.IntValue vandalRifleReloadTimeTicks;

        // Commands / particles
        public final ModConfigSpec.IntValue particleCommandDefaultDurationTicks;

        // Reload animation settings
        public final ModConfigSpec.BooleanValue reloadAnimationEnabled;
        public final ModConfigSpec.DoubleValue reloadHandSwingAmount;
        public final ModConfigSpec.DoubleValue reloadMagazineScale;
        public final ModConfigSpec.DoubleValue reloadLeftHandMovement;
        public final ModConfigSpec.BooleanValue reloadShowEjectedMagazine;
        public final ModConfigSpec.DoubleValue reloadEjectedMagazineOffset;
        public final ModConfigSpec.BooleanValue reloadRackSlide;
        public final ModConfigSpec.DoubleValue reloadRackSlideAmount;

        Common(ModConfigSpec.Builder builder) {
            builder.push("curveball");

            curveballMaxCharges = builder.comment("Maximum Curveball charges a player can hold at once.")
                    .define("maxCharges", 2);

            curveballKillRechargeThreshold = builder.comment("Number of kills required to restore one Curveball charge.")
                    .defineInRange("killRechargeThreshold", 2, 1, 10);

            curveballThrowCooldownTicks = builder.comment("Cooldown in ticks applied after throwing a Curveball.")
                    .defineInRange("throwCooldownTicks", 20, 0, 20 * 30);

            curveballInitialVelocity = builder.comment("Initial forward velocity applied to the Curveball orb.")
                    .define("initialVelocity", 0.8D);

            curveballPreCurveDistance = builder.comment("Distance in blocks the orb travels before beginning its curve.")
                    .define("preCurveDistance", 2.5D);

            curveballCurveDurationTicks = builder.comment("Duration in ticks for the curved segment of the orb's flight.")
                    .define("curveDurationTicks", 6);

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
                    .define("fadeTicks", 120);
            builder.pop();

            builder.pop();
            
            // Fireball configuration
            builder.push("fireball");

            fireballMaxCharges = builder.comment("Maximum Fireball charges a player can hold at once.")
                    .defineInRange("maxCharges", 1, 1, 5);

            fireballKillRechargeThreshold = builder.comment("Number of kills required to restore one Fireball charge.")
                    .defineInRange("killRechargeThreshold", 2, 1, 10);

            fireballThrowCooldownTicks = builder.comment("Cooldown in ticks applied after throwing a Fireball.")
                    .defineInRange("throwCooldownTicks", 20, 0, 20 * 30);

            fireballInitialVelocity = builder.comment("Initial forward velocity applied to the Fireball entity.")
                    .defineInRange("initialVelocity", 1.5D, 0.5D, 5.0D);
            
            fireballDamage = builder.comment("Damage dealt by the Fireball explosion.")
                    .defineInRange("damage", 6.0D, 0.0D, 100.0D);
            
            fireballExplosionRadius = builder.comment("Radius of the Fireball explosion.")
                    .defineInRange("explosionRadius", 3.0D, 1.0D, 10.0D);

            // Molly cloud parameters (used instead of explosion)
            builder.push("molly");
            fireballMollyRadius = builder.comment("Radius of fire molly cloud.")
                    .defineInRange("radius", 3.5D, 0.5D, 12.0D);
            fireballMollyDurationTicks = builder.comment("Duration of molly cloud in ticks.")
                    .defineInRange("durationTicks", 200, 20, 20 * 30);
            fireballMollyWitherAmplifier = builder.comment("Wither amplifier applied while in cloud (tick damage strength). 0=base.")
                    .defineInRange("witherAmplifier", 0, 0, 4);
            fireballMollyReapplicationDelay = builder.comment("Ticks before the cloud reapplies its effect to the same target.")
                    .defineInRange("reapplicationDelay", 10, 1, 40);
            fireballMollyDamagePerTick = builder.comment("Direct damage dealt each tick interval inside the cloud.")
                    .defineInRange("damagePerTick", 1.0D, 0.0D, 20.0D);
            fireballMollyTickInterval = builder.comment("Interval in ticks between damage applications.")
                    .defineInRange("tickInterval", 10, 1, 40);
            fireballMollyPrimaryParticle = builder.comment("Primary particle id for molly (e.g., FLAME, CAMPFIRE_COSY_SMOKE, ASH, LAVA, SOUL_FIRE_FLAME)")
                    .define("primaryParticle", "CAMPFIRE_COSY_SMOKE");
            fireballMollySecondaryParticle = builder.comment("Secondary particle id for molly (optional, empty to disable)")
                    .define("secondaryParticle", "FLAME");
            builder.pop();

            builder.pop();

            // Fire Wall configuration
            builder.push("firewall");

            firewallMaxCharges = builder.comment("Maximum Fire Wall charges a player can hold at once.")
                    .defineInRange("maxCharges", 1, 1, 5);

            firewallKillRechargeThreshold = builder.comment("Number of kills required to restore one Fire Wall charge.")
                    .defineInRange("killRechargeThreshold", 3, 1, 10);

            firewallThrowCooldownTicks = builder.comment("Cooldown in ticks applied after using Fire Wall.")
                    .defineInRange("throwCooldownTicks", 20, 0, 20 * 30);

            firewallMaxRange = builder.comment("Maximum range in blocks for Fire Wall.")
                    .defineInRange("maxRange", 18.0D, 18.0D, 18.0D);

            firewallGrowthSpeed = builder.comment("Speed at which wall segments are placed (ticks per segment).")
                    .defineInRange("growthSpeed", 0.1D, 0.1D, 0.1D);

            firewallSegmentSpacing = builder.comment("Distance between wall segments in blocks.")
                    .defineInRange("segmentSpacing", 0.5D, 0.5D, 0.5D);

            firewallCurveSensitivity = builder.comment("How sensitive the wall is to player head movement (degrees per tick).")
                    .defineInRange("curveSensitivity", 0.1D, 0.1D, 0.1D);

            firewallDurationTicks = builder.comment("Duration in ticks that the fire wall lasts.")
                    .defineInRange("durationTicks", 300, 20, 20 * 60);

            firewallDamagePerTick = builder.comment("Damage dealt per tick to entities touching the wall.")
                    .defineInRange("damagePerTick", 2.0D, 0.0D, 10.0D);

            firewallParticleType = builder.comment("Particle type for fire wall effect.")
                    .define("particleType", "FLAME");

            firewallYOffset = builder.comment("Vertical offset added to each wall segment position (blocks).")
                    .defineInRange("yOffset", -1.8D, -1.8D, -1.8D);

            firewallRotationOffsetDegrees = builder.comment("Additional yaw rotation (degrees) applied to each wall segment.")
                    .defineInRange("rotationOffsetDegrees", 0.0D, -180.0D, 180.0D);

            builder.pop();

            // Agent configuration
            builder.push("agent");
            
            agentSelectionEnabled = builder.comment("Enable the agent selection menu system.")
                    .define("selectionEnabled", true);
            
            agentSelectionKeyBinding = builder.comment("Key binding code for opening agent selection menu.")
                    .defineInRange("selectionKeyBinding", 77, 0, 255); // 77 = M key
            
            defaultAgent = builder.comment("Default agent ID to use when player has no selection.")
                    .define("defaultAgent", "phoenix");

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

            // Title overlay settings
            builder.push("titleOverlay");
            titleFadeInTicks = builder.comment("Ticks for title overlay to fade in.")
                    .defineInRange("fadeInTicks", 10, 0, 100);
            titleStayTicks = builder.comment("Ticks for title overlay to stay visible.")
                    .defineInRange("stayTicks", 700, 0, 400);
            titleFadeOutTicks = builder.comment("Ticks for title overlay to fade out.")
                    .defineInRange("fadeOutTicks", 10, 0, 100);
            builder.pop();

            builder.pop();

            // Weapons configuration
            builder.push("weapons");

            // Classic
            builder.push("classic");
            classicDamage = builder.comment("Damage per shot for Classic pistol.")
                    .defineInRange("damage", 6.0D, 0.0D, 100.0D);
            classicRange = builder.comment("Max range in blocks for Classic pistol.")
                    .defineInRange("range", 1000.0D, 1000.0D, 1000.0D);
            classicSpreadDegrees = builder.comment("Random spread in degrees for Classic pistol.")
                    .defineInRange("spreadDegrees", 1.25D, 0.0D, 15.0D);
            classicCooldownTicks = builder.comment("Cooldown (fire rate) in ticks for Classic pistol.")
                    .defineInRange("cooldownTicks", 8, 0, 40);
            classicTracerParticles = builder.comment("Tracer particle steps for Classic pistol (visual only).")
                    .defineInRange("tracerSteps", 5, 5, 5);
            classicMuzzleParticles = builder.comment("Muzzle particle count for Classic pistol (visual only).")
                    .defineInRange("muzzleParticles", 4, 1, 32);
            classicMagazineSize = builder.comment("Magazine size for Classic pistol.")
                    .defineInRange("magazineSize", 12, 1, 100);
            classicMaxReserveAmmo = builder.comment("Maximum reserve ammo for Classic pistol.")
                    .defineInRange("maxReserveAmmo", 36, 0, 500);
            classicReloadTimeTicks = builder.comment("Reload time in ticks for Classic pistol (40 ticks = 2.0 seconds).")
                    .defineInRange("reloadTimeTicks", 40, 1, 20 * 30);
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
            ghostMagazineSize = builder.comment("Magazine size for Ghost pistol.")
                    .defineInRange("magazineSize", 15, 1, 100);
            ghostMaxReserveAmmo = builder.comment("Maximum reserve ammo for Ghost pistol.")
                    .defineInRange("maxReserveAmmo", 45, 0, 500);
            ghostReloadTimeTicks = builder.comment("Reload time in ticks for Ghost pistol (45 ticks = 2.25 seconds).")
                    .defineInRange("reloadTimeTicks", 45, 1, 20 * 30);
            builder.pop();

            // Vandal Rifle
            builder.push("vandal_rifle");
            vandalRifleDamage = builder.comment("Damage per shot for Vandal Rifle.")
                    .defineInRange("damage", 5.0D, 0.0D, 100.0D);
            vandalRifleRange = builder.comment("Max range in blocks for Vandal Rifle.")
                    .defineInRange("range", 80.0D, 1.0D, 256.0D);
            vandalRifleSpreadDegrees = builder.comment("Random spread in degrees for Vandal Rifle.")
                    .defineInRange("spreadDegrees", 0.75D, 0.0D, 15.0D);
            vandalRifleCooldownTicks = builder.comment("Cooldown (fire rate) in ticks for Vandal Rifle.")
                    .defineInRange("cooldownTicks", 4, 0, 40);
            vandalRifleTracerParticles = builder.comment("Tracer particle steps for Vandal Rifle (visual only).")
                    .defineInRange("tracerSteps", 24, 1, 128);
            vandalRifleMuzzleParticles = builder.comment("Muzzle particle count for Vandal Rifle (visual only).")
                    .defineInRange("muzzleParticles", 6, 1, 32);
            vandalRifleMagazineSize = builder.comment("Magazine size for Vandal Rifle.")
                    .defineInRange("magazineSize", 25, 1, 100);
            vandalRifleMaxReserveAmmo = builder.comment("Maximum reserve ammo for Vandal Rifle.")
                    .defineInRange("maxReserveAmmo", 75, 0, 500);
            vandalRifleReloadTimeTicks = builder.comment("Reload time in ticks for Vandal Rifle (50 ticks = 2.5 seconds).")
                    .defineInRange("reloadTimeTicks", 50, 1, 20 * 30);
            builder.pop();

            builder.pop();

            // Commands / particles
            builder.push("commands");
            builder.push("particles");
            particleCommandDefaultDurationTicks = builder
                    .comment("Default duration for /valorant particle command (ticks)")
                    .defineInRange("defaultDurationTicks", 200, 1, 20 * 60 * 10);
            builder.pop();
            builder.pop();

            // Reload animation settings
            builder.push("reloadAnimation");
            reloadAnimationEnabled = builder.comment("Enable first-person reload animations for both hands.")
                    .define("enabled", true);
            reloadHandSwingAmount = builder.comment("Amount of hand swing/movement during reload animation.")
                    .defineInRange("handSwingAmount", 0.3D, 0.0D, 2.0D);
            reloadMagazineScale = builder.comment("Scale of the magazine item shown during reload animation.")
                    .defineInRange("magazineScale", 0.8D, 0.1D, 2.0D);
            reloadLeftHandMovement = builder.comment("Movement amplitude of the left hand during magazine swap.")
                    .defineInRange("leftHandMovement", 0.5D, 0.0D, 2.0D);
            reloadShowEjectedMagazine = builder.comment("Show ejected magazine during reload animation.")
                    .define("showEjectedMagazine", true);
            reloadEjectedMagazineOffset = builder.comment("Distance the ejected magazine flies out.")
                    .defineInRange("ejectedMagazineOffset", 0.8D, 0.1D, 2.0D);
            reloadRackSlide = builder.comment("Enable slide racking animation at the end of reload.")
                    .define("rackSlide", true);
            reloadRackSlideAmount = builder.comment("How far back the slide is pulled during racking.")
                    .defineInRange("rackSlideAmount", 0.15D, 0.0D, 0.5D);
            builder.pop();

            // Spike section (runtime flags)
            builder.push("spike");
            spikePlanted = builder.comment("Runtime flag set to true when Spike is planted. Server-updated.")
                    .define("planted", false);
            plantedSpikeYOffset = builder.comment("Vertical offset applied to the planted spike ArmorStand (negative sinks it).")
                    .defineInRange("plantedSpikeYOffset", -1.6D, -1.6D, -1.60D);
            builder.pop();

            // Team settings
            builder.push("teams");
            maxTeamSize = builder.comment("Maximum players per team (A and V).")
                    .defineInRange("maxTeamSize", 5, 1, 10);
            builder.pop();
        }
    }
}
