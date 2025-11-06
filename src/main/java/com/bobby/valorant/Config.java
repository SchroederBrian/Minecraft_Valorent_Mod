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
        
        // Molly Launcher settings
        public final ModConfigSpec.DoubleValue mollyLauncherDamage;
        public final ModConfigSpec.DoubleValue mollyLauncherRange;
        public final ModConfigSpec.DoubleValue mollyLauncherSpreadDegrees;
        public final ModConfigSpec.IntValue mollyLauncherCooldownTicks;
        public final ModConfigSpec.IntValue mollyLauncherTracerParticles;
        public final ModConfigSpec.IntValue mollyLauncherMuzzleParticles;
        public final ModConfigSpec.IntValue mollyLauncherMagazineSize;
        public final ModConfigSpec.IntValue mollyLauncherMaxReserveAmmo;
        public final ModConfigSpec.IntValue mollyLauncherReloadTimeTicks;

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

        // Agent skin override settings
        public final ModConfigSpec.BooleanValue agentSkinsEnabled;
        public final ModConfigSpec.ConfigValue<String> agentSkinsDefaultAgent;
        public final ModConfigSpec.ConfigValue<Object> agentSkinsTextures;
        public final ModConfigSpec.BooleanValue agentSkinsDisableCapes;
        public final ModConfigSpec.ConfigValue<Object> agentAbilities;

        // HUD/Inventory settings
        public final ModConfigSpec.BooleanValue blockAllVanillaInventories;
        public final ModConfigSpec.BooleanValue hideVanillaHotbar;
        public final ModConfigSpec.BooleanValue showValorantHud;
        public final ModConfigSpec.DoubleValue hudScale;

        // Player movement settings
        public final ModConfigSpec.BooleanValue preventSprinting;

        // Killfeed settings
        public final ModConfigSpec.BooleanValue killfeedEnabled;
        public final ModConfigSpec.IntValue killfeedDurationTicks;
        public final ModConfigSpec.IntValue killfeedMaxMessages;
        public final ModConfigSpec.IntValue killfeedOffsetRight;
        public final ModConfigSpec.IntValue killfeedOffsetDown;
        public final ModConfigSpec.IntValue killfeedLineSpacing;
        public final ModConfigSpec.IntValue killfeedKillerBgColor;
        public final ModConfigSpec.IntValue killfeedVictimBgColor;
        public final ModConfigSpec.IntValue killfeedTextColor;

        // Spike icon (plant indicator under timer)
        public final ModConfigSpec.IntValue spikeIconWidth;
        public final ModConfigSpec.IntValue spikeIconHeight;
        public final ModConfigSpec.IntValue spikeIconRotationDegrees;
        public final ModConfigSpec.IntValue spikeIconOffsetX;
        public final ModConfigSpec.IntValue spikeIconOffsetY;

        // Title overlay settings
        public final ModConfigSpec.IntValue titleFadeInTicks;
        public final ModConfigSpec.IntValue titleStayTicks;
        public final ModConfigSpec.IntValue titleFadeOutTicks;

        // Teams
        public final ModConfigSpec.IntValue maxTeamSize;

        // FancyMenu integration
        public final ModConfigSpec.BooleanValue enableFancyMenuTeamVars;
        public final ModConfigSpec.IntValue teamVarSlots;
        public final ModConfigSpec.ConfigValue<String> fancymenuVariablePrefix;

        // Spawn areas (team polygons and particle rendering)
        public final ModConfigSpec.ConfigValue<String> spawnAreaConfigPath;
        public final ModConfigSpec.ConfigValue<String> spawnAreaParticleType;
        public final ModConfigSpec.DoubleValue spawnAreaParticleSpacing;
        public final ModConfigSpec.IntValue spawnAreaParticleTickInterval;
        public final ModConfigSpec.ConfigValue<String> bombSiteParticleType;
        public final ModConfigSpec.IntValue spawnAreaParticleColor;
        public final ModConfigSpec.IntValue bombSiteParticleColor;

        // Sky Smoke settings
        public final ModConfigSpec.ConfigValue<String> skySmokeMapDimensionId;
        public final ModConfigSpec.IntValue skySmokeMapMinX;
        public final ModConfigSpec.IntValue skySmokeMapMinZ;
        public final ModConfigSpec.IntValue skySmokeMapMaxX;
        public final ModConfigSpec.IntValue skySmokeMapMaxZ;
        public final ModConfigSpec.DoubleValue skySmokeMapRotationDegrees;
        // Calibration precision/model
        public final ModConfigSpec.ConfigValue<String> skySmokeCalibrationModel; // SIMILARITY|AFFINE|HOMOGRAPHY
        public final ModConfigSpec.IntValue skySmokeCalibrationMinPoints;
        public final ModConfigSpec.BooleanValue skySmokeCalibrationUseRansac;
        public final ModConfigSpec.IntValue skySmokeCalibrationRansacIterations;
        public final ModConfigSpec.DoubleValue skySmokeCalibrationRansacThreshold;
        public final ModConfigSpec.DoubleValue skySmokeCalibrationUiZoomFactor;
        public final ModConfigSpec.BooleanValue skySmokeCalibrationUiEnableSnap;
        public final ModConfigSpec.IntValue skySmokeMaxPerCast;
        public final ModConfigSpec.DoubleValue skySmokeRadius;
        public final ModConfigSpec.IntValue skySmokeDurationTicks;
        public final ModConfigSpec.BooleanValue skySmokePlaceOnGround;
        public final ModConfigSpec.DoubleValue skySmokeYOffset;
        public final ModConfigSpec.DoubleValue skySmokeSpawnXOffset;
        public final ModConfigSpec.DoubleValue skySmokeSpawnZOffset;
        public final ModConfigSpec.BooleanValue skySmokeApplyBlindness;
        public final ModConfigSpec.DoubleValue skySmokeBlindnessRadiusOffset;
        public final ModConfigSpec.IntValue skySmokeBlindnessTicks;
        public final ModConfigSpec.ConfigValue<String> skySmokeAreasConfigPath;

        // Sky Smoke zones rendering
        public final ModConfigSpec.ConfigValue<String> skySmokeAreasParticleType;
        public final ModConfigSpec.DoubleValue skySmokeAreasParticleSpacing;
        public final ModConfigSpec.IntValue skySmokeAreasParticleTickInterval;
        public final ModConfigSpec.IntValue skySmokeAreasAllowedParticleColor;
        public final ModConfigSpec.IntValue skySmokeAreasBlockedParticleColor;
        public final ModConfigSpec.BooleanValue skySmokeAreasShowRecordingParticles;
        public final ModConfigSpec.IntValue skySmokeAreasRecordingParticleColor;

        // Sky Smoke map UI (player marker / follow)
        public final ModConfigSpec.BooleanValue skySmokeUiShowPlayerMarker;
        public final ModConfigSpec.BooleanValue skySmokeUiFollowPlayer;
        public final ModConfigSpec.DoubleValue skySmokeUiFollowZoomFactor;
        public final ModConfigSpec.IntValue skySmokeUiPlayerMarkerColor;
        public final ModConfigSpec.IntValue skySmokeUiPlayerMarkerSize;

        // Spike runtime signal
        public final ModConfigSpec.BooleanValue spikePlanted;
        public final ModConfigSpec.DoubleValue plantedSpikeYOffset;
        public final ModConfigSpec.IntValue spikePlantHoldTicks;
        public final ModConfigSpec.BooleanValue lockMovementWhilePlanting;
        public final ModConfigSpec.BooleanValue lockMovementWhileDefusing;

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

        public final ModConfigSpec.DoubleValue frenzyDamage;
        public final ModConfigSpec.DoubleValue frenzyRange;
        public final ModConfigSpec.DoubleValue frenzySpreadDegrees;
        public final ModConfigSpec.IntValue frenzyCooldownTicks;
        public final ModConfigSpec.IntValue frenzyTracerParticles;
        public final ModConfigSpec.IntValue frenzyMuzzleParticles;
        public final ModConfigSpec.IntValue frenzyMagazineSize;
        public final ModConfigSpec.IntValue frenzyMaxReserveAmmo;
        public final ModConfigSpec.IntValue frenzyReloadTimeTicks;

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

        // Knife settings
        public final ModConfigSpec.DoubleValue knifeSpeedMultiplier;

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

        // Sound settings
        public final ModConfigSpec.BooleanValue soundEnabled;
        public final ModConfigSpec.DoubleValue soundReloadVolume;
        public final ModConfigSpec.DoubleValue soundSpikeVolume;
        public final ModConfigSpec.DoubleValue soundUiVolume;
        public final ModConfigSpec.DoubleValue soundWeaponVolume;
        public final ModConfigSpec.DoubleValue soundAnnouncerVolume;
        public final ModConfigSpec.BooleanValue soundDisableBackgroundMusic;

        // Ability shop (per-agent C/Q/E pricing and caps)
        public final ModConfigSpec.ConfigValue<Object> abilityShop;

        // Drop/Pickup system
        public final ModConfigSpec.ConfigValue<Object> droppableWhitelist; // list<String> ids/tags
        public final ModConfigSpec.ConfigValue<Object> itemTargetSlots;    // map<String,int>
        public final ModConfigSpec.DoubleValue pickupRange;
        public final ModConfigSpec.ConfigValue<String> uiPromptText;
        public final ModConfigSpec.IntValue uiPromptOffsetX;
        public final ModConfigSpec.IntValue uiPromptOffsetY;
        public final ModConfigSpec.IntValue autoDespawnTicks;
        public final ModConfigSpec.DoubleValue classicDropYOffset;
        public final ModConfigSpec.DoubleValue ghostDropYOffset;
        public final ModConfigSpec.DoubleValue spikeDropYOffset;
        public final ModConfigSpec.DoubleValue vandalDropYOffset;
        public final ModConfigSpec.BooleanValue enableDropSfx;
        public final ModConfigSpec.BooleanValue enablePickupSfx;
        public final ModConfigSpec.BooleanValue enableParticles;
        public final ModConfigSpec.BooleanValue allowStackDrop;
        public final ModConfigSpec.BooleanValue enableGlow;

        // Corpses (dead body entity)
        public final ModConfigSpec.BooleanValue corpseEnabled;
        public final ModConfigSpec.BooleanValue corpseDespawnAtRoundStart;
        public final ModConfigSpec.IntValue corpseLifetimeTicks;
        public final ModConfigSpec.BooleanValue corpseCollision;
        public final ModConfigSpec.BooleanValue corpseGlow;

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

            // Agent abilities configuration (defaults per agent/slot)
            builder.push("agent_abilities");
            {
                com.electronwill.nightconfig.core.Config table = com.electronwill.nightconfig.core.Config.inMemory();
                // Phoenix
                table.set("phoenix.c.charges", 1);
                table.set("phoenix.q.charges", 1);
                table.set("phoenix.e.charges", 1);
                table.set("phoenix.x.ult_cost", 6);
                // Brimstone
                table.set("brimstone.c.charges", 1);
                table.set("brimstone.q.charges", 1);
                table.set("brimstone.e.charges", 3);
                table.set("brimstone.x.ult_cost", 8);
                // Jett
                table.set("jett.c.charges", 2);
                table.set("jett.q.charges", 1);
                table.set("jett.e.charges", 1);
                table.set("jett.x.ult_cost", 8);
                // Omen
                table.set("omen.c.charges", 2);
                table.set("omen.q.charges", 1);
                table.set("omen.e.charges", 2);
                table.set("omen.x.ult_cost", 7);
                // Raze
                table.set("raze.c.charges", 1);
                table.set("raze.q.charges", 2);
                table.set("raze.e.charges", 1);
                table.set("raze.x.ult_cost", 8);
                // Sage
                table.set("sage.c.charges", 1);
                table.set("sage.q.charges", 2);
                table.set("sage.e.charges", 1);
                table.set("sage.x.ult_cost", 7);
                // Sova
                table.set("sova.c.charges", 1);
                table.set("sova.q.charges", 2);
                table.set("sova.e.charges", 1);
                table.set("sova.x.ult_cost", 8);
                agentAbilities = builder.comment("Mapping: agent.slot.* defaults (charges / ult_cost)")
                        .define("defaults", table);
            }
            builder.pop();

            // Ability shop configuration (data-first, per agent/slot)
            builder.push("ability_shop");
            {
                com.electronwill.nightconfig.core.Config shop = com.electronwill.nightconfig.core.Config.inMemory();
                // Brimstone
                shop.set("brimstone.c.price", 200);
                shop.set("brimstone.c.max", 1);
                shop.set("brimstone.c.purchasable", true);
                shop.set("brimstone.q.price", 250);
                shop.set("brimstone.q.max", 1);
                shop.set("brimstone.q.purchasable", true);
                shop.set("brimstone.e.price", 100);
                shop.set("brimstone.e.max", 3);
                shop.set("brimstone.e.purchasable", true);
                // Jett
                shop.set("jett.c.price", 200);
                shop.set("jett.c.max", 2);
                shop.set("jett.c.purchasable", true);
                shop.set("jett.q.price", 150);
                shop.set("jett.q.max", 1);
                shop.set("jett.q.purchasable", true);
                shop.set("jett.e.price", 0);
                shop.set("jett.e.max", 1);
                shop.set("jett.e.purchasable", false);
                // Omen
                shop.set("omen.c.price", 100);
                shop.set("omen.c.max", 2);
                shop.set("omen.c.purchasable", true);
                shop.set("omen.q.price", 250);
                shop.set("omen.q.max", 1);
                shop.set("omen.q.purchasable", true);
                shop.set("omen.e.price", 0);
                shop.set("omen.e.max", 2);
                shop.set("omen.e.purchasable", false);
                // Phoenix
                shop.set("phoenix.c.price", 150);
                shop.set("phoenix.c.max", 1);
                shop.set("phoenix.c.purchasable", true);
                shop.set("phoenix.q.price", 200);
                shop.set("phoenix.q.max", 1);
                shop.set("phoenix.q.purchasable", true);
                shop.set("phoenix.e.price", 0);
                shop.set("phoenix.e.max", 1);
                shop.set("phoenix.e.purchasable", false);
                // Raze
                shop.set("raze.c.price", 300);
                shop.set("raze.c.max", 1);
                shop.set("raze.c.purchasable", true);
                shop.set("raze.q.price", 200);
                shop.set("raze.q.max", 2);
                shop.set("raze.q.purchasable", true);
                shop.set("raze.e.price", 0);
                shop.set("raze.e.max", 1);
                shop.set("raze.e.purchasable", false);
                // Sage
                shop.set("sage.c.price", 300);
                shop.set("sage.c.max", 1);
                shop.set("sage.c.purchasable", true);
                shop.set("sage.q.price", 200);
                shop.set("sage.q.max", 2);
                shop.set("sage.q.purchasable", true);
                shop.set("sage.e.price", 0);
                shop.set("sage.e.max", 1);
                shop.set("sage.e.purchasable", false);
                // Sova
                shop.set("sova.c.price", 400);
                shop.set("sova.c.max", 1);
                shop.set("sova.c.purchasable", true);
                shop.set("sova.q.price", 150);
                shop.set("sova.q.max", 2);
                shop.set("sova.q.purchasable", true);
                shop.set("sova.e.price", 0);
                shop.set("sova.e.max", 1);
                shop.set("sova.e.purchasable", false);
                abilityShop = builder.comment("Per-agent ability shop settings: <agent>.<slot>.price/max/purchasable")
                        .define("settings", shop);
            }
            builder.pop();

            builder.pop();
            
            // Drop/Pickup configuration
            builder.push("dropPickup");
            {
                // Whitelist list
                java.util.List<String> wl = new java.util.ArrayList<>();
                wl.add("valorant:classic");
                wl.add("valorant:ghost");
                wl.add("valorant:spike");
                wl.add("valorant:vandal");
                droppableWhitelist = builder.comment("Whitelist of droppable items by id or tag (#namespace:id)")
                        .define("droppableWhitelist", wl);

                // Target slot mapping (id/tag -> slot index)
                com.electronwill.nightconfig.core.Config slotMap = com.electronwill.nightconfig.core.Config.inMemory();
                // Rifles to hotbar slot 1, pistols to slot 2, spike to slot 4
                slotMap.set("valorant:vandal", 0);    // Rifle
                slotMap.set("valorant:classic", 1);   // Pistol
                slotMap.set("valorant:ghost", 1);     // Pistol
                slotMap.set("valorant:spike", 3);     // Spike

                itemTargetSlots = builder.comment("Mapping from item id or tag to target inventory slot index")
                        .define("itemTargetSlots", slotMap);

                pickupRange = builder.comment("Maximum pickup range (blocks)")
                        .defineInRange("pickupRange", 2.0D, 2.0D, 2.0D);

                uiPromptText = builder.comment("Prompt text; {item} replaced with item name; [Use] with key label")
                        .define("uiPromptText", "Press [Use] to pick up {item}");
                uiPromptOffsetX = builder.comment("Prompt X offset from screen center (pixels)")
                        .defineInRange("uiPromptOffsetX", 0, -500, 500);
                uiPromptOffsetY = builder.comment("Prompt Y offset from screen center (pixels)")
                        .defineInRange("uiPromptOffsetY", 80, 80, 80);

                autoDespawnTicks = builder.comment("Auto-despawn timer for dropped stands (0 disables)")
                        .defineInRange("autoDespawnTicks", 0 * 60 * 5, 0, 0 * 60 * 60);

                // Item-specific Y offsets for dropped items
                classicDropYOffset = builder.comment("Y offset added when dropping Classic pistol")
                        .defineInRange("classicDropYOffset", 1.50D, 1.50D, 1.50D);
                ghostDropYOffset = builder.comment("Y offset added when dropping Ghost pistol")
                        .defineInRange("ghostDropYOffset", 1.50D, 1.50D, 1.50D);
                spikeDropYOffset = builder.comment("Y offset added when dropping Spike")
                        .defineInRange("spikeDropYOffset", 1.50D, 1.50D, 1.50D);
                vandalDropYOffset = builder.comment("Y offset added when dropping Vandal rifle")
                        .defineInRange("vandalDropYOffset", 1.50D, 1.50D, 1.50D);

                enableDropSfx = builder.comment("Play sound on drop")
                        .define("enableDropSfx", true);
                enablePickupSfx = builder.comment("Play sound on pickup")
                        .define("enablePickupSfx", true);
                enableParticles = builder.comment("Enable particles on drop/pickup events")
                        .define("enableParticles", true);

                allowStackDrop = builder.comment("Allow dropping entire stacks (otherwise always 1)")
                        .define("allowStackDrop", false);
                enableGlow = builder.comment("Enable glow/outline on dropped stands")
                        .define("enableGlow", false);
            }
            builder.pop();

            // Corpse configuration (dead bodies)
            builder.push("corpse");
            corpseEnabled = builder.comment("Enable spawning corpse entities on player death.")
                    .define("enabled", true);
            corpseDespawnAtRoundStart = builder.comment("Despawn all corpses automatically when a new round (BUY) starts.")
                    .define("despawnAtRoundStart", true);
            corpseLifetimeTicks = builder.comment("Auto-despawn corpses after this many ticks (0 disables).")
                    .defineInRange("lifetimeTicks", 0, 0, 20 * 60 * 30);
            corpseCollision = builder.comment("If true, corpse collides with players; if false, no collision.")
                    .define("collision", false);
            corpseGlow = builder.comment("If true, corpse has glowing outline.")
                    .define("glow", false);
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
                    .defineInRange("segmentSpacing", 0.55D, 0.55D, 0.55D);

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

            // Agent skins configuration (data-first)
            builder.push("agent_skins");
            agentSkinsEnabled = builder.comment("Globally enable/disable overriding player skins with agent textures.")
                    .define("enabled", true);
            agentSkinsDefaultAgent = builder.comment("Agent to use when none is selected/known for skin rendering.")
                    .define("default_agent", "phoenix");
            agentSkinsDisableCapes = builder.comment("If true, capes and elytra textures/flags are disabled for players.")
                    .define("disable_capes", true);
            {
                com.electronwill.nightconfig.core.Config table = com.electronwill.nightconfig.core.Config.inMemory();
                table.set("phoenix", "valorant:textures/agent/phoenix.png");
                table.set("jett", "valorant:textures/agent/jett.png");
                table.set("sova", "valorant:textures/agent/sova.png");
                table.set("sage", "valorant:textures/agent/sage.png");
                table.set("brimstone", "valorant:textures/agent/brimstone.png");
                table.set("raze", "valorant:textures/agent/raze.png");
                table.set("omen", "valorant:textures/agent/omen.png");
                agentSkinsTextures = builder.comment("Mapping: agent id -> texture resource location (namespace:path)")
                        .define("textures", table);
            }
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

            // Player movement settings
            builder.push("playerMovement");
            preventSprinting = builder.comment("Prevent players from sprinting when enabled.")
                    .define("preventSprinting", true);
            builder.pop();

            // Title overlay settings
            builder.push("titleOverlay");
            titleFadeInTicks = builder.comment("Ticks for title overlay to fade in.")
                    .defineInRange("fadeInTicks", 10, 0, 100);
            titleStayTicks = builder.comment("Ticks for title overlay to stay visible.")
                    .defineInRange("stayTicks", 700, 0, 400);
            titleFadeOutTicks = builder.comment("Ticks for title overlay to fade out.")
                    .defineInRange("fadeOutTicks", 10, 0, 100);
            builder.pop();

            // Killfeed settings
            builder.push("killfeed");
            killfeedEnabled = builder.comment("Enable the Valorant-style killfeed overlay (top-right).")
                    .define("enabled", true);
            killfeedDurationTicks = builder.comment("How long each killfeed entry stays visible (ticks). 60 = 3s.")
                    .defineInRange("durationTicks", 60, 1, 20 * 30);
            killfeedMaxMessages = builder.comment("Maximum number of stacked killfeed messages to display.")
                    .defineInRange("maxMessages", 3, 1, 10);
            killfeedOffsetRight = builder.comment("Right margin (pixels) from the screen edge for killfeed.")
                    .defineInRange("offsetRight", 16, 0, 1000);
            killfeedOffsetDown = builder.comment("Vertical offset (pixels) below the top team bar.")
                    .defineInRange("offsetDown", 6, 0, 1000);
            killfeedLineSpacing = builder.comment("Vertical spacing (pixels) between killfeed rows.")
                    .defineInRange("lineSpacing", 4, 0, 100);
            killfeedKillerBgColor = builder.comment("ARGB color for killer+weapon chip background.")
                    .defineInRange("killerBgColor", 0xFF2ECC71, Integer.MIN_VALUE, Integer.MAX_VALUE);
            killfeedVictimBgColor = builder.comment("ARGB color for victim chip background.")
                    .defineInRange("victimBgColor", 0xFFE74C3C, Integer.MIN_VALUE, Integer.MAX_VALUE);
            killfeedTextColor = builder.comment("ARGB text color used on killfeed chips.")
                    .defineInRange("textColor", 0xFFFFFFFF, Integer.MIN_VALUE, Integer.MAX_VALUE);
            builder.pop();

            // Spike icon under timer
            builder.push("spikeIcon");
            spikeIconWidth = builder.comment("Spike icon width in pixels (rendered under timer)")
                    .defineInRange("width", 1, 1, 1);
            spikeIconHeight = builder.comment("Spike icon height in pixels (rendered under timer)")
                    .defineInRange("height", 1, 1, 1);
            spikeIconRotationDegrees = builder.comment("Spike icon rotation around center (degrees)")
                    .defineInRange("rotationDegrees", 135, 135, 135);
            spikeIconOffsetX = builder.comment("Spike icon X offset from horizontal center (pixels)")
                    .defineInRange("offsetX", 0, 0, 0);
            spikeIconOffsetY = builder.comment("Spike icon Y offset below the timer chip (pixels)")
                    .defineInRange("offsetY", 20, 20, 20);
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

            // Frenzy
            builder.push("frenzy");
            frenzyDamage = builder.comment("Damage per shot for Frenzy pistol.")
                    .defineInRange("damage", 6.0D, 0.0D, 100.0D);
            frenzyRange = builder.comment("Max range in blocks for Frenzy pistol.")
                    .defineInRange("range", 1000.0D, 1000.0D, 1000.0D);
            frenzySpreadDegrees = builder.comment("Random spread in degrees for Frenzy pistol.")
                    .defineInRange("spreadDegrees", 1.25D, 0.0D, 15.0D);
            frenzyCooldownTicks = builder.comment("Cooldown (fire rate) in ticks for Frenzy pistol.")
                    .defineInRange("cooldownTicks", 8, 0, 40);
            frenzyTracerParticles = builder.comment("Tracer particle steps for Frenzy pistol (visual only).")
                    .defineInRange("tracerSteps", 5, 5, 5);
            frenzyMuzzleParticles = builder.comment("Muzzle particle count for Frenzy pistol (visual only).")
                    .defineInRange("muzzleParticles", 4, 1, 32);
            frenzyMagazineSize = builder.comment("Magazine size for Frenzy pistol.")
                    .defineInRange("magazineSize", 12, 1, 100);
            frenzyMaxReserveAmmo = builder.comment("Maximum reserve ammo for Frenzy pistol.")
                    .defineInRange("maxReserveAmmo", 36, 0, 500);
            frenzyReloadTimeTicks = builder.comment("Reload time in ticks for Frenzy pistol (40 ticks = 2.0 seconds).")
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

            // Knife configuration
            builder.push("knife");
            knifeSpeedMultiplier = builder.comment("Movement speed multiplier when holding knife (1.0 = normal speed, 1.2 = 20% faster).")
                    .defineInRange("speedMultiplier", 1.2D, 0.1D, 5.0D);
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

            // Sound configuration
            builder.push("sound");
            soundEnabled = builder.comment("Enable all custom Valorant sounds.")
                    .define("enabled", true);
            soundReloadVolume = builder.comment("Volume scale for reload sounds (0.0 - 1.0).")
                    .defineInRange("reloadVolume", 1.0D, 1.0D, 1.0D);
            soundSpikeVolume = builder.comment("Volume scale for spike plant/defuse sounds (0.0 - 1.0).")
                    .defineInRange("spikeVolume", 1.0D, 1.0D, 1.0D);
            soundUiVolume = builder.comment("Volume scale for UI sounds (buy success/failure) (0.0 - 1.0).")
                    .defineInRange("uiVolume", 1.0D, 1.0D, 1.0D);
            soundWeaponVolume = builder.comment("Volume scale for weapon sounds (shots, equips) (0.0 - 1.0).")
                    .defineInRange("weaponVolume", 1.0D, 1.0D, 1.0D);
            soundAnnouncerVolume = builder.comment("Volume scale for announcer sounds (0.0 - 1.0).")
                    .defineInRange("announcerVolume", 1.0D, 1.0D, 1.0D);
            soundDisableBackgroundMusic = builder.comment("Disable Minecraft's background music while playing.")
                    .define("disableBackgroundMusic", true);
            builder.pop();

            // Spike section (runtime flags)
            builder.push("spike");
            spikePlanted = builder.comment("Runtime flag set to true when Spike is planted. Server-updated.")
                    .define("planted", false);
            plantedSpikeYOffset = builder.comment("Vertical offset applied to the planted spike ArmorStand (negative sinks it).")
                    .defineInRange("plantedSpikeYOffset", -1.6D, -1.6D, -1.60D);
            spikePlantHoldTicks = builder.comment("Ticks required to plant the spike (20 ticks = 1 second).")
                    .defineInRange("plantHoldTicks", 88, 88, 88);
            lockMovementWhilePlanting = builder.comment("If true, lock player horizontal movement while planting the spike.")
                    .define("lockMovementWhilePlanting", true);
            lockMovementWhileDefusing = builder.comment("If true, lock player horizontal movement while defusing the spike.")
                    .define("lockMovementWhileDefusing", true);
            builder.pop();

            // Team settings
            builder.push("teams");
            maxTeamSize = builder.comment("Maximum players per team (A and V).")
                    .defineInRange("maxTeamSize", 5, 1, 10);
            builder.pop();

            // FancyMenu integration
            builder.push("fancymenu");
            enableFancyMenuTeamVars = builder.comment("Enable syncing team player slots to FancyMenu variables.")
                    .define("enableTeamVariables", true);
            teamVarSlots = builder.comment("Number of player slots per team to expose to FancyMenu (1..10).")
                    .defineInRange("teamVarSlots", 10, 1, 10);
            fancymenuVariablePrefix = builder.comment("Optional prefix prepended to FancyMenu variable names (e.g., 'valo-').")
                    .define("variablePrefix", "");
            builder.pop();

            // Spawn areas configuration
            builder.push("spawnAreas");
            spawnAreaConfigPath = builder.comment("Path to JSON file describing team spawn polygons per dimension.")
                            .define("configPath", "config/valorant/spawn_areas.json");
            spawnAreaParticleType = builder.comment("Particle id used to outline spawn area perimeters (e.g., FLAME, GLOW, ASH).")
                            .define("particle", "DUST");
            spawnAreaParticleSpacing = builder.comment("Spacing in blocks between perimeter particles when rendering spawn areas.")
                            .defineInRange("particleSpacing", 0.5D, 0.1D, 5.0D);
            spawnAreaParticleTickInterval = builder.comment("How often (in ticks) to render the perimeter particles (lower = more frequent).")
                            .defineInRange("particleTickInterval", 10, 1, 200);
            bombSiteParticleType = builder.comment("Particle id used to outline bomb site perimeters (A/B/C).")
                            .define("bombSiteParticle", "DUST");
            spawnAreaParticleColor = builder.comment("RGB hex color used when particle supports color (e.g., DUST). Format: 0xRRGGBB")
                            .defineInRange("spawnColor", 0x00FFFF, 0x000000, 0xFFFFFF);
            bombSiteParticleColor = builder.comment("RGB hex color used when particle supports color (e.g., DUST). Format: 0xRRGGBB")
                            .defineInRange("bombColor", 0xFF0000, 0x000000, 0xFFFFFF);
            builder.pop();

            // Sky Smoke settings
            builder.push("sky_smoke");
            skySmokeMapDimensionId = builder.comment("Dimension ID where Sky Smoke map bounds apply.")
                    .define("mapDimensionId", "minecraft:overworld");
            skySmokeMapMinX = builder.comment("Minimum X coordinate of the Sky Smoke map in world space.")
                    .defineInRange("mapMinX", 0, -1000000, 1000000);
            skySmokeMapMinZ = builder.comment("Minimum Z coordinate of the Sky Smoke map in world space.")
                    .defineInRange("mapMinZ", 0, -1000000, 1000000);
            skySmokeMapMaxX = builder.comment("Maximum X coordinate of the Sky Smoke map in world space.")
                    .defineInRange("mapMaxX", 512, -1000000, 1000000);
            skySmokeMapMaxZ = builder.comment("Maximum Z coordinate of the Sky Smoke map in world space.")
                    .defineInRange("mapMaxZ", 512, -1000000, 1000000);
            skySmokeMapRotationDegrees = builder.comment("Rotation of the Sky Smoke map in degrees (clockwise, 0 = north is up).")
                    .defineInRange("mapRotationDegrees", 0.0D, -180.0D, 180.0D);
            // Calibration model/settings
            skySmokeCalibrationModel = builder.comment("Calibration model used to fit GUI map to world: SIMILARITY, AFFINE, or HOMOGRAPHY")
                    .define("calibrationModel", "HOMOGRAPHY");
            skySmokeCalibrationMinPoints = builder.comment("Minimum number of calibration anchors to collect before preview/apply.")
                    .defineInRange("calibrationMinPoints", 4, 3, 16);
            skySmokeCalibrationUseRansac = builder.comment("Use RANSAC to robustly fit the transform and reject outliers.")
                    .define("calibrationUseRansac", true);
            skySmokeCalibrationRansacIterations = builder.comment("RANSAC iterations for calibration model fitting.")
                    .defineInRange("calibrationRansacIterations", 300, 1, 10000);
            skySmokeCalibrationRansacThreshold = builder.comment("RANSAC inlier threshold (blocks) measured as reprojection error in world XZ.")
                    .defineInRange("calibrationRansacThreshold", 1.0D, 0.01D, 10.0D);
            skySmokeCalibrationUiZoomFactor = builder.comment("GUI calibration zoom factor when holding Shift (1.0 = disabled)")
                    .defineInRange("calibrationUiZoomFactor", 2.0D, 1.0D, 8.0D);
            skySmokeCalibrationUiEnableSnap = builder.comment("Enable Ctrl snapping during calibration to pixel centers of the map texture.")
                    .define("calibrationUiEnableSnap", true);
            skySmokeMaxPerCast = builder.comment("Maximum Sky Smoke placements per cast.")
                    .defineInRange("maxPerCast", 3, 1, 10);
            skySmokeRadius = builder.comment("Radius in blocks for Sky Smoke clouds.")
                    .defineInRange("radius", 4.5D, 0.5D, 20.0D);
            skySmokeDurationTicks = builder.comment("Duration in ticks for Sky Smoke clouds.")
                    .defineInRange("durationTicks", 280, 20, 20 * 60);
            skySmokePlaceOnGround = builder.comment("If true, place Sky Smoke on ground heightmap; else use player Y + yOffset.")
                    .define("placeOnGround", true);
            skySmokeYOffset = builder.comment("Y offset added when not placing on ground (blocks).")
                    .defineInRange("yOffset", 0.1D, -10.0D, 10.0D);
            skySmokeSpawnXOffset = builder.comment("X offset added to smoke spawn position after coordinate transformation.")
                    .defineInRange("spawnXOffset", 0.0D, -10.0D, 10.0D);
            skySmokeSpawnZOffset = builder.comment("Z offset added to smoke spawn position after coordinate transformation.")
                    .defineInRange("spawnZOffset", 0.0D, 0.0D, 0.0D);
            skySmokeApplyBlindness = builder.comment("If true, Sky Smoke clouds apply blindness effect.")
                    .define("applyBlindness", true);
            skySmokeBlindnessRadiusOffset = builder.comment("Offset subtracted from smoke radius for blindness application (player must be closer to center).")
                    .defineInRange("blindnessRadiusOffset", 1.5D, 0.0D, 10.0D);
            skySmokeBlindnessTicks = builder.comment("Duration of blindness effect in ticks (only used if applyBlindness is true).")
                    .defineInRange("blindnessTicks", 255, 255, 20 * 60);
            skySmokeAreasConfigPath = builder.comment("Path to JSON file describing Sky Smoke allowed/blocked zones per dimension.")
                    .define("areasConfigPath", "config/valorant/sky_smoke_areas.json");

            // Sky Smoke zones rendering
            builder.push("areasRendering");
            skySmokeAreasParticleType = builder.comment("Particle type for Sky Smoke zone rendering (e.g., DUST, FLAME, GLOW).")
                    .define("particleType", "DUST");
            skySmokeAreasParticleSpacing = builder.comment("Spacing in blocks between zone perimeter particles.")
                    .defineInRange("particleSpacing", 1.0D, 0.5D, 10.0D);
            skySmokeAreasParticleTickInterval = builder.comment("How often (in ticks) to render zone particles (lower = more frequent).")
                    .defineInRange("particleTickInterval", 20, 1, 200);
            skySmokeAreasAllowedParticleColor = builder.comment("RGB hex color for allowed zone particles (e.g., 0x00FF00 for green).")
                    .defineInRange("allowedParticleColor", 0x00FF00, 0x000000, 0xFFFFFF);
            skySmokeAreasBlockedParticleColor = builder.comment("RGB hex color for blocked zone particles (e.g., 0xFF0000 for red).")
                    .defineInRange("blockedParticleColor", 0xFF0000, 0x000000, 0xFFFFFF);
            skySmokeAreasShowRecordingParticles = builder.comment("If true, show particles at recorded points during zone recording.")
                    .define("showRecordingParticles", true);
            skySmokeAreasRecordingParticleColor = builder.comment("RGB hex color for recording point particles (e.g., 0xFFFF00 for yellow).")
                    .defineInRange("recordingParticleColor", 0xFFFF00, 0x000000, 0xFFFFFF);
            builder.pop();

            builder.pop();

            // Sky Smoke map UI
            builder.push("sky_smoke_ui");
            skySmokeUiShowPlayerMarker = builder.comment("Show a live player marker on the Sky Smoke map.")
                    .define("showPlayerMarker", true);
            skySmokeUiFollowPlayer = builder.comment("Center and zoom the map view on the player position.")
                    .define("followPlayer", true);
            skySmokeUiFollowZoomFactor = builder.comment("Zoom factor when following player (2.0 = 2x zoom, higher = closer).")
                    .defineInRange("followZoomFactor", 2.0D, 1.0D, 8.0D);
            skySmokeUiPlayerMarkerColor = builder.comment("ARGB color for the player marker on the map.")
                    .defineInRange("playerMarkerColor", 0xFFFF4D00, Integer.MIN_VALUE, Integer.MAX_VALUE);
            skySmokeUiPlayerMarkerSize = builder.comment("Marker size (pixels) for the player marker on the map.")
                    .defineInRange("playerMarkerSize", 6, 2, 24);
            builder.pop();

            // Molly Launcher
            builder.push("molly_launcher");
            mollyLauncherDamage = builder.comment("Damage per shot for Molly Launcher.")
                    .defineInRange("damage", 10.0D, 0.0D, 100.0D);
            mollyLauncherRange = builder.comment("Max range in blocks for Molly Launcher.")
                    .defineInRange("range", 100.0D, 1.0D, 256.0D);
            mollyLauncherSpreadDegrees = builder.comment("Random spread in degrees for Molly Launcher.")
                    .defineInRange("spreadDegrees", 0.75D, 0.0D, 15.0D);
            mollyLauncherCooldownTicks = builder.comment("Cooldown (fire rate) in ticks for Molly Launcher.")
                    .defineInRange("cooldownTicks", 4, 0, 40);
            mollyLauncherTracerParticles = builder.comment("Tracer particle steps for Molly Launcher (visual only).")
                    .defineInRange("tracerSteps", 24, 1, 128);
            mollyLauncherMuzzleParticles = builder.comment("Muzzle particle count for Molly Launcher (visual only).")
                    .defineInRange("muzzleParticles", 6, 1, 32);
            mollyLauncherMagazineSize = builder.comment("Magazine size for Molly Launcher.")
                    .defineInRange("magazineSize", 25, 1, 100);
            mollyLauncherMaxReserveAmmo = builder.comment("Maximum reserve ammo for Molly Launcher.")
                    .defineInRange("maxReserveAmmo", 75, 0, 500);
            mollyLauncherReloadTimeTicks = builder.comment("Reload time in ticks for Molly Launcher (50 ticks = 2.5 seconds).")
                    .defineInRange("reloadTimeTicks", 50, 1, 20 * 30);
            builder.pop();
        }
    }
}
