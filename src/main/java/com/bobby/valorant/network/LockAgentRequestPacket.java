package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.round.AgentLockManager;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

public record LockAgentRequestPacket(String agentId) implements CustomPacketPayload {
    public static final Type<LockAgentRequestPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "lock_agent"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LockAgentRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUtf(packet.agentId()),
            buf -> new LockAgentRequestPacket(buf.readUtf())
    );

    public static void handle(LockAgentRequestPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Agent agent = Agent.byId(packet.agentId());
        AgentLockManager manager = AgentLockManager.get(serverPlayer.getServer());
        boolean ok = manager.tryLock(serverPlayer, agent);
        if (ok) {
            AgentData.setSelectedAgent(serverPlayer, agent);
            // Broadcast updated locks to all players
            var lockedA = new ArrayList<String>();
            var lockedV = new ArrayList<String>();
            manager.getLockedA().forEach(a -> lockedA.add(a.getId()));
            manager.getLockedV().forEach(a -> lockedV.add(a.getId()));
            SyncAgentLocksPacket sync = new SyncAgentLocksPacket(lockedA, lockedV);
            serverPlayer.getServer().getPlayerList().getPlayers().forEach(p ->
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, sync)
            );
        } else {
            serverPlayer.displayClientMessage(net.minecraft.network.chat.Component.literal("Agent already locked by your team"), true);
        }
    }

    @Override
    public Type<LockAgentRequestPacket> type() {
        return TYPE;
    }
}


