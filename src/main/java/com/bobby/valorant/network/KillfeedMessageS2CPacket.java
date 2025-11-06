package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KillfeedMessageS2CPacket(String killerName, String victimName, String weaponItemId, String killerAgentId, String victimAgentId)
        implements CustomPacketPayload {

    public static final Type<KillfeedMessageS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "killfeed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, KillfeedMessageS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeUtf(p.killerName());
                buf.writeUtf(p.victimName());
                buf.writeUtf(p.weaponItemId());
                buf.writeUtf(p.killerAgentId());
                buf.writeUtf(p.victimAgentId());
            },
            buf -> new KillfeedMessageS2CPacket(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(KillfeedMessageS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            com.bobby.valorant.world.agent.Agent killerAgent = com.bobby.valorant.world.agent.Agent.byId(packet.killerAgentId());
            com.bobby.valorant.world.agent.Agent victimAgent = com.bobby.valorant.world.agent.Agent.byId(packet.victimAgentId());
            com.bobby.valorant.client.hud.KillfeedOverlay.pushId(packet.killerName(), packet.victimName(), packet.weaponItemId(), killerAgent, victimAgent);
        });
    }
}


