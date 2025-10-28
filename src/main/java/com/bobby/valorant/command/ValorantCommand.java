package com.bobby.valorant.command;

import com.bobby.valorant.player.CurveballData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
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
        );
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
