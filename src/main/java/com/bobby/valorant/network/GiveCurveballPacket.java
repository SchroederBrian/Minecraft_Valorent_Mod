package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GiveCurveballPacket() implements CustomPacketPayload {
    public static final Type<GiveCurveballPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "give_curveball"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GiveCurveballPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                // No data to write
            },
            buf -> new GiveCurveballPacket());

    public static void handle(GiveCurveballPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            Inventory inventory = serverPlayer.getInventory();
            
            // Check if player already has a curveball
            boolean hasCurveball = false;
            for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.is(ModItems.CURVEBALL.get())) {
                    hasCurveball = true;
                    Valorant.LOGGER.info("[SERVER] Player already has curveball in slot {}", i);
                    break;
                }
            }
            
            if (!hasCurveball) {
                // Give player a curveball in the first empty hotbar slot
                ItemStack curveballStack = new ItemStack(ModItems.CURVEBALL.get());
                boolean given = false;
                
                for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                    if (inventory.getItem(i).isEmpty()) {
                        inventory.setItem(i, curveballStack);
                        Valorant.LOGGER.info("[SERVER] Gave curveball to player in slot {}", i);
                        given = true;
                        break;
                    }
                }
                
                if (!given) {
                    Valorant.LOGGER.warn("[SERVER] Could not give curveball - no empty hotbar slots");
                }
            }
        }
    }

    @Override
    public Type<GiveCurveballPacket> type() {
        return TYPE;
    }
}

