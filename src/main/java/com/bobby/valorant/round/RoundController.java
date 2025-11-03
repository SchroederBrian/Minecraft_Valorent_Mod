package com.bobby.valorant.round;

import com.bobby.valorant.util.SoundManager;
import com.bobby.valorant.world.item.IWeapon;
import com.bobby.valorant.world.item.WeaponAmmoData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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
    private boolean spikeDefused = false; // tracks if spike has been defused this round

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
			// Teleport players to their team spawn areas (if configured)
			for (ServerPlayer sp : level.players()) {
				var server = sp.getServer();
				if (server == null) continue;
				net.minecraft.world.scores.Scoreboard sb = server.getScoreboard();
				net.minecraft.world.scores.PlayerTeam team = sb.getPlayersTeam(sp.getScoreboardName());
				if (team == null) continue;
				String teamId = "A".equals(team.getName()) ? "A" : "V";
				var area = com.bobby.valorant.spawn.SpawnAreaManager.get(level, teamId);
				if (area != null && sp.connection != null) {
					var pos = com.bobby.valorant.spawn.SpawnAreaManager.chooseRandomPointInside(area, level, level.random);
					if (pos != null) {
						sp.connection.teleport(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, sp.getYRot(), sp.getXRot());
					}
				}
			}
        // Reset spike planted and defused flags at round start
        com.bobby.valorant.Config.COMMON.spikePlanted.set(false);
        spikeDefused = false;
        // Capture spawn positions for lock and apply OT budget/default pistols
        for (ServerPlayer sp : level.players()) {
            buySpawn.put(sp.getUUID(), sp.position());
            // Set max health to 100 and heal to full at round start
            if (sp.getAttribute(Attributes.MAX_HEALTH) != null) {
                sp.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
            }
            sp.setHealth(sp.getMaxHealth());
            if (overtimeBudget > 0) {
                com.bobby.valorant.economy.EconomyData.setCredits(sp, overtimeBudget);
                com.bobby.valorant.economy.EconomyData.syncCredits(sp);
                // Give heavy armor baseline in OT
                sp.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE));
            }
            ensureDefaultPistol(sp);
            ensureKnife(sp);
            resetPlayerAmmo(sp);

            // Reset abilities and effects for the new round, removing spectator mode
            if (!sp.isCreative()) {
                sp.getAbilities().flying = false;
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
        plantSpike(null); // For backward compatibility, spawn at default location if no position provided
    }

    public void plantSpike(Vec3 position) {
        if (phase == Phase.ROUND) {
            phase = Phase.PLANTED;
            remainingSeconds = PLANTED_SECONDS;

            // Spawn planted spike entity if position is provided
            if (position != null) {
                com.bobby.valorant.spike.SpikePlantingHandler.spawnPlanted(level, position);
                com.bobby.valorant.Config.COMMON.spikePlanted.set(true);
            }

            EconomyManager.onSpikePlanted(level, attackersOnLeft);
            syncNow();
        }
    }

    public void defuseSpikeFull() {
        if (phase == Phase.PLANTED && !spikeDefused) {
            // Defenders win - set defused flag to prevent further defusing
            spikeDefused = true;
            System.out.println("[RoundController] Spike defused successfully, spikeDefused set to true");
            // Play spike defused sound
            SoundManager.playSpikeDefusedSound(level);
            awardDefenders();
            enterPost();
        } else {
            System.out.println("[RoundController] defuseSpikeFull called but conditions not met - phase: " + phase + ", spikeDefused: " + spikeDefused);
        }
    }

    public Phase phase() { return phase; }

    public boolean isSpikeDefused() { return spikeDefused; }

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
                        // Play round start countdown sound
                        SoundManager.playRoundStartCountdownSound(level);
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
            // Announcer sounds at specific times for both ROUND and PLANTED phases
            if (phase == Phase.ROUND || phase == Phase.PLANTED) {
                if (remainingSeconds == 30) {
                    SoundManager.playAnnouncer30SecondsLeft(level);
                } else if (remainingSeconds == 10) {
                    SoundManager.playAnnouncer10SecondsLeft(level);
                }
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
        // Play attackers win sound
        SoundManager.playMatchVictorySound(level, true);
    }

    private void awardDefenders() {
        if (attackersOnLeft) rightScore++; else leftScore++;
        EconomyManager.onRoundWin(level, false, attackersOnLeft);
        // Play defenders win sound
        SoundManager.playMatchVictorySound(level, false);
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
            var server = sp.getServer();
            if (server == null) continue;
            net.minecraft.world.scores.Scoreboard sb = server.getScoreboard();
            net.minecraft.world.scores.PlayerTeam team = sb.getPlayersTeam(sp.getScoreboardName());
            if (team == null) continue;
            // MVP rule: Attackers are always team "A"
            boolean isAttacker = "A".equals(team.getName());
            if (isAttacker) attackers.add(sp);
        }
        if (attackers.isEmpty()) return;
        ServerPlayer chosen = attackers.get(level.random.nextInt(attackers.size()));
        // Ensure only one Spike exists: remove from everyone first
        for (ServerPlayer sp : level.players()) {
            removeAllSpikes(sp);
        }
        var inv = chosen.getInventory();
        // Place Spike into hotbar slot 3 (the 4th slot)
        inv.setItem(3, com.bobby.valorant.registry.ModItems.SPIKE.get().getDefaultInstance());
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
        net.minecraft.world.item.ItemStack itemInSlot = sp.getInventory().getItem(1);
        if (itemInSlot.getItem() instanceof com.bobby.valorant.world.item.IWeapon) {
            return;
        }
        // If slot is not a pistol (could be empty or something else), give Classic.
        sp.getInventory().setItem(1, com.bobby.valorant.registry.ModItems.CLASSIC.get().getDefaultInstance());
    }

    public static void ensureKnife(ServerPlayer sp) {
        // First, remove any existing knives to prevent duplication
        sp.getInventory().removeItem(com.bobby.valorant.registry.ModItems.KNIFE.get().getDefaultInstance());
        // Then, place a new knife in slot 3 (index 2)
        sp.getInventory().setItem(2, com.bobby.valorant.registry.ModItems.KNIFE.get().getDefaultInstance());
    }

    private void resetPlayerAmmo(ServerPlayer sp) {
        for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
            ItemStack stack = sp.getInventory().getItem(i);
            if (stack.getItem() instanceof IWeapon weapon) {
                WeaponAmmoData.setCurrentAmmo(stack, weapon.getMagazineSize());
                WeaponAmmoData.setReserveAmmo(stack, weapon.getMaxReserveAmmo());

                // Sync ammo to client
                int currentAmmo = WeaponAmmoData.getCurrentAmmo(stack);
                int reserveAmmo = WeaponAmmoData.getReserveAmmo(stack);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                    new com.bobby.valorant.network.SyncWeaponAmmoPacket(i, currentAmmo, reserveAmmo));
            }
        }
    }

    // Persistence is intentionally omitted for MVP; controller resets on server restart/world reload.
}


