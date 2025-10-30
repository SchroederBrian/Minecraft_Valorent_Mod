package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.client.lock.AgentLockState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncAgentLocksPacket(List<String> lockedA, List<String> lockedV) implements CustomPacketPayload {
    public static final Type<SyncAgentLocksPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_agent_locks"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAgentLocksPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.lockedA.size());
                for (String id : packet.lockedA) buf.writeUtf(id);
                buf.writeVarInt(packet.lockedV.size());
                for (String id : packet.lockedV) buf.writeUtf(id);
            },
            buf -> {
                int aCount = buf.readVarInt();
                List<String> a = new ArrayList<>(aCount);
                for (int i = 0; i < aCount; i++) a.add(buf.readUtf());
                int vCount = buf.readVarInt();
                List<String> v = new ArrayList<>(vCount);
                for (int i = 0; i < vCount; i++) v.add(buf.readUtf());
                return new SyncAgentLocksPacket(a, v);
            }
    );

    public static void handle(SyncAgentLocksPacket packet, IPayloadContext context) {
        // Client-side: update lock state
        AgentLockState.updateFromIds(packet.lockedA, packet.lockedV);
    }

    @Override
    public Type<SyncAgentLocksPacket> type() {
        return TYPE;
    }
}


