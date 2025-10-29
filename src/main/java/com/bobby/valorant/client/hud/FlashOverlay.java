package com.bobby.valorant.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class FlashOverlay {
    private static boolean isFlashing = false;
    private static int windupTicks = 0;
    private static int fullFlashTicks = 0;
    private static int fadeTicks = 0;
    private static int maxFadeTicks = 0;

    public static void triggerFlash(int windupDurationTicks, int fullDurationTicks, int fadeDurationTicks) {
        isFlashing = true;
        windupTicks = Math.max(0, windupDurationTicks);
        fullFlashTicks = Math.max(0, fullDurationTicks);
        fadeTicks = Math.max(0, fadeDurationTicks);
        maxFadeTicks = fadeTicks;
    }

    // Backward compatibility overload
    public static void triggerFlash(int fullDurationTicks, int fadeDurationTicks) {
        triggerFlash(0, fullDurationTicks, fadeDurationTicks);
    }

    public static void render(GuiGraphics guiGraphics) {
        if (!isFlashing) {
            return;
        }

        if (windupTicks > 0) {
            windupTicks--;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int alpha;

        if (fullFlashTicks > 0) {
            alpha = 255;
            fullFlashTicks--;
        } else if (fadeTicks > 0) {
            float progress = (float) fadeTicks / (float) maxFadeTicks;
            alpha = (int) (255 * progress);
            alpha = Math.max(0, Math.min(255, alpha));
            fadeTicks--;
        } else {
            isFlashing = false;
            return;
        }

        int color = (alpha << 24) | 0xFFFFFF; // White color with calculated alpha

        guiGraphics.fill(0, 0, screenWidth, screenHeight, color);

        if (windupTicks <= 0 && fullFlashTicks <= 0 && fadeTicks <= 0) {
            isFlashing = false;
        }
    }
}
