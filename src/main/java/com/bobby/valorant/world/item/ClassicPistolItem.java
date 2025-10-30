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
    protected int getCooldownTicks() {
        return Config.COMMON.classicCooldownTicks.get();
    }

    @Override
    protected int getTracerParticles() {
        return Config.COMMON.classicTracerParticles.get();
    }

    @Override
    protected int getMuzzleParticles() {
        return Config.COMMON.classicMuzzleParticles.get();
    }
}
