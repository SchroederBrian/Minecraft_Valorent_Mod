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

public record EquipSpikePacket() implements CustomPacketPayload {
    public static final Type<EquipSpikePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "equip_spike"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipSpikePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new EquipSpikePacket());

    public static void handle(EquipSpikePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                Inventory inv = sp.getInventory();
                try {
                    var field = Inventory.class.getDeclaredField("selected");
                    field.setAccessible(true);
                    int selectedSlot = field.getInt(inv);
                    ItemStack current = inv.getItem(selectedSlot);

                    if (current.is(ModItems.SPIKE.get()) ||
                        current.is(ModItems.CURVEBALL.get()) ||
                        current.is(ModItems.FIREBALL.get())) {
                        return;
                    }

                    AbilityEquipData.saveCurrent(sp, current);
                    Valorant.LOGGER.info("[SPIKE EQUIP] Saved item: {}", current.getItem());

                    for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                        if (inv.getItem(i).is(ModItems.SPIKE.get())) {
                            field.set(inv, i);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Valorant.LOGGER.error("[SPIKE EQUIP] Failed to equip spike: {}", e.toString());
                }
            }
        });
    }

    @Override
    public Type<EquipSpikePacket> type() {
        return TYPE;
    }
}
