package com.bobby.valorant.world.item;

import com.bobby.valorant.Config;

public class KnifeItem extends MeleeWeapon {
    public KnifeItem(Properties properties) {
        super(properties);
    }

    @Override
    protected double getLightAttackDamage() {
        return Config.COMMON.knifeLightAttackDamage.get();
    }

    @Override
    protected double getHeavyAttackDamage() {
        return Config.COMMON.knifeHeavyAttackDamage.get();
    }

    @Override
    protected double getRange() {
        return Config.COMMON.knifeRange.get();
    }

    @Override
    protected int getLightAttackCooldownTicks() {
        return Config.COMMON.knifeLightAttackCooldownTicks.get();
    }

    @Override
    protected int getHeavyAttackCooldownTicks() {
        return Config.COMMON.knifeHeavyAttackCooldownTicks.get();
    }

    // IWeapon interface methods - knives don't have magazines/reload
    @Override
    public int getMagazineSize() {
        return 1; // Not applicable for knives
    }

    @Override
    public int getMaxReserveAmmo() {
        return 0; // Not applicable for knives
    }

    @Override
    public int getReloadTimeTicks() {
        return 0; // Not applicable for knives
    }
}


