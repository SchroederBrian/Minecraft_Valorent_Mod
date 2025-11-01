package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.player.AbilityStateData;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectAgentC2SPacket(String agentId) implements CustomPacketPayload {
    public static final Type<SelectAgentC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "select_agent"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectAgentC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUtf(packet.agentId()),
            buf -> new SelectAgentC2SPacket(buf.readUtf())
    );

    public static void handle(SelectAgentC2SPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sp)) return;

        Agent agent = Agent.byId(packet.agentId());
        if (agent == Agent.UNSELECTED) {
            return; // reject unknown ids
        }

        // Persist selection
        AgentData.setSelectedAgent(sp, agent);

        // Broadcast to all players (including self)
        SyncAgentS2CPacket sync = new SyncAgentS2CPacket(sp.getUUID(), agent.getId());
        var server = sp.getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(p -> PacketDistributor.sendToPlayer(p, sync));
        }

        // Initialize ability state for new agent and sync to self
        AbilityStateData.initForAgent(sp, agent);
        var set = com.bobby.valorant.ability.Abilities.getForAgent(agent);
        int c = AbilityStateData.getCharges(sp, set.c());
        int q = AbilityStateData.getCharges(sp, set.q());
        int e = AbilityStateData.getCharges(sp, set.e());
        int x = AbilityStateData.getUltPoints(sp);
        PacketDistributor.sendToPlayer(sp, new SyncAbilityStateS2CPacket(c, q, e, x));
    }

    @Override
    public Type<SelectAgentC2SPacket> type() { return TYPE; }
}


