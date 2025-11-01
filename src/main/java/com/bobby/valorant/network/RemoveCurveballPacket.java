
package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.AbilityEquipData;
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
                    Valorant.LOGGER.info("[CURVEBALL] Scroll removal request: slot={}, held={} (empty={})",
                            selectedSlot, heldStack.getItem(), heldStack.isEmpty());
                    if (heldStack.is(ModItems.CURVEBALL.get())) {
                        ItemStack restore = AbilityEquipData.takeSaved(serverPlayer);
                        inv.setItem(selectedSlot, restore);
                        Integer prev = AbilityEquipData.takeSavedSelectedSlot(serverPlayer);
                        if (prev != null) {
                            field.setInt(inv, prev);
                        }
                        inv.setChanged();
                        ItemStack now = inv.getItem(selectedSlot);
                        Valorant.LOGGER.info("[CURVEBALL] After scroll removal now holding: {} (empty={})",
                                now.getItem(), now.isEmpty());
                    }
                } catch (Exception e) {
                    Valorant.LOGGER.error("[CURVEBALL] Failed to remove curveball via packet: {}", e.toString());
                }
            }
        });
    }

    @Override
    public Type<RemoveCurveballPacket> type() {
        return TYPE;
    }
}
