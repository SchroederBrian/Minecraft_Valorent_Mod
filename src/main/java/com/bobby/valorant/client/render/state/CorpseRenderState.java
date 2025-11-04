package com.bobby.valorant.client.render.state;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/** NeoForge 1.21.x */
public class CorpseRenderState extends HumanoidRenderState {
    /** Blickrichtung beim Tod, normalisiert in Grad [-180, 180). */
    public float deathYawDeg = 0.0F;

    /** Agent-ID für die Textur des Leichnams. */
    public String agentId = "";

    // --- Zusatz: Gliedmaßen-Winkel in Grad ---
    public float headX, headY, headZ;
    public float rArmY, rArmZ;
    public float lArmY, lArmZ;
    public float rLegY, rLegZ;
    public float lLegY, lLegZ;

    public CorpseRenderState() {
        super();
        // Neutralisieren, damit nur unsere Matrix-Rotationen wirken.
        this.yRot    = 0.0F;
        this.xRot    = 0.0F;
        this.bodyRot = 0.0F;

        this.deathTime     = 0.0F;
        this.hasRedOverlay = false;
        this.isUpsideDown  = false;
    }
}