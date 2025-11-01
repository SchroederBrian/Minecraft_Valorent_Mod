package com.bobby.valorant.util;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.ClassicPistolItem;
import com.bobby.valorant.world.item.GhostPistolItem;
import com.bobby.valorant.world.item.VandalRifleItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import java.util.Optional;

public class SoundManager {
    private SoundManager() {}

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
            } else {
                soundPath = "classic.reload";
            }
        } else {
            soundPath = "classic.reload";
        }
        float vol = com.bobby.valorant.Config.COMMON.soundReloadVolume.get().floatValue();
        debug("ReloadSound", player, soundPath, vol, 1.0f);
        playResolvedForPlayer(player, soundPath, SoundSource.PLAYERS, vol, 1.0f);
    }

    public static void playReloadSound(ServerPlayer player, String weaponType) {
        ResourceLocation soundLocation = switch (weaponType.toLowerCase()) {
            case "classic" -> getSoundLocation("classic.reload");
            case "ghost" -> getSoundLocation("ghost.reload");
            case "valor_rifle", "vandal" -> getSoundLocation("vandal.reload");
            default -> getSoundLocation("classic.reload"); // fallback
        };
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundReloadVolume.get().floatValue();
        String path = soundLocation.getPath();
        debug("ReloadSound(type)", player, path, vol, 1.0f);
        playResolvedForPlayer(player, path, SoundSource.PLAYERS, vol, 1.0f);
    }

    // Spike sounds
    public static void playSpikePlantSound(net.minecraft.server.level.ServerLevel level, double x, double y, double z) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundSpikeVolume.get().floatValue();
        debug("SpikePlantSound", level, x, y, z, "spike.plant", vol, 1.0f);
        playResolvedAt(level, x, y, z, "spike.plant", SoundSource.BLOCKS, vol, 1.0f);
    }

    public static void playSpikeDefuseSound(net.minecraft.server.level.ServerLevel level, double x, double y, double z) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundSpikeVolume.get().floatValue();
        debug("SpikeDefuseSound", level, x, y, z, "spike.defuse", vol, 1.0f);
        playResolvedAt(level, x, y, z, "spike.defuse", SoundSource.BLOCKS, vol, 1.0f);
    }

    // Planting loop control
    public static void startSpikePlantingSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundSpikeVolume.get().floatValue();
        debug("SpikePlantingStart", player, "spike.plant", vol, 1.0f);
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
        debug("BuySuccessSound", player, "ui.buy_success", vol, 1.0f);
        playResolvedForPlayer(player, "ui.buy_success", SoundSource.PLAYERS, vol, 1.0f);
    }

    public static void playBuyFailureSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue() * 0.5f;
        debug("BuyFailureSound", player, "ui.buy_failure", vol, 1.0f);
        playResolvedForPlayer(player, "ui.buy_failure", SoundSource.PLAYERS, vol, 1.0f);
    }

    // Weapon shot sounds
    public static void playWeaponShotSound(ServerPlayer player, String weaponType) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundWeaponVolume.get().floatValue();
        String soundPath = getWeaponShotSoundPath(weaponType, player.level().getRandom().nextInt(4) + 1);
        debug("WeaponShotSound", player, soundPath, vol, 1.0f);
        playResolvedForPlayer(player, soundPath, SoundSource.PLAYERS, vol, 1.0f);
    }

    // Weapon equip sounds
    public static void playWeaponEquipSound(ServerPlayer player, String weaponType) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundWeaponVolume.get().floatValue();
        String soundPath = getWeaponEquipSoundPath(weaponType, player.level().getRandom().nextInt(2) + 1);
        debug("WeaponEquipSound", player, soundPath, vol, 1.0f);
        playResolvedForPlayer(player, soundPath, SoundSource.PLAYERS, vol, 1.0f);
    }

    // Announcer sounds
    public static void playAnnouncer30SecondsLeft(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundAnnouncerVolume.get().floatValue();
        debug("Announcer30Seconds", level, 0, 0, 0, "announcer.30_seconds_left", vol, 1.0f);
        playResolvedAt(level, 0, 0, 0, "announcer.30_seconds_left", SoundSource.VOICE, vol, 1.0f);
    }

    public static void playAnnouncer10SecondsLeft(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundAnnouncerVolume.get().floatValue();
        debug("Announcer10Seconds", level, 0, 0, 0, "announcer.10_seconds_left", vol, 1.0f);
        playResolvedAt(level, 0, 0, 0, "announcer.10_seconds_left", SoundSource.VOICE, vol, 1.0f);
    }

    // UI sounds
    public static void playSpikePlantedSound(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        debug("SpikePlantedSound", level, 0, 0, 0, "ui.spike_planted", vol, 1.0f);
        playResolvedAt(level, 0, 0, 0, "ui.spike_planted", SoundSource.VOICE, vol, 1.0f);
    }

    public static void playSpikeDefusedSound(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        debug("SpikeDefusedSound", level, 0, 0, 0, "ui.spike_defused", vol, 1.0f);
        playResolvedAt(level, 0, 0, 0, "ui.spike_defused", SoundSource.VOICE, vol, 1.0f);
    }

    public static void playRoundStartCountdownSound(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        debug("RoundStartCountdown", level, 0, 0, 0, "ui.round_start_countdown", vol, 1.0f);
        playResolvedAt(level, 0, 0, 0, "ui.round_start_countdown", SoundSource.VOICE, vol, 1.0f);
    }

    public static void playMatchVictorySound(ServerLevel level, boolean isAttackers) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        String soundPath = isAttackers ? "ui.attackers_win" : "ui.defenders_win";
        debug("MatchVictorySound", level, 0, 0, 0, soundPath, vol, 1.0f);
        playResolvedAt(level, 0, 0, 0, soundPath, SoundSource.VOICE, vol, 1.0f);
    }

    public static void playMatchVictorySound(ServerLevel level) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        String soundPath = level.getRandom().nextBoolean() ? "ui.match_victory_1" : "ui.match_victory_2";
        debug("MatchVictoryRandom", level, 0, 0, 0, soundPath, vol, 1.0f);
        playResolvedAt(level, 0, 0, 0, soundPath, SoundSource.VOICE, vol, 1.0f);
    }

    public static void playAgentSelectSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        debug("AgentSelectSound", player, "ui.select_agent", vol, 1.0f);
        playResolvedForPlayer(player, "ui.select_agent", SoundSource.PLAYERS, vol, 1.0f);
    }

    public static void playAgentLockInSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue();
        debug("AgentLockInSound", player, "ui.lock_in", vol, 1.0f);
        playResolvedForPlayer(player, "ui.lock_in", SoundSource.PLAYERS, vol, 1.0f);
    }

    public static void playAgentHoverSound(ServerPlayer player) {
        if (!com.bobby.valorant.Config.COMMON.soundEnabled.get()) return;
        float vol = com.bobby.valorant.Config.COMMON.soundUiVolume.get().floatValue() * 0.3f;
        debug("AgentHoverSound", player, "ui.hover_over_agent", vol, 1.0f);
        playResolvedForPlayer(player, "ui.hover_over_agent", SoundSource.PLAYERS, vol, 1.0f);
    }

    // Helper methods for weapon sound selection
    private static String getWeaponShotSoundPath(String weaponType, int variant) {
        return switch (weaponType.toLowerCase()) {
            case "classic" -> "classic.single_shot_" + variant;
            case "ghost" -> "ghost.shot_" + variant;
            case "vandal" -> "vandal.shot_" + variant;
            default -> "classic.single_shot_" + variant;
        };
    }

    private static String getWeaponEquipSoundPath(String weaponType, int variant) {
        return switch (weaponType.toLowerCase()) {
            case "classic" -> "classic.equip";
            case "ghost" -> "ghost.equip_" + variant;
            case "vandal" -> "vandal.equip";
            default -> "classic.equip";
        };
    }

    // --- Resolution and debug helpers ---

    private static SoundEvent resolveEvent(String path) {
        ResourceLocation id = getSoundLocation(path);
        var opt = BuiltInRegistries.SOUND_EVENT.get(id);
        if (opt.isPresent()) {
            return opt.get().value();
        }
        // Not found; log and return null
        Valorant.LOGGER.warn("[Sound] SoundEvent not found in registry: {}", id);
        return null;
    }

    private static void playResolvedForPlayer(ServerPlayer player, String path, SoundSource source, float volume, float pitch) {
        SoundEvent event = resolveEvent(path);
        if (event != null) {
            // Broadcast from server so client receives via world packet (more robust than notify-only on some setups)
            var level = (net.minecraft.server.level.ServerLevel) player.level();
            level.playSound(null, player.getX(), player.getY(), player.getZ(), event, source, volume, pitch);
        } else {
            Valorant.LOGGER.warn("[Sound] Skipping play (player) for missing event: {} (vol={}, pitch={})", path, volume, pitch);
        }
    }

    private static void playResolvedAt(net.minecraft.server.level.ServerLevel level, double x, double y, double z, String path, SoundSource source, float volume, float pitch) {
        SoundEvent event = resolveEvent(path);
        if (event != null) {
            level.playSound(null, x, y, z, event, source, volume, pitch);
        } else {
            Valorant.LOGGER.warn("[Sound] Skipping play (world) for missing event: {} at {},{},{} (vol={}, pitch={})", path, x, y, z, volume, pitch);
        }
    }

    private static void debug(String label, ServerPlayer player, String path, float volume, float pitch) {
        Valorant.LOGGER.info("[Sound:{}] player={}, id=valorant:{}, vol={}, pitch={}, enabled={}",
                label,
                player.getGameProfile().getName(),
                path,
                volume,
                pitch,
                com.bobby.valorant.Config.COMMON.soundEnabled.get());
    }

    private static void debug(String label, net.minecraft.server.level.ServerLevel level, double x, double y, double z, String path, float volume, float pitch) {
        Valorant.LOGGER.info("[Sound:{}] level={}, pos=({},{},{}), id=valorant:{}, vol={}, pitch={}, enabled={}",
                label,
                level.dimension().location(),
                x, y, z,
                path,
                volume,
                pitch,
                com.bobby.valorant.Config.COMMON.soundEnabled.get());
    }
}
