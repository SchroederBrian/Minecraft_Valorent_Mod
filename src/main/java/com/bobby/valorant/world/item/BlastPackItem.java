package com.bobby.valorant.world.item;

import com.bobby.valorant.player.AbilityEquipData;
import com.bobby.valorant.player.AbilityStateData;
import com.bobby.valorant.ability.Ability;
import com.bobby.valorant.ability.Abilities;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BlastPackItem extends Item {
    public BlastPackItem(Properties properties) {
        super(properties);
    }

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
            player.displayClientMessage(Component.translatable("message.valorant.blast_pack.no_charges"), true);
            return InteractionResult.FAIL;
        }
        // Sync ability state
        int c = set.c() != null ? AbilityStateData.getCharges(sp, set.c()) : 0;
        int q = AbilityStateData.getCharges(sp, ability);
        int eCharges = set.e() != null ? AbilityStateData.getCharges(sp, set.e()) : 0;
        int x = AbilityStateData.getUltPoints(sp);
        PacketDistributor.sendToPlayer(sp, new com.bobby.valorant.network.SyncAbilityStateS2CPacket(c, q, eCharges, x));
        // TODO: Implement blast pack effect (e.g., place satchel)
        player.displayClientMessage(Component.literal("Blast Pack used!"), true); // Placeholder

        player.swing(hand, true);
        // Restore previous item
        try {
            var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            int selectedSlot = field.getInt(player.getInventory());
            ItemStack restore = AbilityEquipData.takeSaved(player);
            Integer prevSlot = AbilityEquipData.takeSavedSelectedSlot(player);
            player.getInventory().setItem(selectedSlot, restore);
            if (prevSlot != null) {
                field.setInt(player.getInventory(), prevSlot);
            }
            player.getInventory().setChanged();
        } catch (Exception e) {
            // Ignore
        }
        return InteractionResult.SUCCESS;
    }
}
