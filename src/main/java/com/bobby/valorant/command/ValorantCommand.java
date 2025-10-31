package com.bobby.valorant.command;

import com.bobby.valorant.player.CurveballData;
import com.bobby.valorant.player.FireballData;
import com.bobby.valorant.round.RoundController;
import com.bobby.valorant.round.TeamManager;
import com.bobby.valorant.util.ParticleScheduler;
import com.bobby.valorant.Config;
import com.bobby.valorant.server.TitleMessages;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.commands.CommandBuildContext;

import java.util.Collection;
import java.util.List;

public final class ValorantCommand {
    private ValorantCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                Commands.literal("valorant")
                        .then(Commands.literal("particle")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("particle", ParticleArgument.particle(buildContext))
                                        .executes(ctx -> {
                                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                            ParticleOptions po = ParticleArgument.getParticle(ctx, "particle");
                                            int duration = Config.COMMON.particleCommandDefaultDurationTicks.get();
                                            spawnScheduledParticles(sp, po, 0, 0, 0, 0.0D, 100, duration);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Scheduled particles for " + duration + " ticks"), false);
                                            return 1;
                                        })
                                        .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                    ParticleOptions po = ParticleArgument.getParticle(ctx, "particle");
                                                    int duration = IntegerArgumentType.getInteger(ctx, "duration");
                                                    spawnScheduledParticles(sp, po, 0, 0, 0, 0.0D, 100, duration);
                                                    ctx.getSource().sendSuccess(() -> Component.literal("Scheduled particles for " + duration + " ticks"), false);
                                                    return 1;
                                                })
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
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
                                                                                        }))))))))
                        )
                        .then(Commands.literal("fireball")
                                .then(Commands.literal("charges")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                                    return setFireballCharges(context.getSource(), List.of(player), amount);
                                                })
                                                .then(Commands.argument("players", EntityArgument.players())
                                                        .executes(context -> {
                                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
                                                            return setFireballCharges(context.getSource(), players, amount);
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("curveball")
                                .then(Commands.literal("charges")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                                    return setCharges(context.getSource(), List.of(player), amount);
                                                })
                                                .then(Commands.argument("players", EntityArgument.players())
                                                        .executes(context -> {
                                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
                                                            return setCharges(context.getSource(), players, amount);
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("credits").requires(s -> s.hasPermission(2))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                    int amt = IntegerArgumentType.getInteger(ctx, "amount");
                                                    com.bobby.valorant.economy.EconomyData.setCredits(sp, amt);
                                                    com.bobby.valorant.economy.EconomyData.syncCredits(sp);
                                                    ctx.getSource().sendSuccess(() -> Component.literal("Credits set to " + amt + " for " + sp.getGameProfile().getName()), false);
                                                    return 1;
                                                })
                                                .then(Commands.argument("players", EntityArgument.players())
                                                        .executes(ctx -> {
                                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
                                                            int amt = IntegerArgumentType.getInteger(ctx, "amount");
                                                            for (ServerPlayer sp : players) {
                                                                com.bobby.valorant.economy.EconomyData.setCredits(sp, amt);
                                                                com.bobby.valorant.economy.EconomyData.syncCredits(sp);
                                                            }
                                                            ctx.getSource().sendSuccess(() -> Component.literal("Credits set to " + amt + " for " + players.size() + " player(s)"), false);
                                                            return players.size();
                                                        })))
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(Integer.MIN_VALUE))
                                                .executes(ctx -> {
                                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                    int delta = IntegerArgumentType.getInteger(ctx, "amount");
                                                    int newVal = Math.max(0, com.bobby.valorant.economy.EconomyData.getCredits(sp) + delta);
                                                    com.bobby.valorant.economy.EconomyData.setCredits(sp, newVal);
                                                    com.bobby.valorant.economy.EconomyData.syncCredits(sp);
                                                    ctx.getSource().sendSuccess(() -> Component.literal("Credits changed by " + delta + " -> " + newVal + " for " + sp.getGameProfile().getName()), false);
                                                    return 1;
                                                })
                                                .then(Commands.argument("players", EntityArgument.players())
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
                                                        })))
                                )
                        )
                        .then(Commands.literal("team")
                                .then(Commands.literal("join")
                                        .then(Commands.argument("side", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                    String side = StringArgumentType.getString(ctx, "side");
                                                    boolean ok = TeamManager.joinTeam(sp, side);
                                                    if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Joined team " + (side.equalsIgnoreCase("A") ? "A" : "V")), false);
                                                    else ctx.getSource().sendFailure(Component.literal("Team is full"));
                                                    return ok ? 1 : 0;
                                                })))
                                .then(Commands.literal("switch")
                                        .executes(ctx -> {
                                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                            boolean ok = TeamManager.switchTeam(sp);
                                            if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Switched team"), false);
                                            else ctx.getSource().sendFailure(Component.literal("Target team is full"));
                                            return ok ? 1 : 0;
                                        }))
                        )
                        .then(Commands.literal("round").requires(s -> s.hasPermission(2))
                                .then(Commands.literal("start")
                                        .then(Commands.argument("buySeconds", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    ServerLevel level = ctx.getSource().getLevel();
                                                    int secs = IntegerArgumentType.getInteger(ctx, "buySeconds");
                                                    RoundController.get(level).startRound(secs);
                                                    ctx.getSource().sendSuccess(() -> Component.literal("Round started (BUY=" + secs + ")"), true);
                                                    return 1;
                                                })))
                                .then(Commands.literal("stop")
                                        .executes(ctx -> {
                                            ServerLevel level = ctx.getSource().getLevel();
                                            RoundController.get(level).stopRound();
                                            ctx.getSource().sendSuccess(() -> Component.literal("Round stopped"), true);
                                            return 1;
                                        }))
                                .then(Commands.literal("scores")
                                        .then(Commands.argument("left", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("right", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> {
                                                            ServerLevel level = ctx.getSource().getLevel();
                                                            int left = IntegerArgumentType.getInteger(ctx, "left");
                                                            int right = IntegerArgumentType.getInteger(ctx, "right");
                                                            RoundController.get(level).setScores(left, right);
                                                            ctx.getSource().sendSuccess(() -> Component.literal("Scores set L:" + left + " R:" + right), true);
                                                            return 1;
                                                        }))))
                                .then(Commands.literal("plant")
                                        .executes(ctx -> {
                                            var sender = ctx.getSource().getEntity();
                                            if (sender == null) {
                                                ctx.getSource().sendFailure(Component.literal("Command must be run by a player"));
                                                return 0;
                                            }
                                            RoundController.get(ctx.getSource().getLevel()).plantSpike(sender.position());
                                            ctx.getSource().sendSuccess(() -> Component.literal("Spike planted"), true);
                                            return 1;
                                        }))
                                .then(Commands.literal("title")
                                        .then(Commands.literal("test")
                                                .then(Commands.argument("title", StringArgumentType.greedyString())
                                                        .executes(ctx -> {
                                                            String titleText = StringArgumentType.getString(ctx, "title");
                                                            TitleMessages.show(titleText, "Test subtitle", 20, 60, 20, 0xFFFF0000, 0xFFFFFF00);
                                                            ctx.getSource().sendSuccess(() -> Component.literal("Title overlay sent: " + titleText), true);
                                                            return 1;
                                                        }))))
                                .then(Commands.literal("defuse")
                                        .executes(ctx -> {
                                            RoundController.get(ctx.getSource().getLevel()).defuseSpikeFull();
                                            ctx.getSource().sendSuccess(() -> Component.literal("Spike defused"), true);
                                            return 1;
                                        }))
                                .then(Commands.literal("otbudget")
                                        .then(Commands.argument("credits", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    ServerLevel level = ctx.getSource().getLevel();
                                                    int credits = IntegerArgumentType.getInteger(ctx, "credits");
                                                    RoundController.get(level).setOvertimeBudget(credits);
                                                    ctx.getSource().sendSuccess(() -> Component.literal("OT budget set to " + credits), true);
                                                    return 1;
                                                })))
                                .then(Commands.literal("buy")
                                        .then(Commands.argument("item", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                    String id = StringArgumentType.getString(ctx, "item").toUpperCase();
                                                    com.bobby.valorant.economy.ShopItem item;
                                                    try { item = com.bobby.valorant.economy.ShopItem.valueOf(id); } catch (IllegalArgumentException ex) { ctx.getSource().sendFailure(Component.literal("Unknown item")); return 0; }
                                                    RoundController rc = RoundController.get(ctx.getSource().getLevel());
                                                    boolean inPhase = rc.phase() == RoundController.Phase.BUY;
                                                    boolean inSpawn = rc.isInSpawn(sp);
                                                    if (!inPhase) { ctx.getSource().sendFailure(Component.literal("Buy failed: not in Buy Phase")); return 0; }
                                                    if (!inSpawn) { ctx.getSource().sendFailure(Component.literal("Buy failed: left spawn area")); return 0; }
                                                    int credits = com.bobby.valorant.economy.EconomyData.getCredits(sp);
                                                    if (credits < item.price) { ctx.getSource().sendFailure(Component.literal("Buy failed: insufficient credits")); return 0; }
                                                    // Slot rule quick check for secondary upgrades
                                                    if (item.slot == com.bobby.valorant.economy.ShopItem.Slot.SECONDARY) {
                                                        // block only if a non-default secondary exists
                                                        boolean hasNonDefault = hasSecondaryNonDefault(sp);
                                                        if (hasNonDefault) { ctx.getSource().sendFailure(Component.literal("Buy failed: already have a sidearm")); return 0; }
                                                    }
                                                    boolean ok = com.bobby.valorant.economy.EconomyData.tryBuy(sp, item, rc.getCurrentRoundId(), true);
                                                    if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Bought " + id), false); else ctx.getSource().sendFailure(Component.literal("Buy failed"));
                                                    return ok ? 1 : 0;
                                                })))
                                .then(Commands.literal("sell")
                                        .then(Commands.argument("item", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                                    String id = StringArgumentType.getString(ctx, "item").toUpperCase();
                                                    com.bobby.valorant.economy.ShopItem item;
                                                    try { item = com.bobby.valorant.economy.ShopItem.valueOf(id); } catch (IllegalArgumentException ex) { ctx.getSource().sendFailure(Component.literal("Unknown item")); return 0; }
                                                    RoundController rc = RoundController.get(ctx.getSource().getLevel());
                                                    boolean inPhase = rc.phase() == RoundController.Phase.BUY;
                                                    if (!inPhase) { ctx.getSource().sendFailure(Component.literal("Sell failed: not in Buy Phase")); return 0; }
                                                    boolean ok = com.bobby.valorant.economy.EconomyData.trySell(sp, item, rc.getCurrentRoundId(), true);
                                                    if (ok) ctx.getSource().sendSuccess(() -> Component.literal("Sold " + id), false); else ctx.getSource().sendFailure(Component.literal("Sell failed"));
                                                    return ok ? 1 : 0;
                                                })))
                        )
                        .then(Commands.literal("wall")
                                .then(Commands.literal("test")
                                        .executes(ctx -> {
                                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                            ServerLevel level = (ServerLevel) sp.level();

                                            // Create armor stand at player position
                                            net.minecraft.world.entity.decoration.ArmorStand stand = new net.minecraft.world.entity.decoration.ArmorStand(level, sp.getX(), sp.getY(), sp.getZ());
                                            stand.setInvisible(true);
                                            stand.setInvulnerable(true);
                                            stand.setNoGravity(true);
                                            stand.setSilent(true);
                                            // Keep it stationary and unobtrusive
                                            stand.setShowArms(false);
                                            stand.setNoBasePlate(true);
                                            // Display the wall segment item on its head
                                            stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, com.bobby.valorant.registry.ModItems.WALLSEGMENT.get().getDefaultInstance());
                                            level.addFreshEntity(stand);

                                            ctx.getSource().sendSuccess(() -> Component.literal("Wall segment test armor stand spawned"), false);
                                            return 1;
                                        })))
        );
    }

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
        }

        Component message = Component.translatable("commands.valorant.curveball.charges.set", amount, players.size());
        source.sendSuccess(() -> message, true);

        return players.size();
    }

    private static int setFireballCharges(CommandSourceStack source, Collection<ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            FireballData.setCharges(player, amount);
        }

        Component message = Component.translatable("commands.valorant.fireball.charges.set", amount, players.size());
        source.sendSuccess(() -> message, true);

        return players.size();
    }
}
