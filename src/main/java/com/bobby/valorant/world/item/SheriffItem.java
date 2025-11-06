package com.bobby.valorant.world.item;

import com.bobby.valorant.Config;

public class SheriffItem extends GunItem {
    public SheriffItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isAutomatic() {
        return Config.COMMON.sheriffAutomatic.get();
    }

    @Override
    public double getRecoilPitchPerShot() {
        return Config.COMMON.sheriffRecoilPitchDegrees.get();
    }

    @Override
    public double getAutomaticFireRateShotsPerSecond() {
        return Config.COMMON.sheriffAutomaticFireRateShotsPerSecond.get();
    }

    @Override
    protected double getDamage() {
        return Config.COMMON.sheriffDamage.get();
    }

    @Override
    protected double getRange() {
        return Config.COMMON.sheriffRange.get();
    }

    @Override
    protected double getSpreadDegrees() {
        return Config.COMMON.sheriffSpreadDegrees.get();
    }

    @Override
    public double getFireRateShotsPerSecond() {
        return Config.COMMON.sheriffFireRateShotsPerSecond.get();
    }

    @Override
    protected int getTracerParticles() {
        return Config.COMMON.sheriffTracerParticles.get();
    }

    @Override
    protected int getMuzzleParticles() {
        return Config.COMMON.sheriffMuzzleParticles.get();
    }

    @Override
    public int getMagazineSize() {
        return Config.COMMON.sheriffMagazineSize.get();
    }

    @Override
    public int getMaxReserveAmmo() {
        return Config.COMMON.sheriffMaxReserveAmmo.get();
    }

    @Override
    public int getReloadTimeTicks() {
        return Config.COMMON.sheriffReloadTimeTicks.get();
    }
}


