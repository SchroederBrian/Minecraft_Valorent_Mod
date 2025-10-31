package com.bobby.valorant.world.item;

import java.util.function.Predicate;

import com.bobby.valorant.world.item.IWeapon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class GunItem extends Item implements IWeapon {
    protected GunItem(Properties properties) {
        super(properties);
    }

    // Configuration contract per-weapon
    protected abstract double getDamage();
    protected abstract double getRange();
    protected abstract double getSpreadDegrees();
    protected abstract int getCooldownTicks();
    protected abstract int getTracerParticles();
    protected abstract int getMuzzleParticles();

    @Override
    public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        // Disable right-click firing for guns; left-click will be handled via events/packet
        return InteractionResult.PASS;
    }

    // Public entry for server-side callers (packets/events) to fire the gun
    public boolean fire(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(stack)) {
            return false;
        }

        if (WeaponAmmoData.getCurrentAmmo(stack) <= 0) {
            // TODO: Play empty clip sound
            return false;
        }

        if (shoot(player, hand, stack)) {
            WeaponAmmoData.decrementAmmo(stack);
            return true;
        }

        return false;
    }

    protected boolean shoot(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        ServerLevel level = (ServerLevel) player.level();

        // Apply cooldown
        player.getCooldowns().addCooldown(stack, getCooldownTicks());

        // Direction with spread
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = applySpread(player.getLookAngle().normalize(), (float) getSpreadDegrees(), level.getRandom());
        double maxDist = getRange();
        Vec3 end = eyePos.add(lookVec.scale(maxDist));

        // Block raycast
        HitResult blockHit = level.clip(new ClipContext(eyePos, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double bestDist = maxDist;
        Vec3 hitPos = end;
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            hitPos = blockHit.getLocation();
            bestDist = eyePos.distanceTo(hitPos);
        }

        // Entity raycast
        AABB box = player.getBoundingBox().expandTowards(lookVec.scale(maxDist)).inflate(1.0D);
        Predicate<Entity> predicate = e -> e instanceof LivingEntity && e != player && e.isAlive();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eyePos, end, box, predicate, 0.0D);
        LivingEntity hitEntity = null;
        if (entityHit != null) {
            double dist = eyePos.distanceTo(entityHit.getLocation());
            if (dist < bestDist) {
                hitPos = entityHit.getLocation();
                bestDist = dist;
                if (entityHit.getEntity() instanceof LivingEntity le) {
                    hitEntity = le;
                }
            }
        }

        // Apply damage if entity hit
        if (hitEntity != null) {
            hitEntity.hurt(level.damageSources().playerAttack(player), (float) getDamage());
        }

        // Particles: subtle tracer only (no muzzle flash)
        spawnTracer(level, eyePos, hitPos);

        // Impact particles (very short-lived)
        level.sendParticles(ParticleTypes.POOF, hitPos.x, hitPos.y, hitPos.z, 1, 0.01D, 0.01D, 0.01D, 0.0D);

        // Sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.2F);

        return true;
    }

    private void spawnTracer(ServerLevel level, Vec3 start, Vec3 end) {
        // Create a visible bullet representation as a small line traveling from start to end
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        
        // Create dust particle options (white color: 0xFFFFFF; scale 0.3)
        // Color packed as: (R << 16) | (G << 8) | B = 0xFFFFFF for white
        net.minecraft.core.particles.DustParticleOptions dustOptions = 
            new net.minecraft.core.particles.DustParticleOptions(0xFFFFFF, 0.1f);
        
        // Create a dense line of particles to represent the bullet itself
        int steps = Math.max(8, (int)(distance * 4)); // Denser for better visibility
        
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / (double) steps;
            Vec3 p = start.add(direction.scale(t));
            
            // Send dust particles along the trajectory
            level.sendParticles(dustOptions, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        
        // Impact effect at the end point
        level.sendParticles(ParticleTypes.CRIT, end.x, end.y, end.z, 2, 0.1D, 0.1D, 0.1D, 0.0D);
    }

    private Vec3 applySpread(Vec3 look, float spreadDegrees, RandomSource random) {
        if (spreadDegrees <= 0.0F) {
            return look;
        }
        // Random small rotation around a random axis perpendicular to look
        float spreadRad = (float) Math.toRadians(spreadDegrees);
        double yaw = (random.nextDouble() - 0.5D) * spreadRad;
        double pitch = (random.nextDouble() - 0.5D) * spreadRad;

        // Apply yaw around Y and pitch around X in world space approximation
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double x1 = look.x * cosYaw - look.z * sinYaw;
        double z1 = look.x * sinYaw + look.z * cosYaw;

        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);
        double y2 = look.y * cosPitch - z1 * sinPitch;
        double z2 = look.y * sinPitch + z1 * cosPitch;

        Vec3 result = new Vec3(x1, y2, z2);
        return result.normalize();
    }
}


