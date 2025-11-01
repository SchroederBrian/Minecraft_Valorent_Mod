package com.bobby.valorant.ability;

public final class AbilitySet {
    private final Ability c;
    private final Ability q;
    private final Ability e;
    private final Ability x;

    public AbilitySet(Ability c, Ability q, Ability e, Ability x) {
        this.c = c;
        this.q = q;
        this.e = e;
        this.x = x;
    }

    public Ability c() { return c; }
    public Ability q() { return q; }
    public Ability e() { return e; }
    public Ability x() { return x; }
}


