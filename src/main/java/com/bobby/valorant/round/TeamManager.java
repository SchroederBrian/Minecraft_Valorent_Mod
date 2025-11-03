package com.bobby.valorant.round;

import com.bobby.valorant.Config;
import com.bobby.valorant.fancymenu.FancyMenuVarSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public final class TeamManager {
    private TeamManager() {}

    public static boolean joinTeam(ServerPlayer player, String teamId) {
        if (player.getServer() == null) return false;
        teamId = teamId.equalsIgnoreCase("A") ? "A" : "V";
        Scoreboard sb = player.getServer().getScoreboard();
        PlayerTeam team = sb.getPlayerTeam(teamId);
        if (team == null) team = sb.addPlayerTeam(teamId);
        int max = Config.COMMON.maxTeamSize.get();
        int currentSize = team.getPlayers().size();
        if (currentSize >= max) return false;

        // remove from other team if present
        PlayerTeam existing = sb.getPlayersTeam(player.getScoreboardName());
        if (existing != null && existing != team) sb.removePlayerFromTeam(player.getScoreboardName(), existing);

        sb.addPlayerToTeam(player.getScoreboardName(), team);
        // Clear any previous agent lock so the player can re-lock on new team
        AgentLockManager.get(player.getServer()).onTeamChanged(player);
        FancyMenuVarSync.updateAll(player.getServer());
        return true;
    }

    public static boolean switchTeam(ServerPlayer player) {
        if (player.getServer() == null) return false;
        Scoreboard sb = player.getServer().getScoreboard();
        PlayerTeam existing = sb.getPlayersTeam(player.getScoreboardName());
        String target = (existing != null && existing.getName().equals("A")) ? "V" : "A";
        boolean ok = joinTeam(player, target);
        if (ok) {
            AgentLockManager.get(player.getServer()).onTeamChanged(player);
            FancyMenuVarSync.updateAll(player.getServer());
        }
        return ok;
    }
}


