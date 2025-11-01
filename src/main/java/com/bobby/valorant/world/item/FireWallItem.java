package com.bobby.valorant.world.item;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.AbilityEquipData;
import com.bobby.valorant.player.FireWallData;
import com.bobby.valorant.registry.ModSounds;
import com.bobby.valorant.world.entity.FireWallEntity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireWallItem extends Item {
    public FireWallItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        boolean success = tryCreateWall(serverPlayer, hand, stack);
        return success ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
    }

    public boolean tryCreateWall(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        ServerLevel level = (ServerLevel) player.level();

        if (player.getCooldowns().isOnCooldown(stack)) {
            return false;
        }

        if (!FireWallData.tryConsumeCharge(player)) {
            player.displayClientMessage(Component.translatable("message.valorant.firewall.no_charges"), true);
            return false;
        }

        // Create fire wall entity 2 blocks in front of player
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 startPosition = player.getEyePosition().add(direction.scale(2.0));

        FireWallEntity fireWall = new FireWallEntity(level, player, startPosition, direction);
        // Fire wall manages its own armor stands, no need to add to level

        Valorant.LOGGER.info("[FIREWALL] Created by {} at ({}, {}, {}), direction=({}, {}, {})",
                player.getGameProfile().getName(),
                startPosition.x, startPosition.y, startPosition.z,
                direction.x, direction.y, direction.z);

        player.swing(hand, true);
        player.getCooldowns().addCooldown(stack, Config.COMMON.firewallThrowCooldownTicks.get());
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                       ModSounds.CURVEBALL_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // After creating wall, restore the previously saved item in the selected slot
        try {
            var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            int selectedSlot = field.getInt(player.getInventory());

            ItemStack restore = AbilityEquipData.takeSaved(player);
            player.getInventory().setItem(selectedSlot, restore);
            player.getInventory().setChanged();
        } catch (Exception e) {
            Valorant.LOGGER.error("[FIREWALL] Reflection failed while restoring previous item: {}", e.toString());
        }

        return true;
    }
}
