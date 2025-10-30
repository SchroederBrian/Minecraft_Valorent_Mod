package com.bobby.valorant.server;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.GuiGraphics;

public class TitleMessages {
    private static boolean isActive = false;
    private static String title = "";
    private static String subtitle = "";
    private static int fadeInTicks = 0;
    private static int stayTicks = 0;
    private static int fadeOutTicks = 0;
    private static int titleColor = 0xFFFFFFFF;
    private static int subtitleColor = 0xFFFFFFFF;
    private static int tickCounter = 0;

    // Loading animation support
    private static boolean showProgress = false;
    private static float progress = 0.0f; // 0.0 to 1.0
    private static int progressColor = 0xFFFFD700; // Gold color for progress bar
    private static long progressDurationMs = 0; // Duration in milliseconds for 100% progress
    private static long progressStartTime = 0; // When progress started (System.currentTimeMillis)

    /**
     * Update the progress value (0.0 to 1.0)
     */
    public static void updateProgress(float newProgress) {
        progress = Math.max(0.0f, Math.min(1.0f, newProgress));
    }

    /**
     * Hide the current title overlay immediately
     */
    public static void hide() {
        isActive = false;
        tickCounter = 0;
        System.out.println("[TitleOverlay] Title hidden manually");
    }

    /**
     * Automatically update progress based on elapsed time since start
     */
    private static void updateProgressFromTime() {
        if (!showProgress || progressDurationMs <= 0 || progressStartTime <= 0) return;

        long elapsedMs = System.currentTimeMillis() - progressStartTime;
        float newProgress = Math.min(1.0f, (float) elapsedMs / (float) progressDurationMs);

        // If progress reaches 100%, hide the overlay
        if (newProgress >= 1.0f && progress < 1.0f) {
            System.out.println("[TitleOverlay] Progress complete, hiding overlay");
            isActive = false;
            tickCounter = 0;
        }

        progress = newProgress;
    }

    public static void show(String titleText, String subtitleText,
                           int fadeIn, int stay, int fadeOut,
                           int titleCol, int subtitleCol) {
        show(titleText, subtitleText, fadeIn, stay, fadeOut, titleCol, subtitleCol, false, 0.0f, 0L, null);
    }

    public static void show(String titleText, String subtitleText,
                           int fadeIn, int stay, int fadeOut,
                           int titleCol, int subtitleCol, ItemStack itemStack) {
        show(titleText, subtitleText, fadeIn, stay, fadeOut, titleCol, subtitleCol, false, 0.0f, 0L, itemStack);
    }

    public static void showWithProgress(String titleText, String subtitleText,
                                       int fadeIn, int stay, int fadeOut,
                                       int titleCol, int subtitleCol,
                                       float initialProgress) {
        show(titleText, subtitleText, fadeIn, stay, fadeOut, titleCol, subtitleCol, true, initialProgress, 0L, null);
    }

    public static void showWithProgress(String titleText, String subtitleText,
                                       int fadeIn, int stay, int fadeOut,
                                       int titleCol, int subtitleCol,
                                       float initialProgress, ItemStack itemStack) {
        show(titleText, subtitleText, fadeIn, stay, fadeOut, titleCol, subtitleCol, true, initialProgress, 0L, itemStack);
    }

    public static void showWithProgress(String titleText, String subtitleText,
                                       int fadeIn, int stay, int fadeOut,
                                       int titleCol, int subtitleCol,
                                       float initialProgress, long durationMs, ItemStack itemStack) {
        show(titleText, subtitleText, fadeIn, stay, fadeOut, titleCol, subtitleCol, true, initialProgress, durationMs, itemStack);
    }

