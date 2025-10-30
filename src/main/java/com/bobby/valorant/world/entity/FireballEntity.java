package com.bobby.valorant.world.entity;

import com.bobby.valorant.Config;
import com.bobby.valorant.registry.ModEntityTypes;
import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.util.ParticleScheduler;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class FireballEntity extends ThrowableItemProjectile {
    private boolean mollyActive;
    private int mollyTicksRemaining;
    private float mollyRadius;
    private int mollyTickInterval;
    private int mollyTickCounter;
    public FireballEntity(EntityType<? extends FireballEntity> type, Level level) {
        super(type, level);
    }

    public FireballEntity(Level level) {
        super(ModEntityTypes.FIREBALL.get(), level);
    }

    @Override
    protected Item getDefaultItem() { return ModItems.FIREBALL.get(); }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level();
            double x = hitResult.getLocation().x;
            double z = hitResult.getLocation().z;
            int gx = (int) Math.floor(x);
            int gz = (int) Math.floor(z);
            int gy = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, gx, gz);
            double y = Math.max(hitResult.getLocation().y, gy + 0.01D);

            setPos(x, y, z);
            setDeltaMovement(0.0D, 0.0D, 0.0D);
            this.setNoGravity(true);
            // Hide the projectile model once the cloud starts
            this.setItem(ItemStack.EMPTY);

            this.mollyActive = true;
            this.mollyTicksRemaining = Config.COMMON.fireballMollyDurationTicks.get();
            this.mollyRadius = Config.COMMON.fireballMollyRadius.get().floatValue();
            this.mollyTickInterval = Math.max(1, Config.COMMON.fireballMollyTickInterval.get());
            this.mollyTickCounter = 0;

            // New visual effect: equivalent to
            // /valorant particle minecraft:small_flame{scale:1} 100 100 2 0 2 0
            ParticleScheduler.spawnRepeating(serverLevel,
                    ParticleTypes.SMALL_FLAME,
                    x, y + 0.05D, z,
                    100,
                    2.0D, 0.0D, 2.0D,
                    0.0D,
                    100);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!mollyActive) {
            return;
        }

        if (level().isClientSide) {
            return;
        }

        // Server-side damage ticks
        // Re-pin to ground in case terrain varies
        int gx = (int) Math.floor(getX());
        int gz = (int) Math.floor(getZ());
        int gy = ((ServerLevel) level()).getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, gx, gz);
        double newY = Math.max(getY(), gy + 0.01D);
        if (Math.abs(newY - getY()) > 1.0E-3D) {
            setPos(getX(), newY, getZ());
        }
        mollyTickCounter++;
        if (mollyTickCounter >= mollyTickInterval) {
            mollyTickCounter = 0;
            float damage = Config.COMMON.fireballMollyDamagePerTick.get().floatValue();
            AABB area = getBoundingBox().inflate(mollyRadius);
            for (LivingEntity target : ((ServerLevel) level()).getEntitiesOfClass(LivingEntity.class, area)) {
                if (!target.isAlive()) continue;
                target.hurt(level().damageSources().hotFloor(), damage);
            }
        }

        mollyTicksRemaining--;
        if (mollyTicksRemaining <= 0) {
            discard();
        }
    }

    private static ParticleOptions resolveParticle(String id) {
        if (id == null || id.isEmpty()) return null;
        String key = id.trim().toUpperCase();
        return switch (key) {
            case "FLAME" -> ParticleTypes.FLAME;
            case "SMALL_FLAME" -> ParticleTypes.SMALL_FLAME;
            case "LAVA" -> ParticleTypes.LAVA;
            case "ASH" -> ParticleTypes.ASH;
            case "SOUL_FIRE_FLAME" -> ParticleTypes.SOUL_FIRE_FLAME;
            case "CAMPFIRE_COSY_SMOKE" -> ParticleTypes.CAMPFIRE_COSY_SMOKE;
            case "CAMPFIRE_SIGNAL_SMOKE" -> ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
            case "SMOKE" -> ParticleTypes.SMOKE;
            case "POOF" -> ParticleTypes.POOF;
            default -> ParticleTypes.FLAME;
        };
    }
}
