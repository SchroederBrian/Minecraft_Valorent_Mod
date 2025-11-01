package com.bobby.valorant.client.render;

import com.bobby.valorant.Config;
import com.bobby.valorant.player.ReloadStateData;
import com.bobby.valorant.world.item.ClassicPistolItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class ReloadAnimationRenderer {
    private static final Minecraft MC = Minecraft.getInstance();

    private ReloadAnimationRenderer() {}

    /**
     * Berechnet den aktuellen Animation-Fortschritt (0.0 bis 1.0)
     */
    public static float getAnimationProgress(Player player) {
        if (!ReloadStateData.isReloading(player)) {
            return 0.0f;
        }
        return ReloadStateData.getReloadProgress(player);
    }

    /**
     * Easing-Funktion für natürliche Bewegungen (ease-in-out cubic)
     */
    private static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }

    /**
     * Easing-Funktion für schnelle Bewegungen (ease-out quad)
     */
    private static float easeOutQuad(float t) {
        return 1 - (1 - t) * (1 - t);
    }

    /**
     * Erweiterte 6-Phasen-Reload-Animation für die rechte Hand (Haupthand)
     */
    public static void applyRightHandTransform(PoseStack poseStack, float partialTick) {
        if (!Config.COMMON.reloadAnimationEnabled.get()) {
            return;
        }

        Player player = MC.player;
        if (player == null) return;

        float progress = getAnimationProgress(player);
        if (progress <= 0.0f) return;

        float swing = Config.COMMON.reloadHandSwingAmount.get().floatValue();
        float rackAmount = Config.COMMON.reloadRackSlideAmount.get().floatValue();

        // 6-Phasen Animation:
        // 0.00-0.15: Prep - Hände positionieren sich
        // 0.15-0.35: Eject - Magazin auswerfen
        // 0.35-0.50: Hold - Position halten
        // 0.50-0.75: Insert - Neues Magazin einführen
        // 0.75-0.90: Rack - Schlitten zurückziehen
        // 0.90-1.00: Finish - Abschluss

        if (progress < 0.15f) {
            // Phase 1: Prep - Waffe leicht zurückziehen
            float phaseProgress = easeInOutCubic(progress / 0.15f);
            poseStack.translate(0.0f, -swing * phaseProgress * 0.2f, -swing * phaseProgress * 0.3f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-8.0f * phaseProgress));
            poseStack.mulPose(Axis.ZP.rotationDegrees(5.0f * phaseProgress));

        } else if (progress < 0.35f) {
            // Phase 2: Eject - Waffe weiter zurück und nach rechts neigen für Magazin-Ejektion
            float phaseProgress = easeInOutCubic((progress - 0.15f) / 0.2f);
            poseStack.translate(swing * phaseProgress * 0.1f, -swing * (0.2f + phaseProgress * 0.3f), -swing * (0.3f + phaseProgress * 0.4f));
            poseStack.mulPose(Axis.XP.rotationDegrees(-8.0f - 12.0f * phaseProgress));
            poseStack.mulPose(Axis.ZP.rotationDegrees(5.0f + 15.0f * phaseProgress));

        } else if (progress < 0.50f) {
            // Phase 3: Hold - Position halten während Magazin gewechselt wird
            poseStack.translate(swing * 0.1f, -swing * 0.5f, -swing * 0.7f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-20.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(20.0f));

        } else if (progress < 0.75f) {
            // Phase 4: Insert - Waffe leicht nach links neigen für Magazin-Einführung
            float phaseProgress = easeInOutCubic((progress - 0.50f) / 0.25f);
            float reverseProgress = 1.0f - phaseProgress;
            poseStack.translate(swing * 0.1f * reverseProgress, -swing * (0.5f - phaseProgress * 0.1f), -swing * (0.7f - phaseProgress * 0.2f));
            poseStack.mulPose(Axis.XP.rotationDegrees(-20.0f + phaseProgress * 8.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(20.0f - phaseProgress * 25.0f));

        } else if (progress < 0.90f) {
            // Phase 5: Rack - Schlitten zurückziehen (schnelle Bewegung)
            float phaseProgress = easeOutQuad((progress - 0.75f) / 0.15f);
            poseStack.translate(-rackAmount * phaseProgress, -swing * 0.4f, -swing * 0.5f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-12.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-5.0f));

        } else {
            // Phase 6: Finish - Zurück zur Normalposition
            float phaseProgress = easeInOutCubic((progress - 0.90f) / 0.10f);
            float reverseProgress = 1.0f - phaseProgress;
            poseStack.translate(-rackAmount * reverseProgress, -swing * 0.4f * reverseProgress, -swing * 0.5f * reverseProgress);
            poseStack.mulPose(Axis.XP.rotationDegrees(-12.0f * reverseProgress));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-5.0f * reverseProgress));
        }
    }

    /**
     * Erweiterte linke Hand Animation mit präziser Magazin-Interaktion
     */
    public static void applyLeftHandTransform(PoseStack poseStack, float partialTick) {
        if (!Config.COMMON.reloadAnimationEnabled.get()) {
            return;
        }

        Player player = MC.player;
        if (player == null) return;

        float progress = getAnimationProgress(player);
        if (progress <= 0.0f) return;

        float movement = Config.COMMON.reloadLeftHandMovement.get().floatValue();

        // Linke Hand Animation mit präziser Magazin-Interaktion:
        // 0.00-0.15: Prep - Hand positioniert sich
        // 0.15-0.35: Eject - Hand greift Magazin und zieht es heraus
        // 0.35-0.50: Hold - Hand hält altes Magazin
        // 0.50-0.75: Insert - Hand führt neues Magazin ein
        // 0.75-0.90: Release - Hand lässt los
        // 0.90-1.00: Finish - Hand geht zurück

        if (progress < 0.15f) {
            // Phase 1: Prep - Hand bewegt sich zur Waffe
            float phaseProgress = easeInOutCubic(progress / 0.15f);
            poseStack.translate(movement * phaseProgress * 0.3f, -movement * phaseProgress * 0.1f, movement * phaseProgress * 0.4f);
            poseStack.mulPose(Axis.YP.rotationDegrees(15.0f * phaseProgress));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0f * phaseProgress));

        } else if (progress < 0.35f) {
            // Phase 2: Eject - Hand greift Magazin und zieht es heraus
            float phaseProgress = easeInOutCubic((progress - 0.15f) / 0.2f);
            poseStack.translate(movement * (0.3f + phaseProgress * 0.4f), -movement * (0.1f + phaseProgress * 0.2f), movement * (0.4f + phaseProgress * 0.3f));
            poseStack.mulPose(Axis.YP.rotationDegrees(15.0f + 35.0f * phaseProgress));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0f - 15.0f * phaseProgress));
            poseStack.mulPose(Axis.XP.rotationDegrees(20.0f * phaseProgress));

        } else if (progress < 0.50f) {
            // Phase 3: Hold - Hand hält das ausgeworfene Magazin
            poseStack.translate(movement * 0.7f, -movement * 0.3f, movement * 0.7f);
            poseStack.mulPose(Axis.YP.rotationDegrees(50.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-25.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(20.0f));

        } else if (progress < 0.75f) {
            // Phase 4: Insert - Hand führt neues Magazin ein
            float phaseProgress = easeInOutCubic((progress - 0.50f) / 0.25f);
            poseStack.translate(movement * (0.7f - phaseProgress * 0.4f), -movement * (0.3f - phaseProgress * 0.1f), movement * (0.7f - phaseProgress * 0.3f));
            poseStack.mulPose(Axis.YP.rotationDegrees(50.0f - phaseProgress * 25.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-25.0f + phaseProgress * 15.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(20.0f - phaseProgress * 15.0f));

        } else if (progress < 0.90f) {
            // Phase 5: Release - Hand lässt los
            float phaseProgress = easeOutQuad((progress - 0.75f) / 0.15f);
            poseStack.translate(movement * 0.3f * (1.0f - phaseProgress), -movement * 0.2f * (1.0f - phaseProgress), movement * 0.4f * (1.0f - phaseProgress));
            poseStack.mulPose(Axis.YP.rotationDegrees(25.0f * (1.0f - phaseProgress)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0f * (1.0f - phaseProgress)));
            poseStack.mulPose(Axis.XP.rotationDegrees(5.0f * (1.0f - phaseProgress)));

        } else {
            // Phase 6: Finish - Hand geht zurück in Ruheposition
            float phaseProgress = easeInOutCubic((progress - 0.90f) / 0.10f);
            float reverseProgress = 1.0f - phaseProgress;
            poseStack.translate(movement * 0.3f * reverseProgress, -movement * 0.2f * reverseProgress, movement * 0.4f * reverseProgress);
            poseStack.mulPose(Axis.YP.rotationDegrees(25.0f * reverseProgress));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0f * reverseProgress));
            poseStack.mulPose(Axis.XP.rotationDegrees(5.0f * reverseProgress));
        }
    }

    /**
     * Rendert das Magazin in der linken Hand mit verschiedenen Zuständen
     */
    public static void renderMagazineInLeftHand(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        if (!Config.COMMON.reloadAnimationEnabled.get()) {
            return;
        }

        Player player = MC.player;
        if (player == null) return;

        float progress = getAnimationProgress(player);
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof ClassicPistolItem)) return;

        ItemRenderer itemRenderer = MC.getItemRenderer();
        float scale = Config.COMMON.reloadMagazineScale.get().floatValue();

        // Render verschiedene Magazine basierend auf Phase
        if (progress >= 0.15f && progress < 0.50f) {
            // Altes Magazin auswerfen (Phase 2-3)
            renderEjectedMagazine(poseStack, bufferSource, packedLight, itemRenderer, scale, progress);

        } else if (progress >= 0.50f && progress < 0.75f) {
            // Neues Magazin einführen (Phase 4)
            renderNewMagazine(poseStack, bufferSource, packedLight, itemRenderer, scale, progress);
        }
    }

    /**
     * Rendert das ausgeworfene (alte) Magazin
     */
    private static void renderEjectedMagazine(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                           ItemRenderer itemRenderer, float scale, float progress) {
        if (!Config.COMMON.reloadShowEjectedMagazine.get()) return;

        Player player = MC.player;
        if (player == null) return;

        ItemStack magazineStack = new ItemStack(player.getMainHandItem().getItem()); // Kopie für altes Magazin

        poseStack.pushPose();

        // Position basierend auf Fortschritt - Magazin fliegt aus
        float ejectProgress = (progress - 0.15f) / 0.35f;
        float offset = Config.COMMON.reloadEjectedMagazineOffset.get().floatValue();

        poseStack.translate(
            0.2f + offset * ejectProgress * 0.5f,
            -0.1f - offset * ejectProgress * 0.3f,
            0.4f + offset * ejectProgress
        );

        poseStack.scale(scale * 0.9f, scale * 0.9f, scale * 0.9f);

        // Rotation während des Flugs
        poseStack.mulPose(Axis.XP.rotationDegrees(30.0f + ejectProgress * 45.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(ejectProgress * 90.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(ejectProgress * 30.0f));

        itemRenderer.renderStatic(magazineStack, ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                                 packedLight, packedLight, poseStack, bufferSource, MC.level, 0);

        poseStack.popPose();
    }

    /**
     * Rendert das neue Magazin während der Einführung
     */
    private static void renderNewMagazine(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                        ItemRenderer itemRenderer, float scale, float progress) {
        Player player = MC.player;
        if (player == null) return;

        ItemStack magazineStack = new ItemStack(player.getMainHandItem().getItem()); // Neues Magazin

        poseStack.pushPose();

        // Position für Einführung
        float insertProgress = (progress - 0.50f) / 0.25f;

        poseStack.translate(
            0.15f - insertProgress * 0.05f,
            -0.15f + insertProgress * 0.03f,
            0.35f - insertProgress * 0.1f
        );

        poseStack.scale(scale, scale, scale);

        // Rotation während der Einführung
        poseStack.mulPose(Axis.XP.rotationDegrees(15.0f - insertProgress * 10.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(-5.0f + insertProgress * 5.0f));

        itemRenderer.renderStatic(magazineStack, ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                                 packedLight, packedLight, poseStack, bufferSource, MC.level, 0);

        poseStack.popPose();
    }
}
