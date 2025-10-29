package com.bobby.valorant.round;

import com.bobby.valorant.economy.EconomyData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class EconomyManager {
    private EconomyManager() {}

    public static void onRoundWin(ServerLevel level, boolean attackersWin, boolean attackersOnLeft) {
        for (ServerPlayer p : level.players()) {
            boolean isAttacker = isOnLeft(p) == attackersOnLeft; // placeholder team split
            boolean winner = attackersWin == isAttacker;
            if (winner) {
                EconomyData.setCredits(p, EconomyData.getCredits(p) + 3000);
                EconomyData.setLossStreak(p, 0);
            } else {
                int streak = EconomyData.getLossStreak(p) + 1;
                EconomyData.setLossStreak(p, streak);
                int bonus = switch (Math.min(streak, 3)) { case 1 -> 1900; case 2 -> 2400; default -> 2900; };
                EconomyData.setCredits(p, EconomyData.getCredits(p) + bonus);
            }
        }
    }

    public static void onSpikePlanted(ServerLevel level, boolean attackersOnLeft) {
        for (ServerPlayer p : level.players()) {
            if (isOnLeft(p) == attackersOnLeft) {
                EconomyData.setCredits(p, EconomyData.getCredits(p) + 300);
            }
        }
    }

    private static boolean isOnLeft(ServerPlayer p) {
        // MVP placeholder: parity split
        return (p.getUUID().getLeastSignificantBits() & 1L) == 0L;
    }
}


