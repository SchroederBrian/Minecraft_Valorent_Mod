package com.bobby.valorant.world.item;

import com.bobby.valorant.Config;

public class ClassicPistolItem extends GunItem {
    public ClassicPistolItem(Properties properties) {
        super(properties);
    }

    @Override
    protected double getDamage() {
        return Config.COMMON.classicDamage.get();
    }

    @Override
    protected double getRange() {
        return Config.COMMON.classicRange.get();
    }

    @Override
    protected double getSpreadDegrees() {
        return Config.COMMON.classicSpreadDegrees.get();
    }

    @Override
    public double getFireRateShotsPerSecond() {
        return Config.COMMON.classicFireRateShotsPerSecond.get();
    }

    @Override
    protected int getTracerParticles() {
        return Config.COMMON.classicTracerParticles.get();
    }

    @Override
    protected int getMuzzleParticles() {
        return Config.COMMON.classicMuzzleParticles.get();
    }

    @Override
    public int getMagazineSize() {
        return Config.COMMON.classicMagazineSize.get();
    }

    @Override
    public int getMaxReserveAmmo() {
        return Config.COMMON.classicMaxReserveAmmo.get();
    }

    @Override
    public int getReloadTimeTicks() {
        return Config.COMMON.classicReloadTimeTicks.get();
    }

    @Override
    public boolean isAutomatic() {
        return Config.COMMON.classicAutomatic.get();
    }

    @Override
    public double getRecoilPitchPerShot() {
        return Config.COMMON.classicRecoilPitchDegrees.get();
    }

    @Override
    public double getAutomaticFireRateShotsPerSecond() {
        return Config.COMMON.classicAutomaticFireRateShotsPerSecond.get();
    }
}
