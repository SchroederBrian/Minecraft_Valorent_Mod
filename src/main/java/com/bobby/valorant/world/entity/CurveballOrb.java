package com.bobby.valorant.world.entity;

import java.util.List;

import com.bobby.valorant.Config;
import com.bobby.valorant.registry.ModEntityTypes;
import com.bobby.valorant.registry.ModItems;
import com.bobby.valorant.registry.ModSounds;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CurveballOrb extends ThrowableItemProjectile {
    private static final double EPSILON = 1.0E-4D;

    private double targetStraightDistance;
    private double straightDistanceTravelled;
    private int totalCurveTicks;
    private int curveTicksRemaining;
    private float yawPerTickRadians;
    private boolean curveLeft;
    private int detonationDelayTicks;
    private int detonationCountdown;
    private Phase phase = Phase.STRAIGHT;
    private boolean detonated;

    public CurveballOrb(EntityType<? extends CurveballOrb> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public CurveballOrb(Level level) {
        super(ModEntityTypes.CURVEBALL_ORB.get(), level);
        this.setNoGravity(true);
    }

    public void configure(boolean curveLeft, double targetStraightDistance, double curveAngleDegrees, int curveDurationTicks, int detonationDelayTicks) {
        this.curveLeft = curveLeft;
        this.targetStraightDistance = Math.max(0.0D, targetStraightDistance);
        this.totalCurveTicks = Math.max(1, curveDurationTicks);
        this.yawPerTickRadians = (float) Math.toRadians(curveAngleDegrees / (double) this.totalCurveTicks);
        this.detonationDelayTicks = Math.max(0, detonationDelayTicks);
        this.detonationCountdown = -1;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CURVEBALL.get();
    }

    @Override
    public void tick() {
        Vec3 previousPosition = position();
        super.tick();

        if (level().isClientSide) {
            return;
        }

        Vec3 currentPosition = position();
        double moved = currentPosition.distanceTo(previousPosition);
        if (phase == Phase.STRAIGHT) {
            straightDistanceTravelled += moved;
            if (straightDistanceTravelled + EPSILON >= targetStraightDistance) {
                phase = Phase.CURVE;
                curveTicksRemaining = totalCurveTicks;
            }
        }

        if (phase == Phase.CURVE) {
            applyCurveRotation();
            curveTicksRemaining--;
            if (curveTicksRemaining <= 0) {
                phase = Phase.POST_CURVE;
                detonationCountdown = detonationDelayTicks;
            }
        } else if (phase == Phase.POST_CURVE) {
            if (detonationCountdown <= 0) {
                detonate();
            } else {
                detonationCountdown--;
            }
        }

        updateOrientation();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!level().isClientSide) {
            detonate();
        }
    }

    private void applyCurveRotation() {
        Vec3 movement = getDeltaMovement();
        double speed = movement.length();
        if (speed <= 0.0D) {
            return;
        }

        double angle = yawPerTickRadians * (curveLeft ? 1.0D : -1.0D);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newX = movement.x * cos - movement.z * sin;
        double newZ = movement.x * sin + movement.z * cos;
        setDeltaMovement(newX, movement.y, newZ);
    }

    private void detonate() {
        if (detonated || level().isClientSide || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        detonated = true;

        double radius = Config.COMMON.curveballFlashRadius.get();
        double coneAngle = Config.COMMON.curveballFlashConeAngleDegrees.get();
        int duration = Config.COMMON.curveballFlashDurationTicks.get();

        Vec3 center = position();
        AABB area = getBoundingBox().inflate(radius);
        List<ServerPlayer> players = serverLevel.getEntitiesOfClass(ServerPlayer.class, area);
        for (ServerPlayer target : players) {
            if (!shouldBlind(target, center, radius, coneAngle)) {
                continue;
            }
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false));
        }

        serverLevel.sendParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        serverLevel.playSound(null, getX(), getY(), getZ(), ModSounds.CURVEBALL_DETONATE.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
        discard();
    }

    private boolean shouldBlind(Player target, Vec3 flashPosition, double radius, double coneAngle) {
        if (!Config.COMMON.curveballAffectsThrower.get() && target == getOwner()) {
            return false;
        }

        Vec3 eyePosition = target.getEyePosition();
        Vec3 toFlash = flashPosition.subtract(eyePosition);
        double distance = toFlash.length();
        if (distance > radius || distance < EPSILON) {
            return false;
        }

        if (!target.hasLineOfSight(this)) {
            return false;
        }

        toFlash = toFlash.normalize();
        Vec3 look = target.getViewVector(1.0F).normalize();
        double dot = look.dot(toFlash);
        
        // Check if player is looking toward the flash (within the cone angle)
        double angle = Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0D, 1.0D)));
        return angle <= coneAngle * 0.5D;
    }

    private void updateOrientation() {
        Vec3 movement = getDeltaMovement();
        double speed = movement.length();
        if (speed <= EPSILON) {
            return;
        }
        float yaw = (float) Math.toDegrees(Math.atan2(-movement.x, movement.z));
        double vertical = Mth.clamp(-movement.y / speed, -1.0D, 1.0D);
        float pitch = (float) Math.toDegrees(Math.asin(vertical));
        setYRot(yaw);
        setXRot(pitch);
    }

    private enum Phase {
        STRAIGHT,
        CURVE,
        POST_CURVE
    }
}

