package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KnifeAttackPacket(boolean isHeavyAttack) implements CustomPacketPayload {
    public static final Type<KnifeAttackPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "knife_attack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, KnifeAttackPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> buf.writeBoolean(packet.isHeavyAttack),
        buf -> new KnifeAttackPacket(buf.readBoolean())
    );

    public static void handle(KnifeAttackPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                // Perform the knife attack on the server
                performKnifeAttack(serverPlayer, packet.isHeavyAttack);
            }
        });
    }

    private static void performKnifeAttack(net.minecraft.server.level.ServerPlayer player, boolean isHeavyAttack) {
        // Check if player is holding a knife
        var item = player.getMainHandItem();
        if (!(item.getItem() instanceof com.bobby.valorant.world.item.KnifeItem)) {
            return;
        }

        // Perform raycast to find target
        var level = player.level();
        var eyePos = player.getEyePosition(1.0F);
        var lookVec = player.getLookAngle();
        var maxDist = 4.0; // Knife range (matching client)

        var endPos = eyePos.add(lookVec.scale(maxDist));

        // Raycast for entities
        var box = player.getBoundingBox().expandTowards(lookVec.scale(maxDist)).inflate(0.3D); // Tighter for performance
        var predicate = net.minecraft.world.entity.EntitySelector.NO_SPECTATORS
            .and(e -> e instanceof net.minecraft.world.entity.LivingEntity)
            .and(e -> e != player)
            .and(net.minecraft.world.entity.Entity::isAlive);

        var entityHit = ProjectileUtil.getEntityHitResult(
            player, eyePos, endPos, box, predicate, maxDist);

        if (entityHit != null && entityHit.getEntity() instanceof net.minecraft.world.entity.LivingEntity target) {
            // Deal custom knife damage
            var damage = isHeavyAttack ?
                com.bobby.valorant.Config.COMMON.knifeHeavyAttackDamage.get().floatValue() :
                com.bobby.valorant.Config.COMMON.knifeLightAttackDamage.get().floatValue();

            dealCustomKnifeDamage(target, damage, player, isHeavyAttack);
        }
    }

    private static void dealCustomKnifeDamage(net.minecraft.world.entity.LivingEntity target,
                                             float damage,
                                             net.minecraft.server.level.ServerPlayer attacker,
                                             boolean isHeavyAttack) {
        // Use simple player attack damage source (knife attacks are melee)
        var damageSource = attacker.level().damageSources().playerAttack(attacker);

        // Apply damage with custom logic
        target.hurt(damageSource, damage);

        // Add visual effects
        if (isHeavyAttack) {
            // Heavy attack effects (more particles, knockback, etc.)
            attacker.level().sendParticles(
                net.minecraft.core.particles.ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                8, 0.3, 0.3, 0.3, 0.1
            );

            // Small knockback for heavy attacks
            var knockbackVec = target.position().subtract(attacker.position()).normalize();
            target.push(knockbackVec.x * 0.5, 0.2, knockbackVec.z * 0.5);
        } else {
            // Light attack effects
            attacker.level().sendParticles(
                net.minecraft.core.particles.ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                4, 0.2, 0.2, 0.2, 0.05
            );
        }

        // Play custom knife hit sound
        com.bobby.valorant.util.SoundManager.playKnifeHitSound(attacker, isHeavyAttack);
    }

    @Override
    public Type<KnifeAttackPacket> type() {
        return TYPE;
    }
}
