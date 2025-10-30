package com.bobby.valorant.round;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;

import java.util.WeakHashMap;

/**
 * Server-side controller that manages round phases and remaining time.
 * MVP: buy -> round -> post. Plant/defuse scaffolding is included but not yet bound to spikes.
 */
public final class RoundController {
    public enum Phase { IDLE, BUY, ROUND, PLANTED, POST }

    private final ServerLevel level;
    private Phase phase = Phase.IDLE;
    private int remainingSeconds = 0;
    private int leftScore = 0;
    private int rightScore = 0;
    private long lastSyncSecond = 0L; // last game-time second synced

    private static final int BUY_SECONDS = 30;
    private static final int ROUND_SECONDS = 100;
    private static final int POST_SECONDS = 10;
    private static final int PLANTED_SECONDS = 45; // spike timer (MVP)

    private boolean attackersOnLeft = true; // flips at halftime
    private int roundCounter = 0;
    private int overtimeBudget = 0; // 0 disables
    private final java.util.HashMap<java.util.UUID, net.minecraft.world.phys.Vec3> buySpawn = new java.util.HashMap<>();
    private static final double SPAWN_RADIUS = 12.0;

    private RoundController(ServerLevel level) { this.level = level; }

    private static final WeakHashMap<ServerLevel, RoundController> INSTANCES = new WeakHashMap<>();

