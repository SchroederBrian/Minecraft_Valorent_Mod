package com.bobby.valorant.world.item;

import com.bobby.valorant.Config;

public class MollyLauncherItem extends GunItem {
    public MollyLauncherItem(Properties properties) {
        super(properties);
    }

    @Override
    protected double getDamage() {
        return Config.COMMON.mollyLauncherDamage.get();
    }

    @Override
    protected double getRange() {
        return Config.COMMON.mollyLauncherRange.get();
    }

    @Override
    protected double getSpreadDegrees() {
        return Config.COMMON.mollyLauncherSpreadDegrees.get();
    }

    @Override
    protected int getCooldownTicks() {
        return Config.COMMON.mollyLauncherCooldownTicks.get();
    }

    @Override
    protected int getTracerParticles() {
        return Config.COMMON.mollyLauncherTracerParticles.get();
    }

    @Override
    protected int getMuzzleParticles() {
        return Config.COMMON.mollyLauncherMuzzleParticles.get();
    }

    @Override
    public int getMagazineSize() {
        return Config.COMMON.mollyLauncherMagazineSize.get();
    }

    @Override
    public int getMaxReserveAmmo() {
        return Config.COMMON.mollyLauncherMaxReserveAmmo.get();
    }

    @Override
    public int getReloadTimeTicks() {
        return Config.COMMON.mollyLauncherReloadTimeTicks.get();
    }
}


