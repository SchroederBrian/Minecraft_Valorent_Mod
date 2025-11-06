package com.bobby.valorant.client.render;

import com.bobby.valorant.Config;
import com.bobby.valorant.player.KnifeAnimationStateData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Messer-Animationen mit expliziten Rotationswinkeln r_x, r_y, r_z (in GRAD).
 * - LIGHT_ATTACK: diagonaler Cut (oben rechts -> unten links)
 * - HEAVY_ATTACK: Stab (oben -> unten)
 *
 * translate: +x=rechts, +y=runter, +z=zur Kamera (negativ = zur Kamera)
 * Rotationen: r_x(Pitch), r_y(Yaw), r_z(Roll) — angewendet in Reihenfolge X -> Y -> Z
 */
public final class KnifeAnimationRenderer {
    private static final Minecraft MC = Minecraft.getInstance();

    private KnifeAnimationRenderer() {}

    /* ------------------------------ Utilities -------------------------------- */

    private static float clamp01(float t) {
        if (t < 0f) return 0f;
        if (t > 1f) return 1f;
        return t;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float smoothstep(float t) { // weichere Kurve
        t = clamp01(t);
        return t * t * (3f - 2f * t);
    }

    private static float easeInOutCubic(float t) {
        t = clamp01(t);
        return (t < 0.5f) ? (4f * t * t * t) : (1f - (float) Math.pow(-2f * t + 2f, 3) / 2f);
    }

    private static float easeOutQuad(float t) {
        t = clamp01(t);
        return 1f - (1f - t) * (1f - t);
    }

    private static float easeOutExpo(float t) {
        t = clamp01(t);
        if (t >= 1f) return 1f;
        return 1f - (float) Math.pow(2f, -10f * t);
    }

    /** Progress aus deinem State holen und sicher klemmen */
    public static float getAnimationProgress(Player player) {
        if (!KnifeAnimationStateData.isAnimating(player)) return 0f;
        return clamp01(KnifeAnimationStateData.getAnimationProgress(player));
    }

    /** Quadratische Bezier für translationalen Swing */
    private static float bezier2(float p0, float p1, float p2, float t) {
        float u = 1f - t;
        return (u * u * p0) + (2f * u * t * p1) + (t * t * p2);
    }

    /** Rotationen in GRAD anwenden; Reihenfolge X -> Y -> Z */
    private static void applyRotationDegrees(PoseStack stack, float r_x, float r_y, float r_z) {
        stack.mulPose(Axis.XP.rotationDegrees(r_x));
        stack.mulPose(Axis.YP.rotationDegrees(r_y));
        stack.mulPose(Axis.ZP.rotationDegrees(r_z));
    }

    /* -------------------------- LIGHT (Cut) ---------------------------------- */

    /**
     * Linksklick: "Cut" (TopRight -> BottomLeft)
     * Phasen:
     * 0.00–0.16  Windup
     * 0.16–0.58  Swing (Bezier-Bogen)
     * 0.58–0.72  Follow-Through (Overshoot)
     * 0.72–1.00  Return
     */
    public static void applyLightAttackTransform(PoseStack poseStack, float partialTick) {
        Player player = MC.player;
        if (player == null) return;
        if (!Config.COMMON.knifeAnimationEnabled.get()) return;

        if (!KnifeAnimationStateData.isAnimating(player) ||
            KnifeAnimationStateData.getCurrentAnimationType(player) != KnifeAnimationStateData.AnimationType.LIGHT_ATTACK) {
            return;
        }

        final float progress = getAnimationProgress(player);
        if (progress <= 0f) return;

        // Zeiten
        final float T_WINDUP = 0.16f;
        final float T_SWING  = 0.42f; // 0.16..0.58
        final float T_FOLLOW = 0.14f; // 0.58..0.72
        final float T_RETURN = 0.28f; // 0.72..1.00

        // Windup (oben rechts)
        final float W_TX = +0.22f, W_TY = -0.22f, W_TZ = -0.10f;
        final float W_RX = -28f,   W_RY = +34f,  W_RZ = +34f;

        // Swing-Ende (unten links)
        final float S_TX = -0.36f, S_TY = +0.26f, S_TZ = -0.06f;
        final float S_RX = +14f,   S_RY = -23f,   S_RZ = -16f;

        // Bogenkontrolle (relativ zum Windup)
        final float C_TX = -0.05f, C_TY = +0.06f;

        // Follow-Through Overshoot
        final float F_OV_TX = -0.04f, F_OV_TY = +0.035f, F_OV_R = 6f;

        if (progress < T_WINDUP) {
            float t = easeOutExpo(progress / T_WINDUP);
            float r_x = W_RX * t, r_y = W_RY * t, r_z = W_RZ * t;
            poseStack.translate(W_TX * t, W_TY * t, W_TZ * t);
            applyRotationDegrees(poseStack, r_x, r_y, r_z);
            return;
        }

        if (progress < T_WINDUP + T_SWING) {
            float local = (progress - T_WINDUP) / T_SWING;
            float fast = easeOutQuad(Math.min(local * 1.15f, 1f));
            float soft = smoothstep(local);
            float b = 0.6f * fast + 0.4f * soft;

            // Position (Bezier)
            float t_x = bezier2(W_TX, W_TX + C_TX, S_TX, b);
            float t_y = bezier2(W_TY, W_TY + C_TY, S_TY, b);
            float t_z = lerp(W_TZ, S_TZ, b);

            // Rotation (linear)
            float r_x = lerp(W_RX, S_RX, b);
            float r_y = lerp(W_RY, S_RY, b);
            float r_z = lerp(W_RZ, S_RZ, b);

            poseStack.translate(t_x, t_y, t_z);
            applyRotationDegrees(poseStack, r_x, r_y, r_z);
            return;
        }

        if (progress < T_WINDUP + T_SWING + T_FOLLOW) {
            float local = (progress - (T_WINDUP + T_SWING)) / T_FOLLOW;
            float t = easeOutQuad(local);

            float t_x = S_TX + F_OV_TX * t;
            float t_y = S_TY + F_OV_TY * t;
            float t_z = S_TZ;

            float r_x = S_RX + (+F_OV_R) * t;
            float r_y = S_RY + (-F_OV_R) * t;
            float r_z = S_RZ + (-F_OV_R) * t;

            poseStack.translate(t_x, t_y, t_z);
            applyRotationDegrees(poseStack, r_x, r_y, r_z);
            return;
        }

        // Return
        {
            float local = (progress - (T_WINDUP + T_SWING + T_FOLLOW)) / T_RETURN;
            float t = easeInOutCubic(local);

            float t_x = lerp(S_TX, 0f, t);
            float t_y = lerp(S_TY, 0f, t);
            float t_z = lerp(S_TZ, 0f, t);

            float r_x = lerp(S_RX, 0f, t);
            float r_y = lerp(S_RY, 0f, t);
            float r_z = lerp(S_RZ, 0f, t);

            poseStack.translate(t_x, t_y, t_z);
            applyRotationDegrees(poseStack, r_x, r_y, r_z);
        }
    }

    /* -------------------------- HEAVY (Stab) -------------------------------- */
    /**
     * Rechtsklick: "Valorant Heavy Stab" (Pullback -> Forward Thrust).
     * Phasen:
     * 0.00–0.14  Pullback/Chamber (Messer zurück zur Seite ziehen)
     * 0.14–0.32  Thrust (explosive Vorwärtsbewegung)
     * 0.32–0.42  Impact Hold (kurzer Halt bei voller Extension)
     * 0.42–1.00  Recovery (Rückkehr zu neutral)
     */
    public static void applyHeavyAttackTransform(PoseStack poseStack, float partialTick) {
        Player player = MC.player;
        if (player == null) return;
        if (!Config.COMMON.knifeAnimationEnabled.get()) return;

        if (!KnifeAnimationStateData.isAnimating(player) ||
            KnifeAnimationStateData.getCurrentAnimationType(player) != KnifeAnimationStateData.AnimationType.HEAVY_ATTACK) {
            return;
        }

        final float progress = getAnimationProgress(player);
        if (progress <= 0f) return;

        final float T_PULLBACK = 0.14f;
        final float T_THRUST   = 0.18f; // 0.14..0.32
        final float T_HOLD     = 0.10f; // 0.32..0.42
        final float T_RECOVERY = 0.58f; // 0.42..1.00

        // Pullback Position (rechts hinten, leicht erhöht - "Chambering")
        final float P_TX = +0.38f,  P_TY = -0.14f, P_TZ = +0.18f;
        final float P_RX = -8f,     P_RY = +52f,   P_RZ = +18f;

        // Thrust Position (zentral vorwärts, volle Extension)
        final float T_TX = -0.05f,  T_TY = +0.08f, T_TZ = -0.62f;
        final float T_RX = +4f,     T_RY = -12f,   T_RZ = -8f;

        // Impact overshoot für das "Einstechen" Gefühl
        final float IMPACT_PUSH = 0.08f;
        final float IMPACT_SHAKE = 0.015f;

        // === PULLBACK PHASE ===
        if (progress < T_PULLBACK) {
            float local = progress / T_PULLBACK;
            float t = easeOutExpo(local);

            float t_x = P_TX * t;
            float t_y = P_TY * t;
            float t_z = P_TZ * t;

            float r_x = P_RX * t;
            float r_y = P_RY * t;
            float r_z = P_RZ * t;

            poseStack.translate(t_x, t_y, t_z);
            applyRotationDegrees(poseStack, r_x, r_y, r_z);
            return;
        }

        // === THRUST PHASE ===
        if (progress < T_PULLBACK + T_THRUST) {
            float local = (progress - T_PULLBACK) / T_THRUST;
            // Explosive start, dann smooth out
            float t = (local < 0.3f) ? 
                     (local / 0.3f) * 0.5f : 
                     0.5f + easeOutQuad((local - 0.3f) / 0.7f) * 0.5f;

            float t_x = lerp(P_TX, T_TX, t);
            float t_y = lerp(P_TY, T_TY, t);
            float t_z = lerp(P_TZ, T_TZ, t);

            float r_x = lerp(P_RX, T_RX, t);
            float r_y = lerp(P_RY, T_RY, t);
            float r_z = lerp(P_RZ, T_RZ, t);

            poseStack.translate(t_x, t_y, t_z);
            applyRotationDegrees(poseStack, r_x, r_y, r_z);
            return;
        }

        // === IMPACT HOLD PHASE ===
        if (progress < T_PULLBACK + T_THRUST + T_HOLD) {
            float local = (progress - (T_PULLBACK + T_THRUST)) / T_HOLD;
            
            // Overshoot am Anfang, dann settle
            float overshoot = (local < 0.4f) ? 
                             (1f - local / 0.4f) * IMPACT_PUSH : 
                             0f;
            
            // Micro-shake für Impact-Gefühl
            float shake = (float) (Math.sin(local * 40f) * IMPACT_SHAKE * (1f - local));

            float t_x = T_TX + shake;
            float t_y = T_TY;
            float t_z = T_TZ - overshoot;

            float r_x = T_RX + shake * 20f;
            float r_y = T_RY;
            float r_z = T_RZ;

            poseStack.translate(t_x, t_y, t_z);
            applyRotationDegrees(poseStack, r_x, r_y, r_z);
            return;
        }

        // === RECOVERY PHASE ===
        {
            float local = (progress - (T_PULLBACK + T_THRUST + T_HOLD)) / T_RECOVERY;
            float t = easeInOutCubic(local);

            float t_x = lerp(T_TX, 0f, t);
            float t_y = lerp(T_TY, 0f, t);
            float t_z = lerp(T_TZ, 0f, t);

            float r_x = lerp(T_RX, 0f, t);
            float r_y = lerp(T_RY, 0f, t);
            float r_z = lerp(T_RZ, 0f, t);

            poseStack.translate(t_x, t_y, t_z);
            applyRotationDegrees(poseStack, r_x, r_y, r_z);
        }
    }

    /* -------------------------- Dispatcher ---------------------------------- */

    public static void applyKnifeAnimationTransform(PoseStack poseStack, float partialTick) {
        Player player = MC.player;
        if (player == null) return;

        switch (KnifeAnimationStateData.getCurrentAnimationType(player)) {
            case LIGHT_ATTACK -> applyLightAttackTransform(poseStack, partialTick);
            case HEAVY_ATTACK -> applyHeavyAttackTransform(poseStack, partialTick);
            default -> { /* keine Messer-Animation aktiv */ }
        }
    }
}
