package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.CurveballData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncCurveballChargesPacket(int charges) implements CustomPacketPayload {
    public static final Type<SyncCurveballChargesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_curveball_charges"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCurveballChargesPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVarInt(packet.charges()),
            buf -> new SyncCurveballChargesPacket(buf.readVarInt()));

    public static void handle(SyncCurveballChargesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                Valorant.LOGGER.info("[CHARGE SYNC] Received sync packet on {} with {} charges", 
                    player.level().isClientSide ? "CLIENT" : "SERVER", packet.charges());
                CurveballData.setCharges(player, packet.charges());
            }
        });
    }

    @Override
    public Type<SyncCurveballChargesPacket> type() {
        return TYPE;
    }
}

