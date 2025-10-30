package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.FireballData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncFireballChargesPacket(int charges) implements CustomPacketPayload {
    public static final Type<SyncFireballChargesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_fireball_charges"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFireballChargesPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> buf.writeVarInt(packet.charges),
        buf -> new SyncFireballChargesPacket(buf.readVarInt())
    );

    public static void handle(SyncFireballChargesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            FireballData.setCharges(context.player(), packet.charges);
        });
    }

    @Override
    public Type<SyncFireballChargesPacket> type() {
        return TYPE;
    }
}
