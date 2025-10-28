package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChangeHotbarSlotPacket(int slot) implements CustomPacketPayload {
    public static final Type<ChangeHotbarSlotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "change_hotbar_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeHotbarSlotPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVarInt(packet.slot()),
            buf -> new ChangeHotbarSlotPacket(buf.readVarInt()));

    public static void handle(ChangeHotbarSlotPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            // Use reflection to access the private selected field
            try {
                var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
                field.setAccessible(true);
                field.setInt(serverPlayer.getInventory(), packet.slot());
            } catch (Exception e) {
                // Fallback: silently fail if reflection doesn't work
            }
        }
    }

    @Override
    public Type<ChangeHotbarSlotPacket> type() {
        return TYPE;
    }
}

