package com.bobby.valorant.world.entity;

import com.bobby.valorant.Config;
import com.bobby.valorant.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class FireWallEntity {
    private static final float MAX_TURN_DEG = 10.0F;
    private static final List<FireWallEntity> ACTIVE_WALLS = new ArrayList<>();

    public static void tickAll() {
        Iterator<FireWallEntity> iterator = ACTIVE_WALLS.iterator();
        while (iterator.hasNext()) {
            FireWallEntity wall = iterator.next();
            wall.tick();
            if (!wall.active) {
                iterator.remove();
            }
        }
    }
    private final UUID ownerId;
    private final ServerLevel level;
    private final List<ArmorStand> wallSegments = new ArrayList<>();
    private final List<Vec3> segmentPositions = new ArrayList<>();
    private final Vec3 startPosition;
    private Vec3 lastDirection;
    private int ticksSinceCreation = 0;
    private int nextSegmentTick = 0;
    private boolean active = true;
    private final int maxSegments;
    private final double spacing;
    private double fixedY;
    private final float rotationOffsetDeg;

    public FireWallEntity(ServerLevel level, ServerPlayer owner, Vec3 startPosition, Vec3 initialDirection) {
        this.ownerId = owner.getUUID();
        this.level = level;
        this.spacing = Config.COMMON.firewallSegmentSpacing.get();
        this.fixedY = owner.getY() + 0.5D;
        this.rotationOffsetDeg = Config.COMMON.firewallRotationOffsetDegrees.get().floatValue();
        double maxRange = Config.COMMON.firewallMaxRange.get();
        this.maxSegments = Math.max(1, (int) Math.floor(maxRange / Math.max(0.001D, spacing)));
        this.startPosition = new Vec3(startPosition.x, this.fixedY, startPosition.z);
        this.lastDirection = initialDirection.normalize();

        // Create first segment at start position with fixed Y
        addWallSegment(this.startPosition);

        // Add to active walls for ticking
        ACTIVE_WALLS.add(this);
    }

    public void tick() {
        if (!active) return;

        ticksSinceCreation++;

        // Check if we should add a new segment
        if (ticksSinceCreation >= nextSegmentTick) {
            addNewSegment();
            nextSegmentTick += Config.COMMON.firewallGrowthSpeed.get();
        }

        // Curving handled during new-segment placement; nothing to do here

        // Damage entities touching the wall
        damageEntitiesInWall();

        // Check if wall should expire
        if (ticksSinceCreation >= Config.COMMON.firewallDurationTicks.get()) {
            removeWall();
            return;
        }

        // Update particles
        spawnParticles();
    }

    private void updateWallDirection() { /* no-op: handled in addNewSegment */ }

    private void curveLastSegments(Vec3 curveDirection) { /* deprecated, unused */ }

    private void addNewSegment() {
        if (wallSegments.size() >= maxSegments) {
            return; // Max range reached
        }

        // Compute clamped head yaw from player look and advance one spacing step
        Vec3 basePos = segmentPositions.get(segmentPositions.size() - 1);
        float currentYaw = dirToYaw(this.lastDirection);
        float desiredYaw = computeDesiredYawFromPlayer();
        float delta = Mth.wrapDegrees(desiredYaw - currentYaw);
        float clamped = Mth.clamp(delta, -MAX_TURN_DEG, MAX_TURN_DEG);
        float newYaw = currentYaw + clamped;
        Vec3 dir = yawToDir(newYaw);
        this.lastDirection = dir;

        Vec3 target = new Vec3(basePos.x + dir.x * spacing,
                                this.fixedY, // Use current fixed Y level
                                basePos.z + dir.z * spacing);

        net.minecraft.core.BlockPos targetPos = net.minecraft.core.BlockPos.containing(target);
        if (!level.getBlockState(targetPos).isAir()) {
            // Check if we can climb up one block
            net.minecraft.core.BlockPos targetPosAbove = targetPos.above();
            if (level.getBlockState(targetPosAbove).isAir()) {
                // Climb up: update fixedY and try the new position
                this.fixedY += 1.0D;
                target = new Vec3(target.x, this.fixedY, target.z);
                targetPos = targetPosAbove;
            } else {
                // Check if we can descend down one block
                net.minecraft.core.BlockPos targetPosBelow = targetPos.below();
                if (level.getBlockState(targetPosBelow).isAir()) {
                    // Descend down: update fixedY and try the new position
                    this.fixedY -= 1.0D;
                    target = new Vec3(target.x, this.fixedY, target.z);
                    targetPos = targetPosBelow;
                } else {
                    // Can't climb up or down: stop wall
                    this.active = false;
                    return;
                }
            }
        }

        addWallSegment(target);
        // Smooth the tail (last 2–3 segments)
        relayoutTailSmooth(3);
    }

    private void addWallSegment(Vec3 position) {
        ArmorStand stand = new ArmorStand(level, position.x, position.y, position.z);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setNoGravity(false);
        stand.setSilent(true);
        stand.setShowArms(false);
        stand.setNoBasePlate(true);
        stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(ModItems.WALLSEGMENT.get()));

        level.addFreshEntity(stand);
        wallSegments.add(stand);
        segmentPositions.add(position);

        // Orient stand to face along the wall direction
        orientStand(stand, lastDirection);

        // Play sound
        level.playSound(null, position.x, position.y, position.z,
                       net.minecraft.sounds.SoundEvents.BLAZE_SHOOT,
                       SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    private void orientStand(ArmorStand stand, Vec3 direction) {
        Vec3 dir = direction.normalize();
        float yaw = (float) (Math.atan2(dir.z, dir.x) * (180.0D / Math.PI)) - 90.0F + rotationOffsetDeg;
        stand.setYRot(yaw);
        stand.setYBodyRot(yaw);
        stand.setYHeadRot(yaw);
    }

    private float computeDesiredYawFromPlayer() {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null) return dirToYaw(this.lastDirection);
        Vec3 look = owner.getLookAngle().normalize();
        return dirToYaw(look);
    }

    private static float dirToYaw(Vec3 dir) {
        return (float) (Math.atan2(dir.z, dir.x) * (180.0D / Math.PI)) - 90.0F;
    }

    private static Vec3 yawToDir(float yawDeg) {
        float rad = yawDeg * ((float) Math.PI / 180.0F);
        float x = Mth.cos(rad + (float)Math.PI / 2F);
        float z = Mth.sin(rad + (float)Math.PI / 2F);
        return new Vec3(x, 0.0D, z).normalize();
    }

    private void relayoutTailSmooth(int tailCount) {
        int size = segmentPositions.size();
        if (size < 2) return;
        int k = Math.min(tailCount, size - 1);
        int startIndex = size - k - 1; // anchor index (fixed)
        if (startIndex < 0) startIndex = 0;

        Vec3 anchor = segmentPositions.get(startIndex);
        // Use fixed Y for all segments
        float startYaw;
        if (startIndex + 1 < segmentPositions.size()) {
            Vec3 next = segmentPositions.get(startIndex + 1).subtract(anchor);
            startYaw = dirToYaw(next);
        } else {
            startYaw = dirToYaw(this.lastDirection);
        }
        float endYaw = dirToYaw(this.lastDirection);

        // Lay out subsequent k segments with yaw interpolated from startYaw -> endYaw
        float step = 1.0F / (float) k;
        Vec3 prevPos = anchor;
        for (int i = 1; i <= k; i++) {
            float t = i * step;
            float yaw = Mth.lerp(t, startYaw, endYaw);
            Vec3 fwd = yawToDir(yaw);
            Vec3 pos = new Vec3(prevPos.x + fwd.x * spacing,
                                this.fixedY, // Use fixed Y for all segments
                                prevPos.z + fwd.z * spacing);
            int idx = startIndex + i;
            segmentPositions.set(idx, pos);
            ArmorStand stand = wallSegments.get(idx);
            stand.setPos(pos);
            orientStand(stand, fwd);
            prevPos = pos;
        }
    }


    private void damageEntitiesInWall() {
        double damage = Config.COMMON.firewallDamagePerTick.get();
        if (damage <= 0) return;

        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);

        for (ArmorStand segment : wallSegments) {
            AABB aabb = segment.getBoundingBox().inflate(0.5);
            List<net.minecraft.world.entity.Entity> entities = level.getEntities(segment, aabb,
                entity -> entity instanceof net.minecraft.world.entity.LivingEntity &&
                         entity != owner &&
                         !(entity instanceof ArmorStand));

            for (net.minecraft.world.entity.Entity entity : entities) {
                entity.hurt(level.damageSources().inFire(), (float) damage);
                // Apply fire effect
                if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                    living.setRemainingFireTicks(20);
                }
            }
        }
    }

    private void spawnParticles() {
        for (ArmorStand segment : wallSegments) {
            Vec3 pos = segment.position();
            level.sendParticles(ParticleTypes.FLAME,
                              pos.x, pos.y + 0.5, pos.z,
                              2, 0.1, 0.1, 0.1, 0.01);
        }
    }

    private void removeWall() {
        active = false;
        for (ArmorStand segment : wallSegments) {
            segment.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
        wallSegments.clear();
        segmentPositions.clear();
    }

    public boolean isActive() {
        return active;
    }

    public void forceRemove() {
        removeWall();
    }
}
