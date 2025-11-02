package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.ability.Ability;
import com.bobby.valorant.ability.Abilities;
import com.bobby.valorant.ability.AbilitySet;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.round.RoundController;
import com.bobby.valorant.world.agent.Agent;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BuyAbilityRequestPacket(Ability.Slot slot, boolean sell) implements CustomPacketPayload {
    public static final Type<BuyAbilityRequestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "buy_ability_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyAbilityRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeEnum(p.slot());
                buf.writeBoolean(p.sell());
            },
            buf -> new BuyAbilityRequestPacket(buf.readEnum(Ability.Slot.class), buf.readBoolean())
    );

    @Override
    public Type<BuyAbilityRequestPacket> type() { return TYPE; }

    public static void handle(BuyAbilityRequestPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            ServerLevel level = (ServerLevel) sp.level();
            RoundController rc = RoundController.get(level);
            boolean isBuy = rc.phase() == RoundController.Phase.BUY && rc.isInSpawn(sp);
            int roundId = rc.getCurrentRoundId();

            Agent agent = AgentData.getSelectedAgent(sp);
            AbilitySet set = Abilities.getForAgent(agent);
            Ability ability = switch (packet.slot()) { case C -> set.c(); case Q -> set.q(); case E -> set.e(); default -> null; };
            if (ability == null) return;

            // Resolve config for price/max/purchasable
            String aId = agent.getId();
            String slotKey = packet.slot().name().toLowerCase();
            var cfg = (com.electronwill.nightconfig.core.Config) com.bobby.valorant.Config.COMMON.abilityShop.get();
            int price = getCfgInt(cfg, aId + "." + slotKey + ".price", 0);
            int max = getCfgInt(cfg, aId + "." + slotKey + ".max", ability.baseCharges());
            boolean purchasable = getCfgBool(cfg, aId + "." + slotKey + ".purchasable", false);

            if (!purchasable && !packet.sell()) {
                com.bobby.valorant.util.SoundManager.playBuyFailureSound(sp);
                return;
            }

            boolean ok = packet.sell()
                    ? com.bobby.valorant.economy.EconomyData.trySellAbility(sp, ability, price, roundId, isBuy)
                    : com.bobby.valorant.economy.EconomyData.tryBuyAbility(sp, ability, price, max, roundId, isBuy);
            if (ok) com.bobby.valorant.util.SoundManager.playBuySuccessSound(sp);
            else com.bobby.valorant.util.SoundManager.playBuyFailureSound(sp);
        });
    }

    private static int getCfgInt(com.electronwill.nightconfig.core.Config cfg, String key, int def) {
        Object v = cfg.get(key);
        return v instanceof Number ? ((Number) v).intValue() : def;
    }

    private static boolean getCfgBool(com.electronwill.nightconfig.core.Config cfg, String key, boolean def) {
        Object v = cfg.get(key);
        return v instanceof Boolean ? (Boolean) v : def;
    }
}


