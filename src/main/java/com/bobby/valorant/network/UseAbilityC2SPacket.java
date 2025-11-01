package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.ability.Ability;
import com.bobby.valorant.ability.AbilityUseContext;
import com.bobby.valorant.ability.Abilities;
import com.bobby.valorant.player.AbilityStateData;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseAbilityC2SPacket(Ability.Slot slot) implements CustomPacketPayload {
    public static final Type<UseAbilityC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "use_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UseAbilityC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> buf.writeEnum(pkt.slot),
            buf -> new UseAbilityC2SPacket(buf.readEnum(Ability.Slot.class))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(UseAbilityC2SPacket packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sp)) return;
        Agent agent = AgentData.getSelectedAgent(sp);
        var set = Abilities.getForAgent(agent);
        Ability ability = switch (packet.slot) {
            case C -> set.c();
            case Q -> set.q();
            case E -> set.e();
            case X -> set.x();
        };
        if (ability == null) return;

        if (ability.slot() == Ability.Slot.X) {
            int points = AbilityStateData.getUltPoints(sp);
            if (ability.ultCost() <= 0 || points < ability.ultCost()) return;
            AbilityStateData.setUltPoints(sp, points - ability.ultCost());
        }
        // For other abilities (C/Q/E), charges are consumed when the item is used, not on equip

        net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) sp.level();
        ability.effect().execute(sp, AbilityUseContext.of(level));

        int c = AbilityStateData.getCharges(sp, set.c());
        int q = AbilityStateData.getCharges(sp, set.q());
        int e = AbilityStateData.getCharges(sp, set.e());
        int x = AbilityStateData.getUltPoints(sp);
        PacketDistributor.sendToPlayer(sp, new SyncAbilityStateS2CPacket(c, q, e, x));
    }
}


