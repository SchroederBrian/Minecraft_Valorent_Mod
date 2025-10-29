package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncCreditsPacket(int credits) implements CustomPacketPayload {
    public static final Type<SyncCreditsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_credits"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCreditsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeVarInt(p.credits()),
            buf -> new SyncCreditsPacket(buf.readVarInt())
    );

    public static void handle(SyncCreditsPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return;
            CompoundTag root = player.getPersistentData();
            CompoundTag econ = root.getCompound("ValorantEconomy").orElseGet(() -> {
                CompoundTag created = new CompoundTag();
                root.put("ValorantEconomy", created);
                return created;
            });
            econ.putInt("Credits", Math.max(0, packet.credits()));
        });
    }

    @Override
    public Type<SyncCreditsPacket> type() { return TYPE; }
}


