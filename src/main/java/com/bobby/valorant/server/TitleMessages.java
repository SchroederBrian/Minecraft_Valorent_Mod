package com.bobby.valorant.server;

import com.bobby.valorant.network.ShowTitleOverlayPacket;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-side helper for sending title overlay messages to clients.
 */
public final class TitleMessages {
    private TitleMessages() {}

    /**
     * Send a title overlay to a specific player.
     */
    public static void sendTo(ServerPlayer player,
                             String title, String subtitle,
                             int fadeInTicks, int stayTicks, int fadeOutTicks,
                             int titleColor, int subtitleColor) {
        PacketDistributor.sendToPlayer(player,
            new ShowTitleOverlayPacket(title, subtitle, fadeInTicks, stayTicks, fadeOutTicks, titleColor, subtitleColor));
    }

    /**
     * Broadcast a title overlay to all players in a level.
     */
    public static void broadcast(ServerLevel level,
                                String title, String subtitle,
                                int fadeInTicks, int stayTicks, int fadeOutTicks,
                                int titleColor, int subtitleColor) {
        for (ServerPlayer player : level.players()) {
            sendTo(player, title, subtitle, fadeInTicks, stayTicks, fadeOutTicks, titleColor, subtitleColor);
        }
    }

    /**
     * Send a title overlay using config defaults.
     */
    public static void sendTo(ServerPlayer player, String title, String subtitle) {
        sendTo(player, title, subtitle,
               com.bobby.valorant.Config.COMMON.titleFadeInTicks.get(),
               com.bobby.valorant.Config.COMMON.titleStayTicks.get(),
               com.bobby.valorant.Config.COMMON.titleFadeOutTicks.get(),
               com.bobby.valorant.Config.COMMON.titleColor.get(),
               com.bobby.valorant.Config.COMMON.subtitleColor.get());
    }

    /**
     * Broadcast a title overlay using config defaults.
     */
    public static void broadcast(ServerLevel level, String title, String subtitle) {
        broadcast(level, title, subtitle,
                  com.bobby.valorant.Config.COMMON.titleFadeInTicks.get(),
                  com.bobby.valorant.Config.COMMON.titleStayTicks.get(),
                  com.bobby.valorant.Config.COMMON.titleFadeOutTicks.get(),
                  com.bobby.valorant.Config.COMMON.titleColor.get(),
                  com.bobby.valorant.Config.COMMON.subtitleColor.get());
    }
}
