package com.bobby.valorant.events.client;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class MusicControlEvents {
    private MusicControlEvents() {}

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        // Only disable music if the config option is enabled
        if (!Config.COMMON.soundDisableBackgroundMusic.get()) {
            return;
        }

        // Prevent music sounds from playing at all (in-game and in GUIs)
        var sound = event.getSound();
        if (sound != null && sound.getSource() == SoundSource.MUSIC) {
            event.setSound(null); // Cancel the sound by setting it to null
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // Only disable music if the config option is enabled
        if (!Config.COMMON.soundDisableBackgroundMusic.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        // Disable music everywhere (in-game and in GUIs)
        // Only check if Minecraft is loaded
        if (mc == null) {
            return;
        }

        // Stop any playing music tracks as a safety measure
        SoundManager soundManager = mc.getSoundManager();
        if (soundManager != null) {
            // Stop all sounds with MUSIC source
            soundManager.stop(null, SoundSource.MUSIC);
        }
    }
}

