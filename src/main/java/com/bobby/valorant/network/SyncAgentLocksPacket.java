package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.lock.PlayerAgentState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record SyncAgentLocksPacket(Map<UUID, String> playerAgents) implements CustomPacketPayload {
    public static final Type<SyncAgentLocksPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_agent_locks"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAgentLocksPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SyncAgentLocksPacket decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            Map<UUID, String> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                map.put(buf.readUUID(), buf.readUtf());
            }
            return new SyncAgentLocksPacket(map);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SyncAgentLocksPacket packet) {
            buf.writeVarInt(packet.playerAgents.size());
            packet.playerAgents.forEach((uuid, agentId) -> {
                buf.writeUUID(uuid);
                buf.writeUtf(agentId);
            });
        }
    };

    public static void handle(SyncAgentLocksPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerAgentState.update(packet.playerAgents);
        });
    }

    @Override
    public Type<SyncAgentLocksPacket> type() {
        return TYPE;
    }
}


