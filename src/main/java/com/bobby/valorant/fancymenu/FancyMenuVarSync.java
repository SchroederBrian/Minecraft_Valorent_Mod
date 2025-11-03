package com.bobby.valorant.fancymenu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.bobby.valorant.Config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class FancyMenuVarSync {
    private FancyMenuVarSync() {}

    public static void updateAll(MinecraftServer server) {
        if (server == null) return;
        if (!Config.COMMON.enableFancyMenuTeamVars.get()) return;
        if (server.getPlayerList().getPlayerCount() == 0) return; // player context required
        updateTeam(server, "A");
        updateTeam(server, "V");
    }

    public static void onPlayerLeave(ServerPlayer sp) {
        if (sp == null || sp.getServer() == null) return;
        if (!Config.COMMON.enableFancyMenuTeamVars.get()) return;
        updateAll(sp.getServer());
    }

    public static void updateTeam(MinecraftServer server, String teamId) {
        if (server == null) return;
        String normTeam = teamId.equalsIgnoreCase("A") ? "A" : "V";

        int maxSlots = Math.max(1, Math.min(10, Config.COMMON.teamVarSlots.get()));
        String prefix = Config.COMMON.fancymenuVariablePrefix.get();
        if (prefix == null) prefix = "";

        ensureCapacity(normTeam, maxSlots);

        List<ServerPlayer> players = getPlayersForTeam(server, normTeam);
        players.sort(Comparator.comparing(p -> p.getGameProfile().getName().toLowerCase(Locale.ROOT)));

        // Assign to slots and set variables; empty slots default to "Steve"
        for (int slot = 0; slot < maxSlots; slot++) {
            String currentName = slot < players.size() ? players.get(slot).getGameProfile().getName() : "Steve";
            String varName = buildVarName(prefix, normTeam, slot + 1);
            runFmSet(server, varName, currentName);
            setLastName(normTeam, slot, "Steve".equals(currentName) ? "" : currentName);
        }
    }

    private static List<ServerPlayer> getPlayersForTeam(MinecraftServer server, String teamId) {
        List<ServerPlayer> list = new ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            var t = p.getTeam();
            if (t != null && teamId.equalsIgnoreCase(t.getName())) list.add(p);
        }
        return list;
    }

    private static String buildVarName(String prefix, String teamId, int slotIndex1Based) {
        String teamLower = teamId.equals("A") ? "a" : "v";
        String base = teamLower + "-p" + slotIndex1Based + "-name";
        return prefix.isEmpty() ? base : (prefix + base);
    }

    private static void runFmSet(MinecraftServer server, String varName, String value) {
        if (value == null || value.isEmpty()) value = "Steve";
        String sanitized = value.replace("\"", "");
        String cmd = "/fmvariable set " + varName + " false " + sanitized;
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;
        for (ServerPlayer p : players) {
            server.getCommands().performPrefixedCommand(p.createCommandSourceStack().withSuppressedOutput(), cmd);
        }
    }

    // In-memory last assignments (per runtime). Persistence is not required for correct runtime behavior.
    private static final java.util.List<String> lastA = new java.util.ArrayList<>();
    private static final java.util.List<String> lastV = new java.util.ArrayList<>();

    private static void ensureCapacity(String teamId, int size) {
        java.util.List<String> ref = teamId.equals("A") ? lastA : lastV;
        while (ref.size() < size) ref.add("");
    }

    private static void setLastName(String teamId, int slot, String name) {
        java.util.List<String> ref = teamId.equals("A") ? lastA : lastV;
        ensureCapacity(teamId, slot + 1);
        ref.set(slot, name == null ? "" : name);
    }
}


