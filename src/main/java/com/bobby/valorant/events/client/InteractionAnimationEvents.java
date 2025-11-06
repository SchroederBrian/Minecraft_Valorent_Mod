package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.KnifeAttackPacket;
import com.bobby.valorant.world.item.GunItem;
import com.bobby.valorant.world.item.MeleeWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class InteractionAnimationEvents {
    private InteractionAnimationEvents() {}

    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        boolean holdingGun = (main.getItem() instanceof GunItem) || (off.getItem() instanceof GunItem);
        boolean holdingMelee = (main.getItem() instanceof MeleeWeapon) || (off.getItem() instanceof MeleeWeapon);
        if (!holdingGun && !holdingMelee) return;

        // Prevent the vanilla swing/animation for guns and melee weapons
        // - Right-click: suppress and cancel for guns (we don't use right-click for guns)
        // - Right-click: suppress and cancel for melee weapons and start heavy attack animation
        // - Left-click: suppress swing but DO NOT cancel so attack still proceeds
        // Note: Left-click for both guns and melee is now handled by CustomLeftClickHandler
        if (event.isUseItem()) {
            event.setSwingHand(false);
            event.setCanceled(true);
            // Start heavy attack animation for melee weapons (with cooldown check)
            if (holdingMelee) {
                // Check if already in cooldown (animation playing)
                if (!com.bobby.valorant.player.KnifeAnimationStateData.isAnimating(mc.player)) {
                    com.bobby.valorant.player.KnifeAnimationStateData.startAnimation(
                        mc.player,
                        com.bobby.valorant.player.KnifeAnimationStateData.AnimationType.HEAVY_ATTACK,
                        30); // 1.5 seconds for stab animation

                    // Client-side prediction: instant raycast and visual feedback
                    performClientSideRaycast(mc.player, mc.level, true); // true = heavy attack

                    // Send attack packet to server for authoritative damage dealing
                    net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new KnifeAttackPacket(true)); // true = heavy attack
                }
            }
        } else if (event.isAttack()) {
            event.setSwingHand(false);
            // Left-click animations are now handled by CustomLeftClickHandler
        }
    }

    private static void performClientSideRaycast(net.minecraft.world.entity.player.Player player,
                                                 net.minecraft.client.multiplayer.ClientLevel level,
                                                 boolean isHeavyAttack) {
        // Perform client-side raycast for immediate feedback
        var eyePos = player.getEyePosition(1.0F);
        var lookVec = player.getLookAngle();
        var maxDist = 4.0; // Knife range (matching server)

        var endPos = eyePos.add(lookVec.scale(maxDist));

        // Raycast for entities (client-side)
        var box = player.getBoundingBox().expandTowards(lookVec.scale(maxDist)).inflate(0.3D); // Tighter than server for performance
        var predicate = net.minecraft.world.entity.EntitySelector.NO_SPECTATORS
            .and(e -> e instanceof net.minecraft.world.entity.LivingEntity)
            .and(e -> e != player)
            .and(net.minecraft.world.entity.Entity::isAlive);

        var entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
            player, eyePos, endPos, box, predicate, maxDist);

        if (entityHit != null && entityHit.getEntity() instanceof net.minecraft.world.entity.LivingEntity target) {
            // Immediate client feedback - particles
            var hitPos = entityHit.getLocation();
            if (isHeavyAttack) {
                // Heavy attack: more particles
                for (int i = 0; i < 8; i++) {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.CRIT,
                        hitPos.x, hitPos.y + target.getBbHeight() / 2, hitPos.z,
                        (level.random.nextDouble() - 0.5) * 0.3,
                        (level.random.nextDouble() - 0.5) * 0.3,
                        (level.random.nextDouble() - 0.5) * 0.3);
                }
            } else {
                // Light attack: fewer particles
                for (int i = 0; i < 4; i++) {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.CRIT,
                        hitPos.x, hitPos.y + target.getBbHeight() / 2, hitPos.z,
                        (level.random.nextDouble() - 0.5) * 0.2,
                        (level.random.nextDouble() - 0.5) * 0.2,
                        (level.random.nextDouble() - 0.5) * 0.2);
                }
            }

            // Optional: immediate local sound feedback (quiet preview)
            // player.playSound(...); // Could add a quiet hit preview sound here
        }
    }
}


