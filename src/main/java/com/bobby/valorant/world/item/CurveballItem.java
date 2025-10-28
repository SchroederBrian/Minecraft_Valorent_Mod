package com.bobby.valorant.world.item;

import com.bobby.valorant.Config;
import com.bobby.valorant.player.CurveballData;
import com.bobby.valorant.registry.ModEntityTypes;
import com.bobby.valorant.registry.ModSounds;
import com.bobby.valorant.world.entity.CurveballOrb;

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

        if (!CurveballData.tryConsumeCharge(player)) {
            player.displayClientMessage(Component.translatable("message.valorant.curveball.no_charges"), true);
            return false;
        }

        double speed = Config.COMMON.curveballInitialVelocity.get();
        Vec3 look = player.getLookAngle().normalize().scale(speed);

        orb.setOwner(player);
        orb.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        orb.setDeltaMovement(look);
        orb.setItem(stack);
        orb.configure(direction == TurnDirection.LEFT,
                Config.COMMON.curveballPreCurveDistance.get(),
                Config.COMMON.curveballCurveAngleDegrees.get(),
                Config.COMMON.curveballCurveDurationTicks.get(),
                Config.COMMON.curveballDetonationDelayTicks.get());

        level.addFreshEntity(orb);

        player.swing(hand, true);
        player.getCooldowns().addCooldown(stack, Config.COMMON.curveballThrowCooldownTicks.get());
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CURVEBALL_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        
        return true;
    }

    public enum TurnDirection {
        LEFT,
        RIGHT
    }
}

