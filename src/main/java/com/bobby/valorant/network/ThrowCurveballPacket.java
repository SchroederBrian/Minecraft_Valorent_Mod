package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.CurveballItem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ThrowCurveballPacket(CurveballItem.TurnDirection direction) implements CustomPacketPayload {
    public static final Type<ThrowCurveballPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "throw_curveball"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ThrowCurveballPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeEnum(packet.direction()),
            buf -> new ThrowCurveballPacket(buf.readEnum(CurveballItem.TurnDirection.class)));

    public static void handle(ThrowCurveballPacket packet, IPayloadContext context) {
        Player player = context.player();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack stack = serverPlayer.getMainHandItem();
        if (!(stack.getItem() instanceof CurveballItem curveball)) {
            return;
        }
        curveball.tryThrow(serverPlayer, InteractionHand.MAIN_HAND, stack, packet.direction());
    }

    @Override
    public Type<ThrowCurveballPacket> type() {
        return TYPE;
    }
}

