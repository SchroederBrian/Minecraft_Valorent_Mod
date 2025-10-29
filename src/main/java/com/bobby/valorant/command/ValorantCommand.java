package com.bobby.valorant.command;

import com.bobby.valorant.player.CurveballData;
import com.bobby.valorant.round.RoundController;
import com.bobby.valorant.round.TeamManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public final class ValorantCommand {
    private ValorantCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("valorant")
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
                                            RoundController.get(ctx.getSource().getLevel()).plantSpike();
                                            ctx.getSource().sendSuccess(() -> Component.literal("Spike planted"), true);
                                            return 1;
                                        }))
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
        );
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
}
