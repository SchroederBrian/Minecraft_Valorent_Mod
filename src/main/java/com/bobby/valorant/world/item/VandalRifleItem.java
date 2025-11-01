package com.bobby.valorant.world.item;

import com.bobby.valorant.Config;

public class VandalRifleItem extends GunItem {
    public VandalRifleItem(Properties properties) {
        super(properties);
    }

    @Override
    protected double getDamage() {
        return Config.COMMON.vandalRifleDamage.get();
    }

    @Override
    protected double getRange() {
        return Config.COMMON.vandalRifleRange.get();
    }

    @Override
    protected double getSpreadDegrees() {
        return Config.COMMON.vandalRifleSpreadDegrees.get();
    }

    @Override
    protected int getCooldownTicks() {
        return Config.COMMON.vandalRifleCooldownTicks.get();
    }

    @Override
    protected int getTracerParticles() {
        return Config.COMMON.vandalRifleTracerParticles.get();
    }

    @Override
    protected int getMuzzleParticles() {
        return Config.COMMON.vandalRifleMuzzleParticles.get();
    }

    @Override
    public int getMagazineSize() {
        return Config.COMMON.vandalRifleMagazineSize.get();
    }

    @Override
    public int getMaxReserveAmmo() {
        return Config.COMMON.vandalRifleMaxReserveAmmo.get();
    }

    @Override
    public int getReloadTimeTicks() {
        return Config.COMMON.vandalRifleReloadTimeTicks.get();
    }
}