    private static void show(String titleText, String subtitleText,
                           int fadeIn, int stay, int fadeOut,
                           int titleCol, int subtitleCol,
                           boolean withProgress, float initialProgress, long durationMs, ItemStack itemStack ) {
        System.out.println("[TitleOverlay] Showing title: '" + titleText + "' subtitle: '" + subtitleText + "'" +
                          (withProgress ? " with progress: " + initialProgress + ", duration: " + durationMs + "ms" : ""));
        title = titleText;
        subtitle = subtitleText;
        fadeInTicks = Math.max(0, fadeIn);
        stayTicks = Math.max(0, stay);
        fadeOutTicks = Math.max(0, fadeOut);
        titleColor = titleCol;
        subtitleColor = subtitleCol;
        showProgress = withProgress;
        progress = Math.max(0.0f, Math.min(1.0f, initialProgress));
        progressDurationMs = durationMs;
        progressStartTime = withProgress && durationMs > 0 ? System.currentTimeMillis() : 0;
        tickCounter = 0;
        isActive = true;

        // Debug logging
        System.out.println("[TitleOverlay] Showing title: " + title + " with duration: " +
                          (fadeInTicks + stayTicks + fadeOutTicks) + " ticks (converted from " +
                          (fadeIn + stay + fadeOut) + " ticks)" +
                          (showProgress ? ", progress duration: " + progressDurationMs + "ms" : ""));
    }

    public static void render(GuiGraphics guiGraphics) {
        if (!isActive) {
            return;
        }

        tickCounter++;

        // Update progress automatically based on elapsed time
        updateProgressFromTime();

        // Debug logging for first few ticks
        if (tickCounter <= 5) {
            System.out.println("[TitleOverlay] Rendering tick " + tickCounter + ", alpha will be calculated");
        }

        // Calculate current alpha (0-255)
        int alpha;
        if (tickCounter <= fadeInTicks) {
            // Fade in phase
            float progress = (float) tickCounter / (float) fadeInTicks;
            alpha = (int) (255 * progress);
        } else if (tickCounter <= fadeInTicks + stayTicks) {
            // Stay phase
            alpha = 255;
        } else if (tickCounter <= fadeInTicks + stayTicks + fadeOutTicks) {
            // Fade out phase
            int fadeProgress = tickCounter - (fadeInTicks + stayTicks);
            float progress = (float) fadeProgress / (float) fadeOutTicks;
            alpha = (int) (255 * (1.0f - progress));
        } else {
            // End
            isActive = false;
            System.out.println("[TitleOverlay] Title finished displaying");
            return;
        }

        // Ensure alpha is within valid range
        alpha = Math.max(0, Math.min(255, alpha));

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Apply alpha to colors
        int titleColWithAlpha = (alpha << 24) | (titleColor & 0xFFFFFF);
        int subtitleColWithAlpha = (alpha << 24) | (subtitleColor & 0xFFFFFF);

        // Draw title (centered, larger)
        int titleY = screenHeight / 2 - 20;
        guiGraphics.drawCenteredString(mc.font, title, screenWidth / 2, titleY, titleColWithAlpha);

        // Draw subtitle (centered, below title)
        int subtitleY = screenHeight / 2 + 10;
        guiGraphics.drawCenteredString(mc.font, subtitle, screenWidth / 2, subtitleY, subtitleColWithAlpha);

        // Draw progress bar if enabled
        if (showProgress) {
            drawProgressBar(guiGraphics, screenWidth, screenHeight, alpha);
        }
    }

    private static void drawProgressBar(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int alpha) {
        // Progress bar dimensions and position
        int barWidth = 200;
        int barHeight = 8;
        int barX = screenWidth / 2 - barWidth / 2;
        int barY = screenHeight / 2 + 40; // Below subtitle

        // Background bar (semi-transparent)
        int bgColor = (alpha / 2 << 24) | 0x333333; // Semi-transparent dark gray
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, bgColor);

        // Progress fill
        int fillWidth = (int) (barWidth * progress);
        int progressColWithAlpha = (alpha << 24) | (progressColor & 0xFFFFFF);
        guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, progressColWithAlpha);

        // Border
        int borderColor = (alpha << 24) | 0xFFFFFF; // White border
        guiGraphics.renderOutline(barX - 1, barY - 1, barWidth + 2, barHeight + 2, borderColor);

        // Progress text
        String progressText = String.format("%.0f%%", progress * 100);
        int textY = barY - 15; // Above progress bar
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, progressText,
                                     screenWidth / 2, textY, progressColWithAlpha);
    }
}
