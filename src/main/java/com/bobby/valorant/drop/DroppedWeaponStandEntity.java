package com.bobby.valorant.drop;

import com.bobby.valorant.registry.ModEntityTypes;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DroppedWeaponStandEntity extends ArmorStand {
    private boolean lockedOnGround = false;
    private int despawnAtTick = 0; // 0 = disabled / not scheduled
    private double lockX, lockY, lockZ;
    private float lockYRot, lockXRot;
    private int lastInteractTick = -1000;
    private static final int INTERACT_COOLDOWN_TICKS = 6;

    public DroppedWeaponStandEntity(EntityType<? extends DroppedWeaponStandEntity> type, Level level) {
        super(type, level);
        initializeStandAppearance();
    }

    public DroppedWeaponStandEntity(Level level) {
        super(ModEntityTypes.DROPPED_WEAPON_STAND.get(), level);
        initializeStandAppearance();
    }

    private void initializeStandAppearance() {
        this.setInvisible(true);
        this.setNoBasePlate(true);
        this.setShowArms(false);
        this.setNoGravity(false);
        this.setInvulnerable(true);
        this.setSilent(true);
        // Persistence: ArmorStand saves equipment by default; no special flag needed here.
    }

    @Override
    public void tick() {
        super.tick();
        // Basic ground-lock behavior; full logic implemented in subsequent task
        if (!this.level().isClientSide) {
            if (!lockedOnGround) {
                if (this.onGround()) {
                    lockToCurrentPose();
                }
            } else {
                // Stay frozen
                this.setDeltaMovement(0.0D, 0.0D, 0.0D);
                this.setPos(lockX, lockY, lockZ);
                this.setYRot(lockYRot);
                this.setXRot(lockXRot);
            }
            if (despawnAtTick > 0 && this.tickCount >= despawnAtTick) {
                // If despawning with item, ensure safety by dropping the head item
                ItemStack head = getItemBySlot(EquipmentSlot.HEAD);
                if (!head.isEmpty()) {
                    net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(this.level(), this.getX(), this.getY() + 0.1, this.getZ(), head.copy());
                    this.level().addFreshEntity(item);
                }
                discard();
            }
        }
    }

    public void setHeadItem(ItemStack stack) {
        this.setItemSlot(EquipmentSlot.HEAD, stack);
    }

    public void scheduleAutoDespawn(int ticksFromNow) {
        if (ticksFromNow <= 0) {
            this.despawnAtTick = 0;
        } else {
            this.despawnAtTick = this.tickCount + ticksFromNow;
        }
    }

    public boolean isLockedOnGround() {
        return lockedOnGround;
    }

    public void setLockedOnGround(boolean locked) {
        this.lockedOnGround = locked;
        this.setNoGravity(locked);
        if (locked) {
            this.setDeltaMovement(0.0D, 0.0D, 0.0D);
            lockToCurrentPose();
        }
    }

    public void lockToCurrentPose() {
        this.setNoGravity(true);
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        this.lockedOnGround = true;
        this.lockX = this.getX();
        this.lockY = this.getY();
        this.lockZ = this.getZ();
        this.lockYRot = this.getYRot();
        this.lockXRot = this.getXRot();
    }

    public void applyThrowImpulse(double speed, double upward) {
        // Use current rotation to compute look vector
        double yawRad = Math.toRadians(this.getYRot());
        double pitchRad = Math.toRadians(this.getXRot());
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        var motion = new net.minecraft.world.phys.Vec3(x, y, z).normalize().scale(speed).add(0.0D, upward, 0.0D);
        this.setDeltaMovement(motion);
    }

    @Override
    public net.minecraft.world.InteractionResult interact(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (this.level().isClientSide) return net.minecraft.world.InteractionResult.CONSUME;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer sp)) return net.minecraft.world.InteractionResult.PASS;

        // Cooldown gate
        if (this.tickCount - lastInteractTick < INTERACT_COOLDOWN_TICKS) {
            return net.minecraft.world.InteractionResult.FAIL;
        }

        // Validate distance and LoS
        double range = com.bobby.valorant.Config.COMMON.pickupRange.get();
        net.minecraft.world.phys.Vec3 eye = sp.getEyePosition();
        net.minecraft.world.phys.Vec3 target = new net.minecraft.world.phys.Vec3(this.getX(), this.getY() + 0.4, this.getZ());
        if (eye.distanceTo(target) > range + 1.0E-3) return net.minecraft.world.InteractionResult.FAIL;
        var ctx = new net.minecraft.world.level.ClipContext(eye, target, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, sp);
        var hit = this.level().clip(ctx);
        boolean unobstructed = hit == null || hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS || hit.getLocation().distanceToSqr(eye) >= eye.distanceToSqr(target) - 1.0E-3;
        if (!unobstructed) return net.minecraft.world.InteractionResult.FAIL;

        net.minecraft.world.item.ItemStack ground = this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        if (ground.isEmpty()) return net.minecraft.world.InteractionResult.FAIL;

        int slot = getTargetSlotFromConfig(ground);
        if (slot < 0 || slot >= sp.getInventory().getContainerSize()) {
            // Fallback: try add to inventory
            if (sp.getInventory().add(ground.copy())) {
                this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.item.ItemStack.EMPTY);
                this.discard();
                lastInteractTick = this.tickCount;
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
            return net.minecraft.world.InteractionResult.FAIL;
        }

        net.minecraft.world.item.ItemStack inSlot = sp.getInventory().getItem(slot);
        if (inSlot.isEmpty()) {
            sp.getInventory().setItem(slot, ground.copy());
            sp.containerMenu.broadcastChanges();
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.item.ItemStack.EMPTY);
            this.discard();
            if (java.lang.Boolean.TRUE.equals(com.bobby.valorant.Config.COMMON.enablePickupSfx.get()) && java.lang.Boolean.TRUE.equals(com.bobby.valorant.Config.COMMON.soundEnabled.get())) {
                this.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
            }
            if (java.lang.Boolean.TRUE.equals(com.bobby.valorant.Config.COMMON.enableParticles.get()) && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 8, 0.05, 0.05, 0.05, 0.01);
            }
        } else {
            // Swap
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, inSlot.copy());
            sp.getInventory().setItem(slot, ground.copy());
            sp.containerMenu.broadcastChanges();
            // Relock pose (in case)
            if (!this.isLockedOnGround()) {
                this.lockToCurrentPose();
            }
            if (java.lang.Boolean.TRUE.equals(com.bobby.valorant.Config.COMMON.enablePickupSfx.get()) && java.lang.Boolean.TRUE.equals(com.bobby.valorant.Config.COMMON.soundEnabled.get())) {
                this.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
            }
            if (java.lang.Boolean.TRUE.equals(com.bobby.valorant.Config.COMMON.enableParticles.get()) && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 8, 0.05, 0.05, 0.05, 0.01);
            }
        }
        lastInteractTick = this.tickCount;
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    private static int getTargetSlotFromConfig(net.minecraft.world.item.ItemStack stack) {
        Object raw = com.bobby.valorant.Config.COMMON.itemTargetSlots.get();
        if (!(raw instanceof com.electronwill.nightconfig.core.Config conf)) return -1;
        for (java.util.Map.Entry<String, Object> e : conf.valueMap().entrySet()) {
            String key = e.getKey();
            int slotIdx;
            try { slotIdx = Integer.parseInt(String.valueOf(e.getValue())); }
            catch (Exception ex) { continue; }
            if (key.startsWith("#")) {
                var id = net.minecraft.resources.ResourceLocation.tryParse(key.substring(1));
                if (id == null) continue;
                var tag = net.minecraft.tags.ItemTags.create(id);
                if (stack.is(tag)) return slotIdx;
            } else {
                var id = net.minecraft.resources.ResourceLocation.tryParse(key);
                if (id == null) continue;
                var stackId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (id.equals(stackId)) return slotIdx;
            }
        }
        return -1;
    }

    // Full persistence hooks and extra state will be added during entity-impl task
}


