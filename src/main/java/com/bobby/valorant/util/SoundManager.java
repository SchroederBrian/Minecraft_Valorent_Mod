package com.bobby.valorant.util;

import com.bobby.valorant.registry.ModSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

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
        // Default to classic pistol reload for now
        playSoundForPlayer(player, getSoundLocation("classic.reload"), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public static void playReloadSound(ServerPlayer player, String weaponType) {
        ResourceLocation soundLocation = switch (weaponType.toLowerCase()) {
            case "classic" -> getSoundLocation("classic.reload");
            case "ghost" -> getSoundLocation("ghost.reload");
            case "valor_rifle", "vandal" -> getSoundLocation("valor_rifle.reload");
            default -> getSoundLocation("classic.reload"); // fallback
        };
        playSoundForPlayer(player, soundLocation, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // Spike sounds
    public static void playSpikePlantSound(net.minecraft.server.level.ServerLevel level, double x, double y, double z) {
        playSoundAt(level, x, y, z, getSoundLocation("spike.plant"), SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    public static void playSpikeDefuseSound(net.minecraft.server.level.ServerLevel level, double x, double y, double z) {
        playSoundAt(level, x, y, z, getSoundLocation("spike.defuse"), SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    // UI sounds
    public static void playBuySuccessSound(ServerPlayer player) {
        playSoundForPlayer(player, getSoundLocation("ui.buy_success"), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public static void playBuyFailureSound(ServerPlayer player) {
        playSoundForPlayer(player, getSoundLocation("ui.buy_failure"), SoundSource.PLAYERS, 0.5f, 1.0f);
    }
}
