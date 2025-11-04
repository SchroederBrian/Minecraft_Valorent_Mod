        package com.bobby.valorant.world.item;

        import com.bobby.valorant.Config;

        public class FrenzyPistolItem extends GunItem {
            public FrenzyPistolItem(Properties properties) {
                super(properties);
            }

            @Override
            protected double getDamage() {
                return Config.COMMON.frenzyDamage.get();
            }

            @Override
            protected double getRange() {
                return Config.COMMON.frenzyRange.get();
            }

            @Override
            protected double getSpreadDegrees() {
                return Config.COMMON.frenzySpreadDegrees.get();
            }

            @Override
            protected int getCooldownTicks() {
                return Config.COMMON.frenzyCooldownTicks.get();
            }

            @Override
            protected int getTracerParticles() {
            return Config.COMMON.frenzyTracerParticles.get();
            }

            @Override
            protected int getMuzzleParticles() {
                return Config.COMMON.frenzyMuzzleParticles.get();
            }

            @Override
            public int getMagazineSize() {
                return Config.COMMON.frenzyMagazineSize.get();
            }

            @Override
            public int getMaxReserveAmmo() {
                return Config.COMMON.frenzyMaxReserveAmmo.get();
            }

            @Override
            public int getReloadTimeTicks() {
                return Config.COMMON.frenzyReloadTimeTicks.get();
            }
        }
