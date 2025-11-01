package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.lock.PlayerAgentState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SyncAgentS2CPacket(UUID playerId, String agentId) implements CustomPacketPayload {
    public static final Type<SyncAgentS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_agent_selection"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAgentS2CPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SyncAgentS2CPacket decode(RegistryFriendlyByteBuf buf) {
            UUID uuid = buf.readUUID();
            String id = buf.readUtf();
            return new SyncAgentS2CPacket(uuid, id);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SyncAgentS2CPacket packet) {
            buf.writeUUID(packet.playerId);
            buf.writeUtf(packet.agentId);
        }
    };

    public static void handle(SyncAgentS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> PlayerAgentState.put(packet.playerId, packet.agentId));
    }

    @Override
    public Type<SyncAgentS2CPacket> type() { return TYPE; }
}


