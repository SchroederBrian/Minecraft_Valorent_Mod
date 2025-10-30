package com.bobby.valorant.round;

/**
 * Lightweight client-side round state used purely for HUD rendering.
 * The server should sync this via SyncRoundStatePacket.
 */
public final class RoundState {
    private RoundState() {}

    private static volatile boolean running = false;
    private static volatile int remainingSeconds = 0;
    private static volatile int leftScore = 0;
    private static volatile int rightScore = 0;
    private static volatile int phaseOrdinal = 0; // 0=IDLE,1=BUY,2=ROUND,3=PLANTED,4=POST

    public static void update(boolean isRunning, int secondsRemaining, int left, int right) {
        running = isRunning;
        remainingSeconds = Math.max(0, secondsRemaining);
        leftScore = Math.max(0, left);
        rightScore = Math.max(0, right);
    }

    public static void updatePhase(int ordinal) {
        // Reset spike defused flag when starting a new round (BUY phase = 1)
        if (ordinal == 1 && phaseOrdinal != 1) { // Phase changed to BUY
            com.bobby.valorant.events.SpikeClientEvents.resetSpikeDefusedFlag();
        }
        phaseOrdinal = ordinal;
    }

    public static boolean isRunning() {
        return running;
    }

    public static int getRemainingSeconds() {
        return remainingSeconds;
    }

    public static int getLeftScore() {
        return leftScore;
    }

    public static int getRightScore() {
        return rightScore;
    }

    public static String formattedTime() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static boolean isBuyPhase() { return phaseOrdinal == 1; }
}


