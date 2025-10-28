
package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveCurveballPacket() implements CustomPacketPayload {
    public static final Type<RemoveCurveballPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "remove_curveball"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveCurveballPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
            },
            buf -> new RemoveCurveballPacket());

    public static void handle(RemoveCurveballPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                var inv = serverPlayer.getInventory();
                try {
                    var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
                    field.setAccessible(true);
                    int selectedSlot = field.getInt(inv);
                    ItemStack heldStack = inv.getItem(selectedSlot);
                    if (heldStack.is(ModItems.CURVEBALL.get())) {
                        inv.setItem(selectedSlot, ItemStack.EMPTY);
                    }
                } catch (Exception e) {
                    Valorant.LOGGER.error("Failed to remove curveball via packet", e);
                }
            }
        });
    }

    @Override
    public Type<RemoveCurveballPacket> type() {
        return TYPE;
    }
}
