package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.ability.ClientAbilityState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncAbilityStateS2CPacket(int cCharges, int qCharges, int eCharges, int ultPoints) implements CustomPacketPayload {
    public static final Type<SyncAbilityStateS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_ability_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAbilityStateS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeVarInt(pkt.cCharges);
                buf.writeVarInt(pkt.qCharges);
                buf.writeVarInt(pkt.eCharges);
                buf.writeVarInt(pkt.ultPoints);
            },
            buf -> new SyncAbilityStateS2CPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncAbilityStateS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientAbilityState.update(packet.cCharges, packet.qCharges, packet.eCharges, packet.ultPoints));
    }
}


