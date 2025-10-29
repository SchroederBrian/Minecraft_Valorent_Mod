package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.economy.EconomyData;
import com.bobby.valorant.economy.ShopItem;
import com.bobby.valorant.round.RoundController;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BuyRequestPacket(String itemName, boolean sell) implements CustomPacketPayload {
    public static final Type<BuyRequestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "buy_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeUtf(p.itemName());
                buf.writeBoolean(p.sell());
            },
            buf -> new BuyRequestPacket(buf.readUtf(), buf.readBoolean())
    );

    public static void handle(BuyRequestPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = (net.minecraft.server.level.ServerPlayer) ctx.player();
            if (player == null) return;
            ShopItem item;
            try { item = ShopItem.valueOf(packet.itemName()); } catch (IllegalArgumentException e) { return; }
            RoundController rc = RoundController.get((net.minecraft.server.level.ServerLevel) player.level());
            boolean isBuy = rc.phase() == RoundController.Phase.BUY;
            if (packet.sell()) {
                EconomyData.trySell(player, item, 1, isBuy);
            } else {
                EconomyData.tryBuy(player, item, 1, isBuy);
            }
        });
    }

    @Override
    public Type<BuyRequestPacket> type() { return TYPE; }
}


