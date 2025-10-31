package com.bobby.valorant.world.item;

import com.bobby.valorant.Config;

public class GhostPistolItem extends GunItem {
    public GhostPistolItem(Properties properties) {
        super(properties);
    }

    @Override
    protected double getDamage() {
        return Config.COMMON.ghostDamage.get();
    }

    @Override
    protected double getRange() {
        return Config.COMMON.ghostRange.get();
    }

    @Override
    protected double getSpreadDegrees() {
        return Config.COMMON.ghostSpreadDegrees.get();
    }

    @Override
    protected int getCooldownTicks() {
        return Config.COMMON.ghostCooldownTicks.get();
    }

    @Override
    protected int getTracerParticles() {
        return Config.COMMON.ghostTracerParticles.get();
    }

    @Override
    protected int getMuzzleParticles() {
        return Config.COMMON.ghostMuzzleParticles.get();
    }

    @Override
    public int getMagazineSize() {
        return Config.COMMON.ghostMagazineSize.get();
    }

    @Override
    public int getMaxReserveAmmo() {
        return Config.COMMON.ghostMaxReserveAmmo.get();
    }
}


