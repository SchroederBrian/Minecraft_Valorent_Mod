package com.bobby.valorant.util;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.ClassicPistolItem;
import com.bobby.valorant.world.item.GhostPistolItem;
import com.bobby.valorant.world.item.VandalRifleItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

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
            player.playNotifySound(event, source, volume, pitch);
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
