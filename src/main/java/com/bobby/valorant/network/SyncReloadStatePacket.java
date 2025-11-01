package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.ReloadStateData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncReloadStatePacket(boolean isReloading, int ticksRemaining, int totalTicks) implements CustomPacketPayload {
    public static final Type<SyncReloadStatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_reload_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncReloadStatePacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeBoolean(packet.isReloading);
            buf.writeVarInt(packet.ticksRemaining);
            buf.writeVarInt(packet.totalTicks);
        },
        buf -> new SyncReloadStatePacket(buf.readBoolean(), buf.readVarInt(), buf.readVarInt())
    );

    public static void handle(SyncReloadStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Update client-side reload state data
            if (packet.isReloading) {
                // Manually set the reload state for client
                CompoundTag persistent = context.player().getPersistentData();
                CompoundTag reloadTag = persistent.getCompound("ValorantReloadState").orElseGet(() -> {
                    CompoundTag created = new CompoundTag();
                    persistent.put("ValorantReloadState", created);
                    return created;
                });
                reloadTag.putBoolean("IsReloading", true);
                reloadTag.putInt("TicksRemaining", packet.ticksRemaining);
                reloadTag.putInt("TotalTicks", packet.totalTicks);
            } else {
                ReloadStateData.cancelReload(context.player());
            }
        });
    }

    @Override
    public Type<SyncReloadStatePacket> type() {
        return TYPE;
    }
}
