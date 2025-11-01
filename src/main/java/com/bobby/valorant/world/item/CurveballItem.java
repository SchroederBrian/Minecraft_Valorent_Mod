package com.bobby.valorant.world.item;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.AbilityEquipData;
import com.bobby.valorant.player.AbilityStateData;
import com.bobby.valorant.ability.Ability;
import com.bobby.valorant.ability.Abilities;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.world.agent.Agent;
import com.bobby.valorant.registry.ModEntityTypes;
import com.bobby.valorant.registry.ModSounds;
import com.bobby.valorant.world.entity.CurveballOrb;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CurveballItem extends Item {
    public CurveballItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        boolean success = tryThrow(serverPlayer, hand, stack, TurnDirection.RIGHT);
        return success ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
    }

    public boolean tryThrow(ServerPlayer player, InteractionHand hand, ItemStack stack, TurnDirection direction) {
        ServerLevel level = (ServerLevel) player.level();
        CurveballOrb orb = ModEntityTypes.CURVEBALL_ORB.get().create(level, EntitySpawnReason.TRIGGERED);
        if (orb == null) {
            return false;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            return false;
        }

        // Consume ability charge for E slot
        Agent agent = AgentData.getSelectedAgent(player);
        var set = Abilities.getForAgent(agent);
        Ability ability = set.e();
        if (ability == null || !AbilityStateData.tryConsume(player, ability)) {
            player.displayClientMessage(Component.translatable("message.valorant.curveball.no_charges"), true);
            return false;
        }
        // Sync ability state
        int c = set.c() != null ? AbilityStateData.getCharges(player, set.c()) : 0;
        int q = set.q() != null ? AbilityStateData.getCharges(player, set.q()) : 0;
        int eCharges = AbilityStateData.getCharges(player, ability);
        int x = AbilityStateData.getUltPoints(player);
        PacketDistributor.sendToPlayer(player, new com.bobby.valorant.network.SyncAbilityStateS2CPacket(c, q, eCharges, x));

        double speed = Config.COMMON.curveballInitialVelocity.get();
        Vec3 look = player.getLookAngle().normalize().scale(speed);

        orb.setOwner(player);
        orb.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        orb.setDeltaMovement(look);
        // Use a copy so removing from the player's hand doesn't affect the orb's visual/item
        orb.setItem(stack.copy());
        orb.configure(direction == TurnDirection.LEFT,
                Config.COMMON.curveballPreCurveDistance.get(),
                Config.COMMON.curveballCurveAngleDegrees.get(),
                Config.COMMON.curveballCurveDurationTicks.get(),
                Config.COMMON.curveballDetonationDelayTicks.get());

        level.addFreshEntity(orb);
        Valorant.LOGGER.info("[CURVEBALL] Thrown by {} at ({}, {}, {}), dir={}, speed={}",
                player.getGameProfile().getName(), player.getX(), player.getY(), player.getZ(), direction, speed);

        player.swing(hand, true);
        player.getCooldowns().addCooldown(stack, Config.COMMON.curveballThrowCooldownTicks.get());
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CURVEBALL_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // After throwing, restore the previously saved item in the selected slot
        try {
            var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            int selectedSlot = field.getInt(player.getInventory());

            ItemStack restore = AbilityEquipData.takeSaved(player);
            player.getInventory().setItem(selectedSlot, restore);
            player.getInventory().setChanged();
        } catch (Exception e) {
            Valorant.LOGGER.error("[CURVEBALL] Reflection failed while restoring previous item: {}", e.toString());
        }

        return true;
    }

    public enum TurnDirection {
        LEFT,
        RIGHT
    }
}

