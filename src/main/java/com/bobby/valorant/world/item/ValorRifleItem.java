package com.bobby.valorant.world.item;

import com.bobby.valorant.Config;

public class ValorRifleItem extends GunItem {
    public ValorRifleItem(Properties properties) {
        super(properties);
    }

    @Override
    protected double getDamage() {
        return Config.COMMON.valorRifleDamage.get();
    }

    @Override
    protected double getRange() {
        return Config.COMMON.valorRifleRange.get();
    }

    @Override
    protected double getSpreadDegrees() {
        return Config.COMMON.valorRifleSpreadDegrees.get();
    }

    @Override
    protected int getCooldownTicks() {
        return Config.COMMON.valorRifleCooldownTicks.get();
    }

    @Override
    protected int getTracerParticles() {
        return Config.COMMON.valorRifleTracerParticles.get();
    }

    @Override
    protected int getMuzzleParticles() {
        return Config.COMMON.valorRifleMuzzleParticles.get();
    }

    @Override
    public int getMagazineSize() {
        return Config.COMMON.valorRifleMagazineSize.get();
    }

    @Override
    public int getMaxReserveAmmo() {
        return Config.COMMON.valorRifleMaxReserveAmmo.get();
    }
}


