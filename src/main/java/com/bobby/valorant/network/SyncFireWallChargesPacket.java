package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.FireWallData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncFireWallChargesPacket(int charges) implements CustomPacketPayload {
    public static final Type<SyncFireWallChargesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_firewall_charges"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFireWallChargesPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> buf.writeVarInt(packet.charges),
        buf -> new SyncFireWallChargesPacket(buf.readVarInt())
    );

    public static void handle(SyncFireWallChargesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            FireWallData.setCharges(context.player(), packet.charges);
        });
    }

    @Override
    public Type<SyncFireWallChargesPacket> type() {
        return TYPE;
    }
}
