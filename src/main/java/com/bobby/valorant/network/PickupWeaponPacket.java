package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.drop.DroppedWeaponStandEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PickupWeaponPacket(int entityId) implements CustomPacketPayload {
    public static final Type<PickupWeaponPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "pickup_weapon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PickupWeaponPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeInt(packet.entityId),
            buf -> new PickupWeaponPacket(buf.readInt())
    );

    public static void handle(PickupWeaponPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sp)) {
            return;
        }

        // Find the entity by ID
        var entity = sp.level().getEntity(packet.entityId);
        if (!(entity instanceof DroppedWeaponStandEntity stand)) {
            return;
        }

        // Perform the pickup
        stand.interact(sp, InteractionHand.MAIN_HAND);
    }

    @Override
    public Type<PickupWeaponPacket> type() {
        return TYPE;
    }
}
