package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.AbilityEquipData;
import com.bobby.valorant.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server: replace selected hotbar slot with a curveball and remember previous stack.
 */
public record EquipCurveballPacket() implements CustomPacketPayload {
    public static final Type<EquipCurveballPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "equip_curveball"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipCurveballPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {},
            buf -> new EquipCurveballPacket()
    );

    public static void handle(EquipCurveballPacket packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sp)) return;
        Inventory inv = sp.getInventory();
        try {
            var field = Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            int slot = field.getInt(inv);
            com.bobby.valorant.player.AbilityEquipData.saveSelectedSlot(sp, slot);
            ItemStack current = inv.getItem(slot).copy();
            AbilityEquipData.saveCurrent(sp, current);
            inv.setItem(slot, new ItemStack(ModItems.CURVEBALL.get()));
            inv.setChanged();
        } catch (Exception e) {
            // fallback: put in slot 0
            ItemStack current = inv.getItem(0).copy();
            AbilityEquipData.saveCurrent(sp, current);
            inv.setItem(0, new ItemStack(ModItems.CURVEBALL.get()));
            inv.setChanged();
        }
    }

    @Override
    public Type<EquipCurveballPacket> type() { return TYPE; }
}


