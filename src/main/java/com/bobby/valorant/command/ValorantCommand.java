package com.bobby.valorant.command;

import com.bobby.valorant.Config;
import com.bobby.valorant.ability.Abilities;
import com.bobby.valorant.ability.Ability;
import com.bobby.valorant.network.SyncAgentS2CPacket;
import com.bobby.valorant.network.SyncAbilityStateS2CPacket;
import com.bobby.valorant.network.SyncAgentLocksPacket;
import com.bobby.valorant.player.AbilityStateData;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.player.CurveballData;
import com.bobby.valorant.player.FireballData;
import com.bobby.valorant.round.AgentLockManager;
import com.bobby.valorant.round.RoundController;
import com.bobby.valorant.round.TeamManager;
import com.bobby.valorant.server.TitleMessages;
import com.bobby.valorant.util.ParticleScheduler;
import com.bobby.valorant.world.agent.Agent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ValorantCommand {
    private ValorantCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        // -------- /valorant spawnarea --------
        var spawnareaRecordStart =
            Commands.literal("start")
                .then(
                    Commands.argument("team", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            String team = StringArgumentType.getString(ctx, "team");
                            boolean ok = com.bobby.valorant.spawn.SpawnAreaRecording.start(sp, team, null);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Spawn area recording started for team " + (team.equalsIgnoreCase("A") ? "A" : "V")), false);
                            else ctx.getSource().sendFailure(Component.literal("Failed to start recording"));
                            return ok ? 1 : 0;
                        })
                        .then(
                            Commands.argument("y", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    String team = StringArgumentType.getString(ctx, "team");
                                    int y = IntegerArgumentType.getInteger(ctx, "y");
                                    boolean ok = com.bobby.valorant.spawn.SpawnAreaRecording.start(sp, team, y);
                                    if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Spawn area recording started for team " + (team.equalsIgnoreCase("A") ? "A" : "V") + " at y=" + y), false);
                                    else ctx.getSource().sendFailure(Component.literal("Failed to start recording"));
                                    return ok ? 1 : 0;
                                })
                        )
                );

        var spawnareaRecord =
            Commands.literal("record")
                .then(spawnareaRecordStart)
                .then(
                    Commands.literal("add")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            boolean ok = com.bobby.valorant.spawn.SpawnAreaRecording.addPoint(sp);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Point added at your current XZ"), false);
                            else ctx.getSource().sendFailure(Component.literal("No active recording; use /valorant spawnarea record start"));
                            return ok ? 1 : 0;
                        })
                )
                .then(
                    Commands.literal("undo")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            boolean ok = com.bobby.valorant.spawn.SpawnAreaRecording.undo(sp);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Removed last point"), false);
                            else ctx.getSource().sendFailure(Component.literal("Nothing to undo or no active recording"));
                            return ok ? 1 : 0;
                        })
                )
                .then(
                    Commands.literal("clear")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            boolean ok = com.bobby.valorant.spawn.SpawnAreaRecording.clear(sp);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Cleared all points"), false);
                            else ctx.getSource().sendFailure(Component.literal("No active recording"));
                            return ok ? 1 : 0;
                        })
                )
                .then(
                    Commands.literal("sety")
                        .then(
                            Commands.argument("y", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    int y = IntegerArgumentType.getInteger(ctx, "y");
                                    boolean ok = com.bobby.valorant.spawn.SpawnAreaRecording.setY(sp, y);
                                    if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Recording Y set to " + y), false);
                                    else ctx.getSource().sendFailure(Component.literal("No active recording"));
                                    return ok ? 1 : 0;
                                })
                        )
                )
                .then(
                    Commands.literal("save")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            boolean ok = com.bobby.valorant.spawn.SpawnAreaRecording.save(sp);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Spawn area saved and synced"), true);
                            else ctx.getSource().sendFailure(Component.literal("Need at least 3 points or no active recording"));
                            return ok ? 1 : 0;
                        })
                )
                .then(
                    Commands.literal("cancel")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            boolean ok = com.bobby.valorant.spawn.SpawnAreaRecording.cancel(sp);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Recording canceled"), false);
                            else ctx.getSource().sendFailure(Component.literal("No active recording"));
                            return ok ? 1 : 0;
                        })
                )
                .then(
                    Commands.literal("status")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            String status = com.bobby.valorant.spawn.SpawnAreaRecording.status(sp);
                            ctx.getSource().sendSuccess(() -> Component.literal(status), false);
                            return 1;
                        })
                );

        var spawnareaReload =
            Commands.literal("reload").executes(ctx -> {
                var server = ctx.getSource().getServer();
                if (server == null) return 0;
                com.bobby.valorant.spawn.SpawnAreaManager.load(server);
                com.bobby.valorant.spawn.SkySmokeCalibration.load(server);
                for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                    com.bobby.valorant.spawn.SpawnAreaManager.syncFor(sp);
                }
                ctx.getSource().sendSuccess(() -> Component.literal("Reloaded spawn areas and sky smoke calibration"), true);
                return 1;
            });

        var spawnarea =
            Commands.literal("spawnarea").requires(s -> s.hasPermission(2))
                .then(spawnareaRecord)
                .then(spawnareaReload);

        // -------- /valorant bombsite --------
        var bombsiteRecordStart =
            Commands.literal("start")
                .then(
                    Commands.argument("site", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            String site = StringArgumentType.getString(ctx, "site");
                            boolean ok = com.bobby.valorant.spawn.BombSiteRecording.start(sp, site, null);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Bomb site recording started for site " + site.toUpperCase()), false);
                            else ctx.getSource().sendFailure(Component.literal("Failed to start recording"));
                            return ok ? 1 : 0;
                        })
                        .then(
                            Commands.argument("y", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    String site = StringArgumentType.getString(ctx, "site");
                                    int y = IntegerArgumentType.getInteger(ctx, "y");
                                    boolean ok = com.bobby.valorant.spawn.BombSiteRecording.start(sp, site, y);
                                    if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Bomb site recording started for site " + site.toUpperCase() + " at y=" + y), false);
                                    else ctx.getSource().sendFailure(Component.literal("Failed to start recording"));
                                    return ok ? 1 : 0;
                                })
                        )
                );

        var bombsiteRecord =
            Commands.literal("record")
                .then(bombsiteRecordStart)
                .then(
                    Commands.literal("add")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            boolean ok = com.bobby.valorant.spawn.BombSiteRecording.addPoint(sp);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Point added at your current XZ"), false);
                            else ctx.getSource().sendFailure(Component.literal("No active recording; use /valorant bombsite record start"));
                            return ok ? 1 : 0;
                        })
                )
                .then(
                    Commands.literal("undo")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            boolean ok = com.bobby.valorant.spawn.BombSiteRecording.undo(sp);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Removed last point"), false);
                            else ctx.getSource().sendFailure(Component.literal("Nothing to undo or no active recording"));
                            return ok ? 1 : 0;
                        })
                )
                .then(
                    Commands.literal("clear")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            boolean ok = com.bobby.valorant.spawn.BombSiteRecording.clear(sp);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Cleared all points"), false);
                            else ctx.getSource().sendFailure(Component.literal("No active recording"));
                            return ok ? 1 : 0;
                        })
                )
                .then(
                    Commands.literal("sety")
                        .then(
                            Commands.argument("y", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    int y = IntegerArgumentType.getInteger(ctx, "y");
                                    boolean ok = com.bobby.valorant.spawn.BombSiteRecording.setY(sp, y);
                                    if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Recording Y set to " + y), false);
                                    else ctx.getSource().sendFailure(Component.literal("No active recording"));
                                    return ok ? 1 : 0;
                                })
                        )
                )
                .then(
                    Commands.literal("save")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            boolean ok = com.bobby.valorant.spawn.BombSiteRecording.save(sp);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Bomb site saved and synced"), true);
                            else ctx.getSource().sendFailure(Component.literal("Need at least 3 points or no active recording"));
                            return ok ? 1 : 0;
                        })
                )
                .then(
                    Commands.literal("cancel")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            boolean ok = com.bobby.valorant.spawn.BombSiteRecording.cancel(sp);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Recording canceled"), false);
                            else ctx.getSource().sendFailure(Component.literal("No active recording"));
                            return ok ? 1 : 0;
                        })
                )
                .then(
                    Commands.literal("status")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            String status = com.bobby.valorant.spawn.BombSiteRecording.status(sp);
                            ctx.getSource().sendSuccess(() -> Component.literal(status), false);
                            return 1;
                        })
                );

        var bombsite =
            Commands.literal("bombsite").requires(s -> s.hasPermission(2))
                .then(bombsiteRecord);

        // -------- /valorant agent --------
        var agentUnlock =
            Commands.literal("unlock")
                .executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    return unlockAgents(ctx.getSource(), List.of(sp));
                })
                .then(
                    Commands.argument("players", EntityArgument.players())
                        .executes(ctx -> unlockAgents(ctx.getSource(), EntityArgument.getPlayers(ctx, "players")))
                );

        var agentLock =
            Commands.literal("lock")
                .then(
                    Commands.argument("agent", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            String agentId = StringArgumentType.getString(ctx, "agent");
                            return lockAgents(ctx.getSource(), List.of(sp), agentId);
                        })
                        .then(
                            Commands.argument("players", EntityArgument.players())
                                .executes(ctx -> {
                                    String agentId = StringArgumentType.getString(ctx, "agent");
                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
                                    return lockAgents(ctx.getSource(), players, agentId);
                                })
                        )
                );

        var agent =
            Commands.literal("agent").requires(s -> s.hasPermission(2))
                .then(agentUnlock)
                .then(agentLock);

        // -------- /valorant particle --------
        var particle =
            Commands.literal("particle").requires(s -> s.hasPermission(2))
                .then(
                    Commands.argument("particle", ParticleArgument.particle(buildContext))
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            ParticleOptions po = ParticleArgument.getParticle(ctx, "particle");
                            int duration = Config.COMMON.particleCommandDefaultDurationTicks.get();
                            spawnScheduledParticles(sp, po, 0, 0, 0, 0.0D, 100, duration);
                            ctx.getSource().sendSuccess(() -> Component.literal("Scheduled particles for " + duration + " ticks"), false);
                            return 1;
                        })
                        .then(
                            Commands.argument("duration", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    ParticleOptions po = ParticleArgument.getParticle(ctx, "particle");
                                    int duration = IntegerArgumentType.getInteger(ctx, "duration");
                                    spawnScheduledParticles(sp, po, 0, 0, 0, 0.0D, 100, duration);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Scheduled particles for " + duration + " ticks"), false);
                                    return 1;
                                })
                                .then(
                                    Commands.argument("count", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("dx", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("dy", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("dz", DoubleArgumentType.doubleArg())
                                                    .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.0D))
                                                        .executes(ctx -> {
                                                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                            ParticleOptions po = ParticleArgument.getParticle(ctx, "particle");
                                                            int duration = IntegerArgumentType.getInteger(ctx, "duration");
                                                            int count = IntegerArgumentType.getInteger(ctx, "count");
                                                            double dx = DoubleArgumentType.getDouble(ctx, "dx");
                                                            double dy = DoubleArgumentType.getDouble(ctx, "dy");
                                                            double dz = DoubleArgumentType.getDouble(ctx, "dz");
                                                            double speed = DoubleArgumentType.getDouble(ctx, "speed");
                                                            spawnScheduledParticles(sp, po, dx, dy, dz, speed, count, duration);
                                                            ctx.getSource().sendSuccess(() -> Component.literal("Scheduled particles for " + duration + " ticks"), false);
                                                            return 1;
                                                        })
                                                    )
                                                )
                                            )
                                        )
                                )
                        )
                );

        // -------- sky smoke calibration --------
        var skySmokeCalibration =
            Commands.literal("skysmoke")
                .then(
                    Commands.literal("bounds")
                        .then(
                            Commands.literal("get").requires(s -> s.hasPermission(2))
                                .executes(ctx -> {
                                    var bounds = com.bobby.valorant.spawn.SkySmokeCalibration.getBounds();
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                        "Sky Smoke Map Bounds: " +
                                        "Dim=" + Config.COMMON.skySmokeMapDimensionId.get() + ", " +
                                        "Min=" + bounds.toMinString() + ", " +
                                        "Max=" + bounds.toMaxString()
                                    ), false);
                                    return 1;
                                })
                        )
                        .then(
                            Commands.literal("set").requires(s -> s.hasPermission(2))
                                .then(
                                    Commands.argument("minX", IntegerArgumentType.integer())
                                        .then(
                                            Commands.argument("minZ", IntegerArgumentType.integer())
                                                .then(
                                                    Commands.argument("maxX", IntegerArgumentType.integer())
                                                        .then(
                                                            Commands.argument("maxZ", IntegerArgumentType.integer())
                                                                .executes(ctx -> {
                                                                    int minX = IntegerArgumentType.getInteger(ctx, "minX");
                                                                    int minZ = IntegerArgumentType.getInteger(ctx, "minZ");
                                                                    int maxX = IntegerArgumentType.getInteger(ctx, "maxX");
                                                                    int maxZ = IntegerArgumentType.getInteger(ctx, "maxZ");

                                                                    // Basic validation
                                                                    if (maxX <= minX || maxZ <= minZ) {
                                                                        ctx.getSource().sendFailure(Component.literal("Max coordinates must be greater than min coordinates"));
                                                                        return 0;
                                                                    }

                                                                    // Save to calibration file (similar to spawn areas)
                                                                    com.bobby.valorant.spawn.SkySmokeCalibration.putAndSave(
                                                                        ctx.getSource().getServer(),
                                                                        minX, minZ, maxX, maxZ
                                                                    );

                                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                                        "Sky Smoke bounds saved: " +
                                                                        minX + "," + minZ + " to " + maxX + "," + maxZ +
                                                                        ". Changes will persist across restarts."
                                                                    ), false);
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("test").requires(s -> s.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ServerLevel level = (ServerLevel) player.level();

                            // Check if player is in the correct dimension
                            String expectedDim = Config.COMMON.skySmokeMapDimensionId.get();
                            if (!level.dimension().location().toString().equals(expectedDim)) {
                                ctx.getSource().sendFailure(Component.literal("You must be in dimension: " + expectedDim));
                                return 0;
                            }

                            // Create a test smoke at player's position
                            var cloud = new AreaEffectCloud(level,
                                player.getX(), player.getY(), player.getZ());
                            cloud.setRadius(Config.COMMON.skySmokeRadius.get().floatValue());
                            cloud.setRadiusPerTick(0.0F);
                            cloud.setDuration(Config.COMMON.skySmokeDurationTicks.get());
                            cloud.setWaitTime(0);

                            // Add blindness if configured
                            if (Config.COMMON.skySmokeApplyBlindness.get()) {
                                cloud.addEffect(new MobEffectInstance(
                                    MobEffects.BLINDNESS,
                                    Config.COMMON.skySmokeBlindnessTicks.get(), 0, true, false));
                            }

                            level.addFreshEntity(cloud);

                            // Add smoke particles for visibility
                            level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                                player.getX(), player.getY() + 0.5D, player.getZ(),
                                20, 0.3D, 0.3D, 0.3D, 0.01D);

                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "Test smoke spawned at your position (" +
                                String.format("%.1f, %.1f, %.1f", player.getX(), player.getY(), player.getZ()) + ")"
                            ), false);
                            return 1;
                        })
                )
                .then(
                    Commands.literal("corners").requires(s -> s.hasPermission(2))
                        .then(
                            Commands.literal("tp").requires(s -> s.hasPermission(2))
                                .then(
                                    Commands.argument("corner", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            String corner = StringArgumentType.getString(ctx, "corner").toLowerCase();

                                            var bounds = com.bobby.valorant.spawn.SkySmokeCalibration.getBounds();
                                            int minX = bounds.minX();
                                            int minZ = bounds.minZ();
                                            int maxX = bounds.maxX();
                                            int maxZ = bounds.maxZ();

                                            double x, z;
                                            switch (corner) {
                                                case "nw", "northwest" -> { x = minX + 0.5; z = minZ + 0.5; }
                                                case "ne", "northeast" -> { x = maxX - 0.5; z = minZ + 0.5; }
                                                case "sw", "southwest" -> { x = minX + 0.5; z = maxZ - 0.5; }
                                                case "se", "southeast" -> { x = maxX - 0.5; z = maxZ - 0.5; }
                                                case "center", "middle" -> { x = (minX + maxX) / 2.0; z = (minZ + maxZ) / 2.0; }
                                                default -> {
                                                    ctx.getSource().sendFailure(Component.literal(
                                                        "Invalid corner. Use: nw/ne/sw/se/center"));
                                                    return 0;
                                                }
                                            }

                                            // Teleport player to corner at current Y level
                                            player.teleportTo(x, player.getY(), z);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                "Teleported to " + corner.toUpperCase() + " corner (" +
                                                String.format("%.1f, %.1f", x, z) + ")"
                                            ), false);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(
                    Commands.literal("calibrate").requires(s -> s.hasPermission(2))
                        .then(
                            Commands.literal("start")
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    ResourceLocation dim = sp.level().dimension().location();
                                    String expectedDim = Config.COMMON.skySmokeMapDimensionId.get();

                                    if (!dim.toString().equals(expectedDim)) {
                                        ctx.getSource().sendFailure(Component.literal("You must be in dimension: " + expectedDim));
                                        return 0;
                                    }

                                    // Start calibration session
                                    com.bobby.valorant.spawn.SkySmokeCalibration.startCalibration(sp.getServer(), dim);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Started Sky Smoke calibration. Stand at point 1 and run '/valorant skysmoke calibrate point'"), false);
                                    return 1;
                                })
                        )
                        .then(
                            Commands.literal("point")
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    ResourceLocation dim = sp.level().dimension().location();

                                    // Try to add calibration point
                                    int result = com.bobby.valorant.spawn.SkySmokeCalibration.addCalibrationPoint(sp.getServer(), dim, sp);
                                    if (result == -1) {
                                        ctx.getSource().sendFailure(Component.literal("Calibration not started. Run '/valorant skysmoke calibrate start' first"));
                                        return 0;
                                    } else if (result == -2) {
                                        ctx.getSource().sendFailure(Component.literal("Wrong dimension for calibration"));
                                        return 0;
                                    } else if (result == -3) {
                                        ctx.getSource().sendFailure(Component.literal("Calibration already complete"));
                                        return 0;
                                    }

                                    // Success - GUI will open
                                    ctx.getSource().sendSuccess(() -> Component.literal("Point " + result + " captured. Click on the map to select the corresponding location."), false);
                                    return 1;
                                })
                        )
                        .then(
                            Commands.literal("preview")
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    ResourceLocation dim = sp.level().dimension().location();

                                    boolean success = com.bobby.valorant.spawn.SkySmokeCalibration.previewCalibration(sp.getServer(), dim, sp.level());
                                    if (!success) {
                                        int minPts = Math.max(3, com.bobby.valorant.Config.COMMON.skySmokeCalibrationMinPoints.get());
                                        ctx.getSource().sendFailure(Component.literal("Calibration not complete or invalid. Need at least " + minPts + " points to preview."));
                                        return 0;
                                    }

                                    ctx.getSource().sendSuccess(() -> Component.literal("Calibration preview active. Map corners shown as particles."), false);
                                    return 1;
                                })
                        )
                        .then(
                            Commands.literal("apply")
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    ResourceLocation dim = sp.level().dimension().location();

                                    boolean success = com.bobby.valorant.spawn.SkySmokeCalibration.applyCalibration(sp.getServer(), dim);
                                    if (!success) {
                                        ctx.getSource().sendFailure(Component.literal("Calibration not complete or invalid."));
                                        return 0;
                                    }

                                    ctx.getSource().sendSuccess(() -> Component.literal("Calibration applied and saved!"), true);
                                    return 1;
                                })
                        )
                        .then(
                            Commands.literal("cancel")
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    ResourceLocation dim = sp.level().dimension().location();

                                    com.bobby.valorant.spawn.SkySmokeCalibration.cancelCalibration(sp.getServer(), dim);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Calibration cancelled."), false);
                                    return 1;
                                })
                        )
                        .then(
                            Commands.literal("status")
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    ResourceLocation dim = sp.level().dimension().location();

                                    String status = com.bobby.valorant.spawn.SkySmokeCalibration.getCalibrationStatus(sp.getServer(), dim);
                                    ctx.getSource().sendSuccess(() -> Component.literal(status), false);
                                    return 1;
                                })
                        )
                );

        // -------- charges helpers (fireball/curveball/blaze) --------
        var fireballCharges =
            Commands.literal("fireball")
                .then(
                    Commands.literal("charges").requires(s -> s.hasPermission(2))
                        .then(
                            Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    return setFireballCharges(ctx.getSource(), List.of(p), amount);
                                })
                                .then(
                                    Commands.argument("players", EntityArgument.players())
                                        .executes(ctx -> {
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            return setFireballCharges(ctx.getSource(), EntityArgument.getPlayers(ctx, "players"), amount);
                                        })
                                )
                        )
                );

        var curveballCharges =
            Commands.literal("curveball")
                .then(
                    Commands.literal("charges").requires(s -> s.hasPermission(2))
                        .then(
                            Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    return setCharges(ctx.getSource(), List.of(p), amount);
                                })
                                .then(
                                    Commands.argument("players", EntityArgument.players())
                                        .executes(ctx -> {
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            return setCharges(ctx.getSource(), EntityArgument.getPlayers(ctx, "players"), amount);
                                        })
                                )
                        )
                );

        var blazeCharges =
            Commands.literal("blaze")
                .then(
                    Commands.literal("charges").requires(s -> s.hasPermission(2))
                        .then(
                            Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    return setBlazeCharges(ctx.getSource(), List.of(p), amount);
                                })
                                .then(
                                    Commands.argument("players", EntityArgument.players())
                                        .executes(ctx -> {
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            return setBlazeCharges(ctx.getSource(), EntityArgument.getPlayers(ctx, "players"), amount);
                                        })
                                )
                        )
                );

        var skySmokeCharges =
            Commands.literal("skysmoke")
                .then(
                    Commands.literal("charges").requires(s -> s.hasPermission(2))
                        .then(
                            Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    return setSkySmokeCharges(ctx.getSource(), List.of(p), amount);
                                })
                                .then(
                                    Commands.argument("players", EntityArgument.players())
                                        .executes(ctx -> {
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            return setSkySmokeCharges(ctx.getSource(), EntityArgument.getPlayers(ctx, "players"), amount);
                                        })
                                )
                        )
                );

        // -------- /valorant credits --------
        var creditsSet =
            Commands.literal("set")
                .then(
                    Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            int amt = IntegerArgumentType.getInteger(ctx, "amount");
                            com.bobby.valorant.economy.EconomyData.setCredits(sp, amt);
                            com.bobby.valorant.economy.EconomyData.syncCredits(sp);
                            ctx.getSource().sendSuccess(() -> Component.literal("Credits set to " + amt + " for " + sp.getGameProfile().getName()), false);
                            return 1;
                        })
                        .then(
                            Commands.argument("players", EntityArgument.players())
                                .executes(ctx -> {
                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
                                    int amt = IntegerArgumentType.getInteger(ctx, "amount");
                                    for (ServerPlayer sp : players) {
                                        com.bobby.valorant.economy.EconomyData.setCredits(sp, amt);
                                        com.bobby.valorant.economy.EconomyData.syncCredits(sp);
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.literal("Credits set to " + amt + " for " + players.size() + " player(s)"), false);
                                    return players.size();
                                })
                        )
                );

        var creditsAdd =
            Commands.literal("add")
                .then(
                    Commands.argument("amount", IntegerArgumentType.integer(Integer.MIN_VALUE))
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            int delta = IntegerArgumentType.getInteger(ctx, "amount");
                            int newVal = Math.max(0, com.bobby.valorant.economy.EconomyData.getCredits(sp) + delta);
                            com.bobby.valorant.economy.EconomyData.setCredits(sp, newVal);
                            com.bobby.valorant.economy.EconomyData.syncCredits(sp);
                            ctx.getSource().sendSuccess(() -> Component.literal("Credits changed by " + delta + " -> " + newVal + " for " + sp.getGameProfile().getName()), false);
                            return 1;
                        })
                        .then(
                            Commands.argument("players", EntityArgument.players())
                                .executes(ctx -> {
                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
                                    int delta = IntegerArgumentType.getInteger(ctx, "amount");
                                    for (ServerPlayer sp : players) {
                                        int newVal = Math.max(0, com.bobby.valorant.economy.EconomyData.getCredits(sp) + delta);
                                        com.bobby.valorant.economy.EconomyData.setCredits(sp, newVal);
                                        com.bobby.valorant.economy.EconomyData.syncCredits(sp);
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.literal("Credits changed by " + delta + " for " + players.size() + " player(s)"), false);
                                    return players.size();
                                })
                        )
                );

        var credits =
            Commands.literal("credits").requires(s -> s.hasPermission(2))
                .then(creditsSet)
                .then(creditsAdd);

        // -------- /valorant team --------
        var teamJoin =
            Commands.literal("join")
                .then(
                    Commands.argument("side", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            String side = StringArgumentType.getString(ctx, "side");
                            boolean ok = TeamManager.joinTeam(sp, side);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Joined team " + (side.equalsIgnoreCase("A") ? "A" : "V")), false);
                            else ctx.getSource().sendFailure(Component.literal("Team is full"));
                            return ok ? 1 : 0;
                        })
                );

        var teamSwitch =
            Commands.literal("switch")
                .executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    boolean ok = TeamManager.switchTeam(sp);
                    if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Switched team"), false);
                    else ctx.getSource().sendFailure(Component.literal("Target team is full"));
                    return ok ? 1 : 0;
                });

        var team =
            Commands.literal("team")
                .then(teamJoin)
                .then(teamSwitch);

        // -------- /valorant round --------
        var roundStart =
            Commands.literal("start")
                .then(
                    Commands.argument("buySeconds", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            int secs = IntegerArgumentType.getInteger(ctx, "buySeconds");
                            RoundController.get(level).startRound(secs);
                            ctx.getSource().sendSuccess(() -> Component.literal("Round started (BUY=" + secs + ")"), true);
                            return 1;
                        })
                );

        var roundStop =
            Commands.literal("stop")
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    RoundController.get(level).stopRound();
                    ctx.getSource().sendSuccess(() -> Component.literal("Round stopped"), true);
                    return 1;
                });

        var roundScores =
            Commands.literal("scores")
                .then(
                    Commands.argument("left", IntegerArgumentType.integer(0))
                        .then(
                            Commands.argument("right", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    ServerLevel level = ctx.getSource().getLevel();
                                    int left = IntegerArgumentType.getInteger(ctx, "left");
                                    int right = IntegerArgumentType.getInteger(ctx, "right");
                                    RoundController.get(level).setScores(left, right);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Scores set L:" + left + " R:" + right), true);
                                    return 1;
                                })
                        )
                );

        var roundPlant =
            Commands.literal("plant")
                .executes(ctx -> {
                    var sender = ctx.getSource().getEntity();
                    if (sender == null) {
                        ctx.getSource().sendFailure(Component.literal("Command must be run by a player"));
                        return 0;
                    }
                    RoundController.get(ctx.getSource().getLevel()).plantSpike(sender.position());
                    ctx.getSource().sendSuccess(() -> Component.literal("Spike planted"), true);
                    return 1;
                });

        var roundTitleTest =
            Commands.literal("title")
                .then(
                    Commands.literal("test")
                        .then(
                            Commands.argument("title", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String titleText = StringArgumentType.getString(ctx, "title");
                                    TitleMessages.show(titleText, "Test subtitle", 20, 60, 20, 0xFFFF0000, 0xFFFFFF00);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Title overlay sent: " + titleText), true);
                                    return 1;
                                })
                        )
                );

        var roundDefuse =
            Commands.literal("defuse")
                .executes(ctx -> {
                    RoundController.get(ctx.getSource().getLevel()).defuseSpikeFull();
                    ctx.getSource().sendSuccess(() -> Component.literal("Spike defused"), true);
                    return 1;
                });

        var roundOtBudget =
            Commands.literal("otbudget")
                .then(
                    Commands.argument("credits", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            int creditsVal = IntegerArgumentType.getInteger(ctx, "credits");
                            RoundController.get(level).setOvertimeBudget(creditsVal);
                            ctx.getSource().sendSuccess(() -> Component.literal("OT budget set to " + creditsVal), true);
                            return 1;
                        })
                );

        var roundBuySellBuy =
            Commands.literal("buy")
                .then(
                    Commands.argument("item", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(ctx, "item").toUpperCase();
                            com.bobby.valorant.economy.ShopItem item;
                            try {
                                item = com.bobby.valorant.economy.ShopItem.valueOf(id);
                            } catch (IllegalArgumentException ex) {
                                ctx.getSource().sendFailure(Component.literal("Unknown item"));
                                return 0;
                            }
                            RoundController rc = RoundController.get(ctx.getSource().getLevel());
                            boolean inPhase = rc.phase() == RoundController.Phase.BUY;
                            boolean inSpawn = rc.isInSpawn(sp);
                            if (!inPhase) {
                                ctx.getSource().sendFailure(Component.literal("Buy failed: not in Buy Phase"));
                                return 0;
                            }
                            if (!inSpawn) {
                                ctx.getSource().sendFailure(Component.literal("Buy failed: left spawn area"));
                                return 0;
                            }
                            int creditsVal = com.bobby.valorant.economy.EconomyData.getCredits(sp);
                            if (creditsVal < item.price) {
                                ctx.getSource().sendFailure(Component.literal("Buy failed: insufficient credits"));
                                return 0;
                            }
                            if (item.slot == com.bobby.valorant.economy.ShopItem.Slot.SECONDARY) {
                                boolean hasNonDefault = hasSecondaryNonDefault(sp);
                                if (hasNonDefault) {
                                    ctx.getSource().sendFailure(Component.literal("Buy failed: already have a sidearm"));
                                    return 0;
                                }
                            }
                            boolean ok = com.bobby.valorant.economy.EconomyData.tryBuy(sp, item, rc.getCurrentRoundId(), true);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Bought " + id), false);
                            else ctx.getSource().sendFailure(Component.literal("Buy failed"));
                            return ok ? 1 : 0;
                        })
                );

        var roundBuySellSell =
            Commands.literal("sell")
                .then(
                    Commands.argument("item", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(ctx, "item").toUpperCase();
                            com.bobby.valorant.economy.ShopItem item;
                            try {
                                item = com.bobby.valorant.economy.ShopItem.valueOf(id);
                            } catch (IllegalArgumentException ex) {
                                ctx.getSource().sendFailure(Component.literal("Unknown item"));
                                return 0;
                            }
                            RoundController rc = RoundController.get(ctx.getSource().getLevel());
                            boolean inPhase = rc.phase() == RoundController.Phase.BUY;
                            if (!inPhase) {
                                ctx.getSource().sendFailure(Component.literal("Sell failed: not in Buy Phase"));
                                return 0;
                            }
                            boolean ok = com.bobby.valorant.economy.EconomyData.trySell(sp, item, rc.getCurrentRoundId(), true);
                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Sold " + id), false);
                            else ctx.getSource().sendFailure(Component.literal("Sell failed"));
                            return ok ? 1 : 0;
                        })
                );

        var round =
            Commands.literal("round").requires(s -> s.hasPermission(2))
                .then(roundStart)
                .then(roundStop)
                .then(roundScores)
                .then(roundPlant)
                .then(roundTitleTest)
                .then(roundDefuse)
                .then(roundOtBudget)
                .then(roundBuySellBuy)
                .then(roundBuySellSell);

        // -------- /valorant wall --------
        var wallTest =
            Commands.literal("wall")
                .then(
                    Commands.literal("test")
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            ServerLevel level = (ServerLevel) sp.level();

                            var stand = new net.minecraft.world.entity.decoration.ArmorStand(level, sp.getX(), sp.getY(), sp.getZ());
                            stand.setInvisible(true);
                            stand.setInvulnerable(true);
                            stand.setNoGravity(true);
                            stand.setSilent(true);
                            stand.setShowArms(false);
                            stand.setNoBasePlate(true);
                            stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, com.bobby.valorant.registry.ModItems.WALLSEGMENT.get().getDefaultInstance());
                            level.addFreshEntity(stand);

                            ctx.getSource().sendSuccess(() -> Component.literal("Wall segment test armor stand spawned"), false);
                            return 1;
                        })
                );

        // -------- /valorant killfeed --------
        var killfeedTest =
            Commands.literal("killfeed")
                .requires(s -> s.hasPermission(2))
                .then(
                    Commands.literal("test")
                        .then(
                            Commands.argument("victim", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer killer = ctx.getSource().getPlayerOrException();
                                    ServerPlayer victim = EntityArgument.getPlayer(ctx, "victim");
                                    var stack = killer.getMainHandItem();
                                    var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                                    String weaponId = key != null ? key.toString() : "valorant:vandal";

                                    var pkt = new com.bobby.valorant.network.KillfeedMessageS2CPacket(
                                        killer.getScoreboardName(), victim.getScoreboardName(), weaponId, Agent.UNSELECTED.getId(), Agent.UNSELECTED.getId());
                                    var server = ctx.getSource().getServer();
                                    if (server != null) {
                                        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                                            PacketDistributor.sendToPlayer(p, pkt);
                                        }
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.literal("Killfeed test sent: " + killer.getScoreboardName() + " -> " + victim.getScoreboardName() + " (" + weaponId + ")"), false);
                                    return 1;
                                })
                                .then(
                                    Commands.argument("weapon", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer killer = ctx.getSource().getPlayerOrException();
                                            ServerPlayer victim = EntityArgument.getPlayer(ctx, "victim");
                                            String w = StringArgumentType.getString(ctx, "weapon");
                                            String weaponId = w.contains(":") ? w : ("valorant:" + w);

                                            var pkt = new com.bobby.valorant.network.KillfeedMessageS2CPacket(
                                                killer.getScoreboardName(), victim.getScoreboardName(), weaponId, Agent.UNSELECTED.getId(), Agent.UNSELECTED.getId());
                                            var server = ctx.getSource().getServer();
                                            if (server != null) {
                                                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                                                    PacketDistributor.sendToPlayer(p, pkt);
                                                }
                                            }
                                            ctx.getSource().sendSuccess(() -> Component.literal("Killfeed test sent: " + killer.getScoreboardName() + " -> " + victim.getScoreboardName() + " (" + weaponId + ")"), false);
                                            return 1;
                                        })
                                )
                        )
                );

        // -------- /valorant test --------
        var testCommand =
                Commands.literal("test").requires(s -> s.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String playerName = player.getGameProfile().getName();
                            UUID playerUUID = player.getUUID();

                            Path assetsDir = Paths.get("run", "config", "fancymenu", "assets");
                            Path htmlFile = assetsDir.resolve("player_info.html");

                            try {
                                // Create directories if they don't exist
                                Files.createDirectories(assetsDir);

                                // Delete file if it exists
                                if (Files.exists(htmlFile)) {
                                    Files.delete(htmlFile);
                                }

                                // Create and write to the new file
                                try (BufferedWriter writer = Files.newBufferedWriter(htmlFile)) {
                                    writer.write("<!DOCTYPE html>\n");
                                    writer.write("<html>\n");
                                    writer.write("<head>\n");
                                    writer.write("<title>Player Info</title>\n");
                                    writer.write("</head>\n");
                                    writer.write("<body>\n");
                                    writer.write("<h1>" + playerName + "</h1>\n");
                                    writer.write("<img src=\"https://crafatar.com/avatars/" + playerUUID.toString() + "?size=128&overlay\" alt=\"Player Head\">\n");
                                    writer.write("</body>\n");
                                    writer.write("</html>\n");
                                }

                                ctx.getSource().sendSuccess(() -> Component.literal("Generated player_info.html for " + playerName), false);
                                return 1;
                            } catch (IOException e) {
                                com.bobby.valorant.Valorant.LOGGER.error("Failed to generate player html file", e);
                                ctx.getSource().sendFailure(Component.literal("Failed to generate HTML file: " + e.getMessage()));
                                return 0;
                            }
                        }            );

        // -------- Sky Smoke area/block commands --------
        var skySmokeAreaCommands =
            Commands.literal("area").requires(s -> s.hasPermission(2))
            .then(
                Commands.literal("record")
                    .then(
                        Commands.literal("start").requires(s -> s.hasPermission(2))
                            .then(
                                Commands.argument("id", StringArgumentType.word())
                                    .then(
                                        Commands.literal("ground")
                                            .executes(ctx -> {
                                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                String id = StringArgumentType.getString(ctx, "id");
                                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.start(sp, id,
                                                    com.bobby.valorant.skysmoke.SkySmokeArea.Type.ALLOWED,
                                                    com.bobby.valorant.skysmoke.SkySmokeArea.Mode.GROUND, null);
                                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Started recording allowed area '" + id + "' (ground mode)"), false);
                                                else ctx.getSource().sendFailure(Component.literal("Failed to start recording"));
                                                return ok ? 1 : 0;
                                            })
                                            .then(
                                                Commands.argument("y", IntegerArgumentType.integer())
                                                    .executes(ctx -> {
                                                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                        String id = StringArgumentType.getString(ctx, "id");
                                                        int y = IntegerArgumentType.getInteger(ctx, "y");
                                                        boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.start(sp, id,
                                                            com.bobby.valorant.skysmoke.SkySmokeArea.Type.ALLOWED,
                                                            com.bobby.valorant.skysmoke.SkySmokeArea.Mode.GROUND, y);
                                                        if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Started recording allowed area '" + id + "' (ground mode) at y=" + y), false);
                                                        else ctx.getSource().sendFailure(Component.literal("Failed to start recording"));
                                                        return ok ? 1 : 0;
                                                    })
                                            )
                                    )
                                    .then(
                                        Commands.literal("fixed")
                                            .then(
                                                Commands.argument("y", IntegerArgumentType.integer())
                                                    .executes(ctx -> {
                                                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                        String id = StringArgumentType.getString(ctx, "id");
                                                        int y = IntegerArgumentType.getInteger(ctx, "y");
                                                        boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.start(sp, id,
                                                            com.bobby.valorant.skysmoke.SkySmokeArea.Type.ALLOWED,
                                                            com.bobby.valorant.skysmoke.SkySmokeArea.Mode.FIXED_Y, y);
                                                        if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Started recording allowed area '" + id + "' (fixed Y=" + y + ")"), false);
                                                        else ctx.getSource().sendFailure(Component.literal("Failed to start recording"));
                                                        return ok ? 1 : 0;
                                                    })
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("add")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.addPoint(sp);
                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Added point at your location"), false);
                                else ctx.getSource().sendFailure(Component.literal("No active recording; use /valorant area record start"));
                                return ok ? 1 : 0;
                            })
                    )
                    .then(
                        Commands.literal("undo")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.undo(sp);
                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Removed last point"), false);
                                else ctx.getSource().sendFailure(Component.literal("Nothing to undo"));
                                return ok ? 1 : 0;
                            })
                    )
                    .then(
                        Commands.literal("clear")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.clear(sp);
                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Cleared all points"), false);
                                else ctx.getSource().sendFailure(Component.literal("No active recording"));
                                return ok ? 1 : 0;
                            })
                    )
                    .then(
                        Commands.literal("setY")
                            .then(
                                Commands.argument("y", IntegerArgumentType.integer())
                                    .executes(ctx -> {
                                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                        int y = IntegerArgumentType.getInteger(ctx, "y");
                                        boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.setY(sp, y);
                                        if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Set area Y to " + y), false);
                                        else ctx.getSource().sendFailure(Component.literal("No active recording"));
                                        return ok ? 1 : 0;
                                    })
                            )
                    )
                    .then(
                        Commands.literal("save")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.save(sp);
                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Saved allowed area"), false);
                                else ctx.getSource().sendFailure(Component.literal("Failed to save (need 3+ points)"));
                                return ok ? 1 : 0;
                            })
                    )
                    .then(
                        Commands.literal("cancel")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.cancel(sp);
                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Cancelled recording"), false);
                                else ctx.getSource().sendFailure(Component.literal("No active recording"));
                                return ok ? 1 : 0;
                            })
                    )
                    .then(
                        Commands.literal("status")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                String status = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.status(sp);
                                ctx.getSource().sendSuccess(() -> Component.literal(status), false);
                                return 1;
                            })
                    )
            )
            .then(
                Commands.literal("toggle")
                    .then(
                        Commands.argument("id", StringArgumentType.word())
                            .then(
                                Commands.argument("enabled", StringArgumentType.word())
                                    .executes(ctx -> {
                                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                        String id = StringArgumentType.getString(ctx, "id");
                                        String enabledStr = StringArgumentType.getString(ctx, "enabled").toLowerCase();
                                        boolean enabled = enabledStr.equals("true") || enabledStr.equals("enable") || enabledStr.equals("on");

                                        com.bobby.valorant.skysmoke.SkySmokeManager.toggleArea(sp.getServer(), sp.level().dimension().location(), id, enabled);
                                        ctx.getSource().sendSuccess(() -> Component.literal("Toggled allowed area '" + id + "' " + (enabled ? "enabled" : "disabled")), false);
                                        return 1;
                                    })
                            )
                    )
            )
            .then(
                Commands.literal("list")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        var areas = com.bobby.valorant.skysmoke.SkySmokeManager.get((ServerLevel) sp.level());
                        if (areas == null || areas.allowed().isEmpty()) {
                            ctx.getSource().sendSuccess(() -> Component.literal("No allowed areas defined"), false);
                            return 1;
                        }

                        StringBuilder sb = new StringBuilder("Allowed areas:\n");
                        for (var entry : areas.allowed().entrySet()) {
                            var area = entry.getValue();
                            sb.append("  ").append(entry.getKey())
                              .append(" (").append(area.mode().name().toLowerCase())
                              .append(area.mode() == com.bobby.valorant.skysmoke.SkySmokeArea.Mode.FIXED_Y ? ",y=" + area.y() : "")
                              .append(", ").append(area.vertices().size()).append(" verts")
                              .append(area.enabled() ? ", enabled" : ", disabled")
                              .append(")\n");
                        }
                        ctx.getSource().sendSuccess(() -> Component.literal(sb.toString().trim()), false);
                        return 1;
                    })
            )
            .then(
                Commands.literal("reload")
                    .executes(ctx -> {
                        var server = ctx.getSource().getServer();
                        if (server == null) return 0;
                        com.bobby.valorant.skysmoke.SkySmokeManager.load(server);
                        ctx.getSource().sendSuccess(() -> Component.literal("Reloaded Sky Smoke areas"), true);
                        return 1;
                    })
            );

        var skySmokeBlockCommands =
            Commands.literal("block").requires(s -> s.hasPermission(2))
            .then(
                Commands.literal("record")
                    .then(
                        Commands.literal("start").requires(s -> s.hasPermission(2))
                            .then(
                                Commands.argument("id", StringArgumentType.word())
                                    .then(
                                        Commands.literal("ground")
                                            .executes(ctx -> {
                                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                String id = StringArgumentType.getString(ctx, "id");
                                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.start(sp, id,
                                                    com.bobby.valorant.skysmoke.SkySmokeArea.Type.BLOCKED,
                                                    com.bobby.valorant.skysmoke.SkySmokeArea.Mode.GROUND, null);
                                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Started recording blocked area '" + id + "' (ground mode)"), false);
                                                else ctx.getSource().sendFailure(Component.literal("Failed to start recording"));
                                                return ok ? 1 : 0;
                                            })
                                    )
                            )
                    )
                    .then(
                        Commands.literal("add")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.addPoint(sp);
                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Added point at your location"), false);
                                else ctx.getSource().sendFailure(Component.literal("No active recording; use /valorant block record start"));
                                return ok ? 1 : 0;
                            })
                    )
                    .then(
                        Commands.literal("undo")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.undo(sp);
                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Removed last point"), false);
                                else ctx.getSource().sendFailure(Component.literal("Nothing to undo"));
                                return ok ? 1 : 0;
                            })
                    )
                    .then(
                        Commands.literal("clear")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.clear(sp);
                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Cleared all points"), false);
                                else ctx.getSource().sendFailure(Component.literal("No active recording"));
                                return ok ? 1 : 0;
                            })
                    )
                    .then(
                        Commands.literal("save")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.save(sp);
                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Saved blocked area"), false);
                                else ctx.getSource().sendFailure(Component.literal("Failed to save (need 3+ points)"));
                                return ok ? 1 : 0;
                            })
                    )
                    .then(
                        Commands.literal("cancel")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                boolean ok = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.cancel(sp);
                                if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Cancelled recording"), false);
                                else ctx.getSource().sendFailure(Component.literal("No active recording"));
                                return ok ? 1 : 0;
                            })
                    )
                    .then(
                        Commands.literal("status")
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                String status = com.bobby.valorant.skysmoke.SkySmokeAreaRecording.status(sp);
                                ctx.getSource().sendSuccess(() -> Component.literal(status), false);
                                return 1;
                            })
                    )
            )
            .then(
                Commands.literal("toggle")
                    .then(
                        Commands.argument("id", StringArgumentType.word())
                            .then(
                                Commands.argument("enabled", StringArgumentType.word())
                                    .executes(ctx -> {
                                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                        String id = StringArgumentType.getString(ctx, "id");
                                        String enabledStr = StringArgumentType.getString(ctx, "enabled").toLowerCase();
                                        boolean enabled = enabledStr.equals("true") || enabledStr.equals("enable") || enabledStr.equals("on");

                                        com.bobby.valorant.skysmoke.SkySmokeManager.toggleArea(sp.getServer(), sp.level().dimension().location(), id, enabled);
                                        ctx.getSource().sendSuccess(() -> Component.literal("Toggled blocked area '" + id + "' " + (enabled ? "enabled" : "disabled")), false);
                                        return 1;
                                    })
                            )
                    )
            )
            .then(
                Commands.literal("list")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        var areas = com.bobby.valorant.skysmoke.SkySmokeManager.get((ServerLevel) sp.level());
                        if (areas == null || areas.blocked().isEmpty()) {
                            ctx.getSource().sendSuccess(() -> Component.literal("No blocked areas defined"), false);
                            return 1;
                        }

                        StringBuilder sb = new StringBuilder("Blocked areas:\n");
                        for (var entry : areas.blocked().entrySet()) {
                            var area = entry.getValue();
                            sb.append("  ").append(entry.getKey())
                              .append(" (").append(area.mode().name().toLowerCase())
                              .append(", ").append(area.vertices().size()).append(" verts")
                              .append(area.enabled() ? ", enabled" : ", disabled")
                              .append(")\n");
                        }
                        ctx.getSource().sendSuccess(() -> Component.literal(sb.toString().trim()), false);
                        return 1;
                    })
            );

        // -------- root /valorant --------
        dispatcher.register(
            Commands.literal("valorant")
                .then(spawnarea)
                .then(bombsite)
                .then(agent)
                .then(particle)
                .then(fireballCharges)
                .then(curveballCharges)
                .then(blazeCharges)
                .then(skySmokeCharges)
                .then(credits)
                .then(team)
                .then(round)
                .then(wallTest)
                .then(killfeedTest)
                .then(testCommand)
                .then(skySmokeCalibration)
                .then(skySmokeAreaCommands)
                .then(skySmokeBlockCommands)
        );
    }

    // -------- Utility methods --------
    private static void spawnScheduledParticles(ServerPlayer sp,
                                               ParticleOptions po,
                                               double dx, double dy, double dz,
                                               double speed,
                                               int count,
                                               int duration) {
        ServerLevel level = (ServerLevel) sp.level();
        double x = sp.getX();
        double y = sp.getY();
        double z = sp.getZ();
        ParticleScheduler.spawnRepeating(level, po, x, y, z, count, dx, dy, dz, speed, duration);
    }

    private static boolean hasSecondaryNonDefault(ServerPlayer sp) {
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            var s = sp.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(net.minecraft.world.item.Items.STONE_SWORD)) continue;
            if (s.is(net.minecraft.world.item.Items.IRON_SWORD) || s.is(com.bobby.valorant.registry.ModItems.GHOST.get())) return true;
        }
        return false;
    }

    private static int setCharges(CommandSourceStack source, Collection<ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            CurveballData.setCharges(player, amount);
            setChargesForSlot(player, Ability.Slot.E, amount);
        }
        Component message = Component.translatable("commands.valorant.curveball.charges.set", amount, players.size());
        source.sendSuccess(() -> message, true);
        return players.size();
    }

    private static int setFireballCharges(CommandSourceStack source, Collection<ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            FireballData.setCharges(player, amount);
            setChargesForSlot(player, Ability.Slot.Q, amount);
        }
        Component message = Component.translatable("commands.valorant.fireball.charges.set", amount, players.size());
        source.sendSuccess(() -> message, true);
        return players.size();
    }

    private static int setBlazeCharges(CommandSourceStack source, Collection<ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            com.bobby.valorant.player.FireWallData.setCharges(player, amount);
            setChargesForSlot(player, Ability.Slot.C, amount);
        }
        Component message = Component.translatable("commands.valorant.wall.charges.set", amount, players.size());
        source.sendSuccess(() -> message, true);
        return players.size();
    }

    private static int setSkySmokeCharges(CommandSourceStack source, Collection<ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            setChargesForSlot(player, Ability.Slot.E, amount);
        }
        Component message = Component.translatable("commands.valorant.skysmoke.charges.set", amount, players.size());
        source.sendSuccess(() -> message, true);
        return players.size();
    }

    private static void setChargesForSlot(ServerPlayer player, Ability.Slot slot, int amount) {
        var agent = AgentData.getSelectedAgent(player);
        var set = Abilities.getForAgent(agent);
        Ability ability = switch (slot) {
            case C -> set.c();
            case Q -> set.q();
            case E -> set.e();
            case X -> set.x();
        };
        if (ability == null) return;
        AbilityStateData.setCharges(player, ability, amount);
        syncAbilityState(player);
    }

    private static void syncAbilityState(ServerPlayer player) {
        var agent = AgentData.getSelectedAgent(player);
        var set = Abilities.getForAgent(agent);
        int c = set.c() != null ? AbilityStateData.getCharges(player, set.c()) : 0;
        int q = set.q() != null ? AbilityStateData.getCharges(player, set.q()) : 0;
        int e = set.e() != null ? AbilityStateData.getCharges(player, set.e()) : 0;
        int x = AbilityStateData.getUltPoints(player);
        PacketDistributor.sendToPlayer(player, new SyncAbilityStateS2CPacket(c, q, e, x));
    }

    private static int unlockAgents(CommandSourceStack source, Collection<ServerPlayer> players) {
        int count = 0;
        for (ServerPlayer target : players) {
            var server = target.getServer();
            if (server == null) continue;
            AgentLockManager manager = AgentLockManager.get(server);
            manager.unlock(target);
            AgentData.setSelectedAgent(target, Agent.UNSELECTED);

            Map<UUID, String> playerAgentMap = new HashMap<>();
            manager.getPlayerToLocked().forEach((uuid, a) -> playerAgentMap.put(uuid, a.getId()));
            SyncAgentLocksPacket locks = new SyncAgentLocksPacket(playerAgentMap);
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                PacketDistributor.sendToPlayer(p, locks);
                PacketDistributor.sendToPlayer(p, new SyncAgentS2CPacket(target.getUUID(), Agent.UNSELECTED.getId()));
            }
            count++;
        }
        int result = count;
        source.sendSuccess(() -> Component.literal("Unlocked agent for " + result + " player(s)"), false);
        return result;
    }

    private static int lockAgents(CommandSourceStack source, Collection<ServerPlayer> players, String agentId) {
        Agent agent = Agent.byId(agentId);
        if (agent == Agent.UNSELECTED) {
            source.sendFailure(Component.literal("Unknown agent: " + agentId));
            return 0;
        }

        int count = 0;
        for (ServerPlayer target : players) {
            var server = target.getServer();
            if (server == null) continue;
            AgentLockManager manager = AgentLockManager.get(server);
            boolean ok = manager.tryLock(target, agent);
            if (ok) {
                AgentData.setSelectedAgent(target, agent);

                Map<UUID, String> playerAgentMap = new HashMap<>();
                manager.getPlayerToLocked().forEach((uuid, a) -> playerAgentMap.put(uuid, a.getId()));
                SyncAgentLocksPacket locks = new SyncAgentLocksPacket(playerAgentMap);
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    PacketDistributor.sendToPlayer(p, locks);
                    PacketDistributor.sendToPlayer(p, new SyncAgentS2CPacket(target.getUUID(), agent.getId()));
                }
                count++;
            } else {
                target.displayClientMessage(Component.literal("Agent already locked by your team"), true);
            }
        }
        int result = count;
        if (result > 0) {
            source.sendSuccess(() -> Component.literal("Locked agent " + agent.getDisplayName().getString() + " for " + result + " player(s)"), false);
        }
        return result;
    }
}
