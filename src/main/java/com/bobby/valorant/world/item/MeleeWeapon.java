package com.bobby.valorant.world.item;

import java.util.function.Predicate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

public abstract class MeleeWeapon extends Item implements IWeapon {
    protected MeleeWeapon(Properties properties) {
        super(properties);
    }

    // Configuration contract per melee weapon
    protected abstract double getLightAttackDamage();
    protected abstract double getHeavyAttackDamage();
    protected abstract double getRange();
    protected abstract int getLightAttackCooldownTicks();
    protected abstract int getHeavyAttackCooldownTicks();

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // Right-click for heavy attack
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            performHeavyAttack(sp, hand);
        }
        return InteractionResult.CONSUME;
    }

    // Public entry for server-side callers (packets/events) to perform light attack
    public boolean performLightAttack(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(stack)) {
            return false;
        }

        if (meleeAttack(player, hand, stack, getLightAttackDamage())) {
            player.getCooldowns().addCooldown(stack, getLightAttackCooldownTicks());
            // Start light attack animation
            com.bobby.valorant.player.KnifeAnimationStateData.startAnimation(player, com.bobby.valorant.player.KnifeAnimationStateData.AnimationType.LIGHT_ATTACK, getLightAttackCooldownTicks());
            return true;
        }
        return false;
    }

    // Public entry for server-side callers (packets/events) to perform heavy attack
    public boolean performHeavyAttack(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return false;
        }

        if (meleeAttack(player, hand, stack, getHeavyAttackDamage())) {
            player.getCooldowns().addCooldown(stack, getHeavyAttackCooldownTicks());
            // Start heavy attack animation
            com.bobby.valorant.player.KnifeAnimationStateData.startAnimation(player, com.bobby.valorant.player.KnifeAnimationStateData.AnimationType.HEAVY_ATTACK, getHeavyAttackCooldownTicks());
            return true;
        }
        return false;
    }

    protected boolean meleeAttack(ServerPlayer player, InteractionHand hand, ItemStack stack, double damage) {
        ServerLevel level = (ServerLevel) player.level();

        // Direction vector
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle().normalize();
        double maxDist = getRange();
        Vec3 end = eyePos.add(lookVec.scale(maxDist));

        // Block raycast - but we'll allow hitting through blocks for melee
        // (unlike guns which stop at blocks)

        // Entity raycast
        AABB box = player.getBoundingBox().expandTowards(lookVec.scale(maxDist)).inflate(1.0D);
        Predicate<Entity> predicate = e -> e instanceof LivingEntity && e != player && e.isAlive();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eyePos, end, box, predicate, 0.0D);

        LivingEntity hitEntity = null;
        if (entityHit != null) {
            hitEntity = (LivingEntity) entityHit.getEntity();
        }

        // Apply damage if entity hit
        if (hitEntity != null) {
            hitEntity.hurt(level.damageSources().playerAttack(player), (float) damage);

            // Play attack sound
            playAttackSound(player);

            return true;
        }

        return false;
    }

    protected void playAttackSound(ServerPlayer player) {
        String weaponType = getWeaponTypeName();
        com.bobby.valorant.util.SoundManager.playWeaponShotSound(player, weaponType);
    }

    protected String getWeaponTypeName() {
        if (this instanceof KnifeItem) {
            return "knife";
        }
        return "knife"; // fallback
    }
}
