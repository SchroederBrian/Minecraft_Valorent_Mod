package com.bobby.valorant.events.client;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.network.KnifeAttackPacket;
import com.bobby.valorant.world.item.MeleeWeapon;
import com.bobby.valorant.world.item.GunItem;
import com.bobby.valorant.network.ShootGunPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class CustomLeftClickHandler {

    private CustomLeftClickHandler() {}

    // Custom left-click handler that triggers knife animations
    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Only handle left-click (button 0) on PRESS (down), not RELEASE (up)
        if (event.getButton() != InputConstants.MOUSE_BUTTON_LEFT) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return; // Only trigger on button down, not up

        var heldItem = mc.player.getMainHandItem().getItem();

        // Handle melee weapons (knives)
        if (heldItem instanceof MeleeWeapon) {
            // Check if already in cooldown (animation playing)
            if (com.bobby.valorant.player.KnifeAnimationStateData.isAnimating(mc.player)) {
                return; // Can't attack while animation is playing
            }

            // Start light attack animation
            com.bobby.valorant.player.KnifeAnimationStateData.startAnimation(
                mc.player,
                com.bobby.valorant.player.KnifeAnimationStateData.AnimationType.LIGHT_ATTACK,
                15); // 0.75 seconds for cut animation

            // Client-side prediction: instant raycast and visual feedback
            performClientSideRaycast(mc.player, mc.level, false); // false = light attack

            // Send attack packet to server for authoritative damage dealing
            ClientPacketDistributor.sendToServer(new KnifeAttackPacket(false)); // false = light attack

        // Handle gun weapons
        } else if (heldItem instanceof GunItem gun) {
            // Check if gun is on cooldown
            if (com.bobby.valorant.player.GunCooldownStateData.isOnCooldown(mc.player)) {
                return; // Can't shoot while on cooldown
            }

            // Client-side prediction: instant gunshot sound, visual feedback
            performClientSideGunshot(mc.player, gun);

            // Start cooldown (use reflection to get cooldown ticks)
            int cooldownTicks = 5; // default fallback
            try {
                var cooldownMethod = GunItem.class.getDeclaredMethod("getCooldownTicks");
                cooldownMethod.setAccessible(true);
                cooldownTicks = (Integer) cooldownMethod.invoke(gun);
            } catch (Exception e) {
                // Use default if reflection fails
            }
            com.bobby.valorant.player.GunCooldownStateData.startCooldown(mc.player, cooldownTicks);

            // Send packet to server for authoritative damage
            ClientPacketDistributor.sendToServer(new ShootGunPacket());
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

    private static void performClientSideGunshot(net.minecraft.world.entity.player.Player player, GunItem gun) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return;

        // Get the weapon stack to check ammo
        var weaponStack = player.getMainHandItem();
        if (weaponStack.isEmpty() || !(weaponStack.getItem() instanceof GunItem)) return;

        // Check if weapon has ammo before showing visual effects
        if (com.bobby.valorant.world.item.WeaponAmmoData.getCurrentAmmo(weaponStack) <= 0) {
            return; // No ammo - don't show visual effects
        }

        // Perform client-side gunshot prediction
        var eyePos = player.getEyePosition(1.0F);
        var lookVec = player.getLookAngle();

        // Apply the same spread as server (predictive) - use reflection for protected methods
        float spreadDegrees = 0.0f;
        double range = 50.0; // default fallback

        try {
            var spreadMethod = GunItem.class.getDeclaredMethod("getSpreadDegrees");
            spreadMethod.setAccessible(true);
            spreadDegrees = (Float) spreadMethod.invoke(gun);

            var rangeMethod = GunItem.class.getDeclaredMethod("getRange");
            rangeMethod.setAccessible(true);
            range = (Double) rangeMethod.invoke(gun);
        } catch (Exception e) {
            // Use defaults if reflection fails
            spreadDegrees = 2.0f; // reasonable default spread
            range = 50.0; // reasonable default range
        }

        var spreadLookVec = applySpread(lookVec.normalize(), spreadDegrees, mc.level.random);
        var maxDist = range;
        var endPos = eyePos.add(spreadLookVec.scale(maxDist));

        // Block raycast (client-side)
        var blockHit = mc.level.clip(new net.minecraft.world.level.ClipContext(
            eyePos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER,
            net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        var bestDist = maxDist;
        var hitPos = endPos;
        if (blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            hitPos = blockHit.getLocation();
            bestDist = eyePos.distanceTo(hitPos);
        }

        // Entity raycast (client-side)
        var box = player.getBoundingBox().expandTowards(spreadLookVec.scale(maxDist)).inflate(1.0D);
        var predicate = (java.util.function.Predicate<net.minecraft.world.entity.Entity>)
            e -> e instanceof net.minecraft.world.entity.LivingEntity && e != player && e.isAlive();
        var entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
            player, eyePos, endPos, box, predicate, 0.0D);

        net.minecraft.world.entity.LivingEntity hitEntity = null;
        if (entityHit != null) {
            var dist = eyePos.distanceTo(entityHit.getLocation());
            if (dist < bestDist) {
                hitPos = entityHit.getLocation();
                bestDist = dist;
                if (entityHit.getEntity() instanceof net.minecraft.world.entity.LivingEntity le) {
                    hitEntity = le;
                }
            }
        }

        // Client-side visual feedback
        if (hitEntity != null) {
            // Hit entity - show crit particles
            mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.CRIT,
                hitPos.x, hitPos.y + hitEntity.getBbHeight() / 2, hitPos.z, 0, 0, 0);
        } else {
            // Hit block/air - show impact particles
            mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.POOF,
                hitPos.x, hitPos.y, hitPos.z, 0.01D, 0.01D, 0.01D);
        }

        // Spawn tracer particles (client prediction)
        spawnTracerParticles(mc.level, eyePos, hitPos);

        // Client-side sound prediction - play gun shot sound immediately
        playClientSideGunSound(player, gun);
    }

    private static void spawnTracerParticles(net.minecraft.client.multiplayer.ClientLevel level,
                                           net.minecraft.world.phys.Vec3 start,
                                           net.minecraft.world.phys.Vec3 end) {
        var direction = end.subtract(start);
        var distance = direction.length();

        // Create dust particle options (white color)
        var dustOptions = new net.minecraft.core.particles.DustParticleOptions(0xFFFFFF, 0.1f);

        // Create a dense line of particles to represent the bullet
        var steps = Math.max(8, (int)(distance * 4));
        for (var i = 0; i <= steps; i++) {
            var t = (double) i / (double) steps;
            var p = start.add(direction.scale(t));
            level.addParticle(dustOptions, p.x, p.y, p.z, 0.0D, 0.0D, 0.0D);
        }

        // Impact effect
        level.addParticle(net.minecraft.core.particles.ParticleTypes.CRIT, end.x, end.y, end.z, 0.1D, 0.1D, 0.1D);
    }

    private static void playClientSideGunSound(net.minecraft.world.entity.player.Player player, GunItem gun) {
        // Play a quieter version of the gun sound for immediate client feedback
        // This provides responsive audio feedback while the server handles the authoritative sound
        try {
            var weaponTypeMethod = GunItem.class.getDeclaredMethod("getWeaponTypeName");
            weaponTypeMethod.setAccessible(true);
            String weaponType = (String) weaponTypeMethod.invoke(gun);

            // Resolve sound resource location
            String soundPath = getGunSoundPath(weaponType, player.level().random.nextInt(4) + 1);
            var soundLocation = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("valorant", soundPath);

            // Get sound event and play if available
            net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(soundLocation).ifPresent(soundEvent -> {
                // Play quieter client-side sound (50% volume for prediction)
                player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                    soundEvent.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.0f, false);
            });

        } catch (Exception e) {
            // Fallback - just don't play client-side sound if we can't resolve it
            // Server will still play the authoritative sound
        }
    }

    private static String getGunSoundPath(String weaponType, int variant) {
        return switch (weaponType.toLowerCase()) {
            case "classic" -> "classic.single_shot_" + variant;
            case "ghost" -> "ghost.shot_" + variant;
            case "vandal" -> "vandal.shot_" + variant;
            default -> "classic.single_shot_" + variant;
        };
    }

    // Copy of the spread application logic from GunItem for client prediction
    private static net.minecraft.world.phys.Vec3 applySpread(net.minecraft.world.phys.Vec3 look,
                                                           float spreadDegrees,
                                                           net.minecraft.util.RandomSource random) {
        if (spreadDegrees <= 0.0F) {
            return look;
        }
        // Random small rotation around a random axis perpendicular to look
        var spreadRad = (float) Math.toRadians(spreadDegrees);
        var yaw = (random.nextDouble() - 0.5D) * spreadRad;
        var pitch = (random.nextDouble() - 0.5D) * spreadRad;

        // Apply yaw around Y and pitch around X in world space approximation
        var cosYaw = Math.cos(yaw);
        var sinYaw = Math.sin(yaw);
        var x1 = look.x * cosYaw - look.z * sinYaw;
        var z1 = look.x * sinYaw + look.z * cosYaw;

        var cosPitch = Math.cos(pitch);
        var sinPitch = Math.sin(pitch);
        var y2 = look.y * cosPitch - z1 * sinPitch;
        var z2 = look.y * sinPitch + z1 * cosPitch;

        var result = new net.minecraft.world.phys.Vec3(x1, y2, z2);
        return result.normalize();
    }
}