    public static RoundController get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, RoundController::new);
    }

    public void startRound(int seconds) {
        // Enter BUY then ROUND
        phase = Phase.BUY;
        remainingSeconds = seconds > 0 ? seconds : BUY_SECONDS;
        roundCounter++;
        // Capture spawn positions for lock and apply OT budget/default pistols
        for (ServerPlayer sp : level.players()) {
            buySpawn.put(sp.getUUID(), sp.position());
            // Heal to full at round start
            sp.setHealth(sp.getMaxHealth());
            if (overtimeBudget > 0) {
                com.bobby.valorant.economy.EconomyData.setCredits(sp, overtimeBudget);
                com.bobby.valorant.economy.EconomyData.syncCredits(sp);
                // Give heavy armor baseline in OT
                sp.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE));
            }
            ensureDefaultPistol(sp);
            ensureKnife(sp);

            // Reset abilities and effects for the new round, removing spectator mode
            if (!sp.isCreative()) {
                sp.getAbilities().mayfly = false;
                sp.getAbilities().invulnerable = false;
                sp.onUpdateAbilities();
            }
            sp.removeEffect(MobEffects.INVISIBILITY);
        }
        // Give Spike to a random attacker at round start
        tryAssignSpikeToRandomAttacker();
        syncNow();
    }

    public int getCurrentRoundId() { return roundCounter; }

    public void stopRound() {
        phase = Phase.IDLE;
        remainingSeconds = 0;
        syncNow();
    }

    public void setScores(int left, int right) {
        this.leftScore = Math.max(0, left);
        this.rightScore = Math.max(0, right);
        syncNow();
    }

    public void plantSpike() {
        if (phase == Phase.ROUND) {
            phase = Phase.PLANTED;
            remainingSeconds = PLANTED_SECONDS;
            EconomyManager.onSpikePlanted(level, attackersOnLeft);
            syncNow();
        }
    }

    public void defuseSpikeFull() {
        if (phase == Phase.PLANTED) {
            // Defenders win
            awardDefenders();
            enterPost();
        }
    }

    public Phase phase() { return phase; }

    public boolean isInSpawn(ServerPlayer sp) {
        net.minecraft.world.phys.Vec3 center = buySpawn.get(sp.getUUID());
        if (center == null) return true; // before first capture
        return sp.position().distanceToSqr(center) <= SPAWN_RADIUS * SPAWN_RADIUS;
    }

    public void setOvertimeBudget(int credits) { this.overtimeBudget = Math.max(0, credits); }

    public void tick() {
        if (phase == Phase.IDLE) return;
        long seconds = level.getGameTime() / 20L;
        if (seconds != lastSyncSecond) {
            lastSyncSecond = seconds;
            if (remainingSeconds > 0) {
                remainingSeconds--;
            }

            // Phase transitions
            switch (phase) {
                case BUY -> {
                    if (remainingSeconds <= 0) {
                        phase = Phase.ROUND;
                        remainingSeconds = ROUND_SECONDS;
                    }
                }
                case ROUND -> {
                    if (remainingSeconds <= 0) {
                        // Time over -> defenders win if no plant
                        awardDefenders();
                        enterPost();
                    }
                }
                case PLANTED -> {
                    if (remainingSeconds <= 0) {
                        // Spike explosion -> attackers win
                        awardAttackers();
                        enterPost();
                    }
                }
                case POST -> {
                    if (remainingSeconds <= 0) {
                        phase = Phase.IDLE; // wait for next start
                    }
                }
                default -> {}
            }

            syncNow();
        }
    }

    private void enterPost() {
        phase = Phase.POST;
        remainingSeconds = POST_SECONDS;
    }

    private void awardAttackers() {
        if (attackersOnLeft) leftScore++; else rightScore++;
        EconomyManager.onRoundWin(level, true, attackersOnLeft);
    }

    private void awardDefenders() {
        if (attackersOnLeft) rightScore++; else leftScore++;
        EconomyManager.onRoundWin(level, false, attackersOnLeft);
    }

    private void syncNow() {
        boolean running = phase != Phase.IDLE;
        com.bobby.valorant.network.SyncRoundStatePacket pkt =
                new com.bobby.valorant.network.SyncRoundStatePacket(running, remainingSeconds, leftScore, rightScore, phase.ordinal());
        for (ServerPlayer sp : level.players()) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, pkt);
        }
        RoundState.update(running, remainingSeconds, leftScore, rightScore);
        RoundState.updatePhase(phase.ordinal());
    }

    private void tryAssignSpikeToRandomAttacker() {
        java.util.List<ServerPlayer> attackers = new java.util.ArrayList<>();
        for (ServerPlayer sp : level.players()) {
            net.minecraft.world.scores.Scoreboard sb = sp.getServer().getScoreboard();
            net.minecraft.world.scores.PlayerTeam team = sb.getPlayersTeam(sp.getScoreboardName());
            if (team == null) continue;
            boolean isAttacker = attackersOnLeft ? "A".equals(team.getName()) : "V".equals(team.getName());
            if (isAttacker) attackers.add(sp);
        }
        if (attackers.isEmpty()) return;
        ServerPlayer chosen = attackers.get(level.random.nextInt(attackers.size()));
        // Ensure only one Spike exists: remove from others
        for (ServerPlayer sp : level.players()) {
            removeAllSpikes(sp);
        }
        chosen.getInventory().add(com.bobby.valorant.registry.ModItems.SPIKE.get().getDefaultInstance());
    }

    private static void removeAllSpikes(ServerPlayer sp) {
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            net.minecraft.world.item.ItemStack s = sp.getInventory().getItem(i);
            if (s.is(com.bobby.valorant.registry.ModItems.SPIKE.get())) {
                sp.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }

    public static void ensureDefaultPistol(ServerPlayer sp) {
        // If no simple sidearm present, give a basic one
        boolean hasSecondary = false;
        int size = sp.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            net.minecraft.world.item.ItemStack s = sp.getInventory().getItem(i);
            if (s.is(com.bobby.valorant.registry.ModItems.CLASSIC.get()) || s.is(com.bobby.valorant.registry.ModItems.GHOST.get())) {
                hasSecondary = true; break;
            }
        }
        if (!hasSecondary) {
            sp.getInventory().add(com.bobby.valorant.registry.ModItems.CLASSIC.get().getDefaultInstance());
        }
    }

    private static void ensureKnife(ServerPlayer sp) {
        if (!sp.getInventory().contains(com.bobby.valorant.registry.ModItems.KNIFE.get().getDefaultInstance())) {
            sp.getInventory().add(com.bobby.valorant.registry.ModItems.KNIFE.get().getDefaultInstance());
        }
    }

    // Persistence is intentionally omitted for MVP; controller resets on server restart/world reload.
}


