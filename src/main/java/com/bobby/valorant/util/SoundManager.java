package com.bobby.valorant.util;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.ClassicPistolItem;
import com.bobby.valorant.world.item.GhostPistolItem;
import com.bobby.valorant.world.item.VandalRifleItem;
import com.bobby.valorant.world.item.SheriffItem;
import com.bobby.valorant.world.item.FrenzyPistolItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class SoundManager {
    private SoundManager() {}

    // Sound cooldown system to prevent duplicate sounds
    private static final Object2LongMap<String> LAST_SOUND_PLAY_TIME = new Object2LongOpenHashMap<>();
    private static final long SOUND_COOLDOWN_TICKS = 2; // 2 ticks = 0.1 seconds minimum between same sounds

    /**
     * Check if a sound can be played (not on cooldown)
     */
    private static boolean canPlaySound(String soundKey, ServerLevel level) {
        long currentTime = level.getGameTime();
        long lastPlayTime = LAST_SOUND_PLAY_TIME.getLong(soundKey);

        if (currentTime - lastPlayTime >= SOUND_COOLDOWN_TICKS) {
            LAST_SOUND_PLAY_TIME.put(soundKey, currentTime);
            return true;
        }

        return false; // Sound is on cooldown
    }

    // Easy sound playing methods

    /**
     * Play a sound for all players in the world
     */
    public static void playSound(net.minecraft.server.level.ServerLevel level, ResourceLocation sound, SoundSource source, float volume, float pitch) {
        BuiltInRegistries.SOUND_EVENT.get(sound).ifPresent(reference ->
            level.playSound(null, 0, 0, 0, reference.value(), source, volume, pitch));
    }

    /**
     * Play a sound for a specific player
     */
    public static void playSoundForPlayer(ServerPlayer player, ResourceLocation sound, SoundSource source, float volume, float pitch) {
        BuiltInRegistries.SOUND_EVENT.get(sound).ifPresent(reference ->
            player.playNotifySound(reference.value(), source, volume, pitch));
    }

    /**
     * Play a sound at a specific location
     */
    public static void playSoundAt(net.minecraft.server.level.ServerLevel level, double x, double y, double z, ResourceLocation sound, SoundSource source, float volume, float pitch) {
        BuiltInRegistries.SOUND_EVENT.get(sound).ifPresent(reference ->
            level.playSound(null, x, y, z, reference.value(), source, volume, pitch));
    }

    // Weapon-specific sound methods

    // Helper method to get ResourceLocation for sound events
    private static ResourceLocation getSoundLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(com.bobby.valorant.Valorant.MODID, path);
    }

    public static void playReloadSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        var held = player.getMainHandItem();
        String soundPath;
        if (held != null) {
            var item = held.getItem();
            if (item instanceof ClassicPistolItem) {
                soundPath = "classic.reload";
            } else if (item instanceof GhostPistolItem) {
                soundPath = "ghost.reload";
            } else if (item instanceof VandalRifleItem) {
                soundPath = "vandal.reload";
            } else if (item instanceof SheriffItem) {
                soundPath = "sheriff.reload";
            } else if (item instanceof FrenzyPistolItem) {
                soundPath = "frenzy.reload";
            } else {
                soundPath = "";
            }
        } else {
            soundPath = "";
        }
        float vol = com.bobby.valorant.Config.COMMON.soundReloadVolume.get().floatValue();
        playResolvedForPlayer(player, soundPath, SoundSource.PLAYERS, vol, 1.0f);
    }

    public static void playReloadSound(ServerPlayer player, String weaponType) {
        ResourceLocation soundLocation = switch (weaponType.toLowerCase()) {
            case "classic" -> getSoundLocation("classic.reload");
            case "ghost" -> getSoundLocation("ghost.reload");
            case "valor_rifle", "vandal" -> getSoundLocation("vandal.reload");
            case "sheriff" -> getSoundLocation("sheriff.reload");
            case "frenzy" -> getSoundLocation("frenzy.reload");
            default -> getSoundLocation(""); // fallback
        };
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundReloadVolume.get().floatValue();
        String path = soundLocation.getPath();
        playResolvedForPlayer(player, path, SoundSource.PLAYERS, vol, 1.0f);
    }

    // Spike sounds
    public static void playSpikePlantSound(net.minecraft.server.level.ServerLevel level, double x, double y, double z) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundSpikeVolume.get().floatValue();
        playResolvedAt(level, x, y, z, "spike.plant", SoundSource.BLOCKS, vol, 1.0f);
    }

    public static void playSpikeDefuseSound(net.minecraft.server.level.ServerLevel level, double x, double y, double z) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundSpikeVolume.get().floatValue();
        playResolvedAt(level, x, y, z, "spike.defuse", SoundSource.BLOCKS, vol, 1.0f);
    }

    // Planting loop control
    public static void startSpikePlantingSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundSpikeVolume.get().floatValue();
        playResolvedForPlayer(player, "spike.plant", SoundSource.PLAYERS, vol, 1.0f);
    }

    public static void stopSpikePlantingSound(ServerPlayer player) {
        try {
            ClientboundStopSoundPacket pkt = new ClientboundStopSoundPacket(getSoundLocation("spike.plant"), SoundSource.PLAYERS);
            player.connection.send(pkt);
        } catch (Exception e) {
            Valorant.LOGGER.warn("[Sound] Failed to stop spike planting sound: {}", e.toString());
        }
    }

    // UI sounds
    public static void playBuySuccessSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        playResolvedForPlayer(player, "ui.buy_success", SoundSource.PLAYERS, vol, 1.0f);
    }

    public static void playBuyFailureSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue() * 0.5f;
        playResolvedForPlayer(player, "ui.buy_failure", SoundSource.PLAYERS, vol, 1.0f);
    }

    // Weapon shot sounds (for guns)
    public static void playWeaponShotSound(ServerPlayer player, String weaponType) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;

        // Create unique sound key for cooldown tracking (player + weapon type)
        String soundKey = player.getUUID().toString() + "_" + weaponType + "_shot";

        // Check cooldown to prevent duplicate sounds
        if (!canPlaySound(soundKey, (ServerLevel) player.level())) {
            return; // Sound is on cooldown, skip playing
        }

        float vol = com.bobby.valorant.Config.COMMON.soundWeaponVolume.get().floatValue();
        String soundPath = getWeaponShotSoundPath(weaponType, player.level().getRandom().nextInt(4) + 1);
        playResolvedForPlayer(player, soundPath, SoundSource.PLAYERS, vol, 1.0f);
    }

    // Knife hit sounds
    public static void playKnifeHitSound(ServerPlayer player, boolean isHeavyAttack) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;

        // Create unique sound key for cooldown tracking (player + attack type)
        String attackType = isHeavyAttack ? "heavy" : "light";
        String soundKey = player.getUUID().toString() + "_knife_" + attackType + "_hit";

        // Check cooldown to prevent duplicate sounds
        if (!canPlaySound(soundKey, (ServerLevel) player.level())) {
            return; // Sound is on cooldown, skip playing
        }

        float vol = com.bobby.valorant.Config.COMMON.soundWeaponVolume.get().floatValue();
        String soundPath = getKnifeHitSoundPath(isHeavyAttack, player.level().getRandom().nextInt(3) + 1);
        playResolvedForPlayer(player, soundPath, SoundSource.PLAYERS, vol, 1.0f);
    }

    // Weapon equip sounds
    public static void playWeaponEquipSound(ServerPlayer player, String weaponType) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundWeaponVolume.get().floatValue();
        String soundPath = getWeaponEquipSoundPath(weaponType, player.level().getRandom().nextInt(2) + 1);
        playResolvedForPlayer(player, soundPath, SoundSource.PLAYERS, vol, 1.0f);
    }

    // Announcer sounds
    public static void playAnnouncer30SecondsLeft(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundAnnouncerVolume.get().floatValue();
        playResolvedForAll(level, "announcer.30_seconds_left", SoundSource.VOICE, vol, 1.0f);
    }

    public static void playAnnouncer10SecondsLeft(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundAnnouncerVolume.get().floatValue();
        playResolvedForAll(level, "announcer.10_seconds_left", SoundSource.VOICE, vol, 1.0f);
    }

    // UI sounds
    public static void playSpikePlantedSound(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundAnnouncerVolume.get().floatValue();
        String soundPath = level.getRandom().nextBoolean() ? "announcer.spike_planted_1" : "announcer.spike_planted_2";
        System.out.println("Playing spike planted sound: " + soundPath);
        playResolvedForAll(level, soundPath, SoundSource.VOICE, vol, 1.0f);
    }

    public static void playSpikeDefusedSound(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundAnnouncerVolume.get().floatValue();
        playResolvedForAll(level, "ui.spike_defused", SoundSource.VOICE, vol, 1.0f);
    }

    public static void playRoundStartCountdownSound(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        playResolvedAt(level, 0, 0, 0, "ui.round_start_countdown", SoundSource.VOICE, vol, 1.0f);
    }

    public static void playMatchVictorySound(ServerLevel level, boolean isAttackers) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundAnnouncerVolume.get().floatValue();
        String soundPath = isAttackers ? "ui.attackers_win" : "ui.defenders_win";
        playResolvedForAll(level, soundPath, SoundSource.VOICE, vol, 1.0f);
    }

    public static void playMatchVictorySound(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundAnnouncerVolume.get().floatValue();
        String soundPath = level.getRandom().nextBoolean() ? "ui.match_victory_1" : "ui.match_victory_2";
        playResolvedForAll(level, soundPath, SoundSource.VOICE, vol, 1.0f);
    }

    public static void playAgentSelectSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        playResolvedForPlayer(player, "ui.select_agent", SoundSource.PLAYERS, vol, 1.0f);
    }

    public static void playAgentLockInSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        playResolvedForPlayer(player, "ui.lock_in", SoundSource.PLAYERS, vol, 1.0f);
    }

    public static void playAgentHoverSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue() * 0.3f;
        playResolvedForPlayer(player, "ui.hover_over_agent", SoundSource.PLAYERS, vol, 1.0f);
    }

    // Helper methods for weapon sound selection
    private static String getWeaponShotSoundPath(String weaponType, int variant) {
        return switch (weaponType.toLowerCase()) {
            case "classic" -> "classic.single_shot_" + variant;
            case "ghost" -> "ghost.shot_" + variant;
            case "vandal" -> "vandal.shot_" + variant;
            case "sheriff" -> "sheriff.shot_" + variant;
            case "frenzy" -> "frenzy.shot_" + variant;
            default -> "classic.single_shot_" + variant;
        };
    }

    private static String getKnifeHitSoundPath(boolean isHeavyAttack, int variant) {
        if (isHeavyAttack) {
            return "knife.heavy_hit_" + variant;
        } else {
            return "knife.light_hit_" + variant;
        }
    }

    private static String getWeaponEquipSoundPath(String weaponType, int variant) {
        return switch (weaponType.toLowerCase()) {
            case "classic" -> "classic.equip";
            case "ghost" -> "ghost.equip_" + variant;
            case "vandal" -> "vandal.equip";
            case "sheriff" -> "sheriff.equip";
            case "frenzy" -> "frenzy.equip";
            default -> "classic.equip";
        };
    }

    // --- Resolution and debug helpers ---

    private static SoundEvent resolveEvent(String path) {
        ResourceLocation id = getSoundLocation(path);
        var opt = BuiltInRegistries.SOUND_EVENT.get(id);
        if (opt.isPresent()) {
            Valorant.LOGGER.debug("[SoundManager] Successfully resolved sound event for path: {}", path);
            return opt.get().value();
        }
        Valorant.LOGGER.warn("[SoundManager] Could not resolve sound event for path: {}", path);
        return null;
    }

    private static void playResolvedForAll(ServerLevel level, String path, SoundSource source, float volume, float pitch) {
        SoundEvent event = resolveEvent(path);
        if (event == null) return;
        var server = level.getServer();
        if (server == null) return;
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), event, source, volume, pitch);
        }
    }

    private static void playResolvedForPlayer(ServerPlayer player, String path, SoundSource source, float volume, float pitch) {
        SoundEvent event = resolveEvent(path);
        if (event != null) {
            // Broadcast from server so client receives via world packet (more robust than notify-only on some setups)
            var level = (net.minecraft.server.level.ServerLevel) player.level();
            level.playSound(null, player.getX(), player.getY(), player.getZ(), event, source, volume, pitch);
        } else {
        }
    }

    private static void playResolvedAt(net.minecraft.server.level.ServerLevel level, double x, double y, double z, String path, SoundSource source, float volume, float pitch) {
        SoundEvent event = resolveEvent(path);
        if (event != null) {
            level.playSound(null, x, y, z, event, source, volume, pitch);
        } else {
        }
    }
}
