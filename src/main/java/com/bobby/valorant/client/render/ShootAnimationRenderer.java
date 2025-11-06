package com.bobby.valorant.client.render;

import com.bobby.valorant.Config;
import com.bobby.valorant.player.GunCooldownStateData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class ShootAnimationRenderer {
    private ShootAnimationRenderer() {}

    public static void applyShootTransform(PoseStack poseStack, float partialTick, InteractionHand hand, ItemStack heldItem) {
        if (!Config.COMMON.shootAnimationEnabled.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Only apply to main hand for now
        if (hand != InteractionHand.MAIN_HAND) return;

        // Check if player has ammo in magazine
        if (com.bobby.valorant.world.item.WeaponAmmoData.getCurrentAmmo(heldItem) <= 0) {
            return; // No ammo, no shoot animation
        }

        // Check if player is on gun cooldown (recently shot)
        if (!GunCooldownStateData.isOnCooldown(mc.player)) {
            return; // No recent shot, no animation
        }

        // Get animation progress (0.0 to 1.0, where 1.0 is fully cooled down)
        int remainingTicks = GunCooldownStateData.getRemainingCooldownTicks(mc.player);
        int totalTicks = 5; // Default cooldown, adjust based on weapon

        // Try to get actual cooldown from weapon
        if (heldItem.getItem() instanceof com.bobby.valorant.world.item.GunItem gun) {
            try {
                var cooldownMethod = com.bobby.valorant.world.item.GunItem.class.getDeclaredMethod("getCooldownTicks");
                cooldownMethod.setAccessible(true);
                totalTicks = (Integer) cooldownMethod.invoke(gun);
            } catch (Exception e) {
                totalTicks = 5; // fallback
            }
        }

        float progress = 1.0f - ((float) remainingTicks / (float) totalTicks);

        // Shoot animation: quick recoil followed by return
        // Phase 1 (0-0.3): Quick recoil back
        // Phase 2 (0.3-1.0): Return to normal position

        float recoilAmount = Config.COMMON.shootRecoilAmount.get().floatValue();

        if (progress < 0.3f) {
            // Recoil phase - move back and slightly up
            float recoilProgress = progress / 0.3f;
            float easedRecoil = easeOutQuad(recoilProgress);

            poseStack.translate(0, -recoilAmount * 0.3f * easedRecoil, recoilAmount * easedRecoil);
            poseStack.mulPose(Axis.XP.rotationDegrees(-recoilAmount * 5f * easedRecoil));
        } else {
            // Return phase - smooth return to normal
            float returnProgress = (progress - 0.3f) / 0.7f;
            float easedReturn = easeOutQuad(returnProgress);

            poseStack.translate(0, -recoilAmount * 0.3f * (1f - easedReturn), recoilAmount * (1f - easedReturn));
            poseStack.mulPose(Axis.XP.rotationDegrees(-recoilAmount * 5f * (1f - easedReturn)));
        }
    }

    private static float easeOutQuad(float t) {
        return 1 - (1 - t) * (1 - t);
    }
}
