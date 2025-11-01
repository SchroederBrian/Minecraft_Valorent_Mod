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
import com.bobby.valorant.world.entity.FireballEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireballItem extends Item {
    public FireballItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ServerPlayer sp = (ServerPlayer) player;
        if (sp.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        // Consume ability charge for Q slot
        Agent agent = AgentData.getSelectedAgent(sp);
        var set = Abilities.getForAgent(agent);
        Ability ability = set.q();
        if (ability == null || !AbilityStateData.tryConsume(sp, ability)) {
            player.displayClientMessage(Component.translatable("message.valorant.fireball.no_charges"), true);
            return InteractionResult.FAIL;
        }
        // Sync ability state
        int c = set.c() != null ? AbilityStateData.getCharges(sp, set.c()) : 0;
        int q = AbilityStateData.getCharges(sp, ability);
        int eCharges = set.e() != null ? AbilityStateData.getCharges(sp, set.e()) : 0;
        int x = AbilityStateData.getUltPoints(sp);
        PacketDistributor.sendToPlayer(sp, new com.bobby.valorant.network.SyncAbilityStateS2CPacket(c, q, eCharges, x));
        ServerLevel serverLevel = (ServerLevel) level;
        FireballEntity orb = ModEntityTypes.FIREBALL.get().create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (orb == null) return InteractionResult.FAIL;

        double speed = Config.COMMON.fireballInitialVelocity.get();
        Vec3 look = player.getLookAngle().normalize().scale(speed);

        orb.setOwner(player);
        orb.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        orb.setDeltaMovement(look);
        orb.setItem(stack.copy());
        serverLevel.addFreshEntity(orb);

        player.swing(hand, true);
        sp.getCooldowns().addCooldown(stack, Config.COMMON.fireballThrowCooldownTicks.get());
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

        try {
            var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            int selectedSlot = field.getInt(player.getInventory());
            ItemStack restore = AbilityEquipData.takeSaved(player);
            player.getInventory().setItem(selectedSlot, restore);
            player.getInventory().setChanged();
        } catch (Exception e) {
            Valorant.LOGGER.error("[FIREBALL] Reflection failed while restoring previous item: {}", e.toString());
        }
        return InteractionResult.SUCCESS;
    }
}
