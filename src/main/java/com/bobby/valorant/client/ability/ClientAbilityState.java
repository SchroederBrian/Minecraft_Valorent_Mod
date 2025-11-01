package com.bobby.valorant.client.ability;

public final class ClientAbilityState {
    private static volatile int cCharges = 0;
    private static volatile int qCharges = 0;
    private static volatile int eCharges = 0;
    private static volatile int ultPoints = 0;

    private ClientAbilityState() {}

    public static void update(int c, int q, int e, int x) {
        cCharges = Math.max(0, c);
        qCharges = Math.max(0, q);
        eCharges = Math.max(0, e);
        ultPoints = Math.max(0, x);
    }

    public static int cCharges() { return cCharges; }
    public static int qCharges() { return qCharges; }
    public static int eCharges() { return eCharges; }
    public static int ultPoints() { return ultPoints; }
}


