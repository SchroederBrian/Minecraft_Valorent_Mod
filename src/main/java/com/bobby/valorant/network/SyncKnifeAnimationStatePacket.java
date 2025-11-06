package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.KnifeAnimationStateData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncKnifeAnimationStatePacket(boolean isAnimating, KnifeAnimationStateData.AnimationType animationType, int ticksRemaining, int totalTicks) implements CustomPacketPayload {
    public static final Type<SyncKnifeAnimationStatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_knife_animation_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncKnifeAnimationStatePacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeBoolean(packet.isAnimating);
            buf.writeVarInt(packet.animationType.ordinal());
            buf.writeVarInt(packet.ticksRemaining);
            buf.writeVarInt(packet.totalTicks);
        },
        buf -> new SyncKnifeAnimationStatePacket(
            buf.readBoolean(),
            KnifeAnimationStateData.AnimationType.values()[buf.readVarInt()],
            buf.readVarInt(),
            buf.readVarInt()
        )
    );

    public static void handle(SyncKnifeAnimationStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Update client-side knife animation state data
            if (packet.isAnimating) {
                // Manually set the animation state for client
                CompoundTag persistent = context.player().getPersistentData();
                CompoundTag animationTag = persistent.getCompound("ValorantKnifeAnimationState").orElseGet(() -> {
                    CompoundTag created = new CompoundTag();
                    persistent.put("ValorantKnifeAnimationState", created);
                    return created;
                });
                animationTag.putBoolean("IsAnimating", true);
                animationTag.putInt("TicksRemaining", packet.ticksRemaining);
                animationTag.putInt("TotalTicks", packet.totalTicks);
                animationTag.putInt("AnimationType", packet.animationType.ordinal());
            } else {
                KnifeAnimationStateData.cancelAnimation(context.player());
            }
        });
    }

    @Override
    public Type<SyncKnifeAnimationStatePacket> type() {
        return TYPE;
    }
}
