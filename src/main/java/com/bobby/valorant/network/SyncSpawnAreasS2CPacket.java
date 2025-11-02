package com.bobby.valorant.network;

import java.util.ArrayList;
import java.util.List;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.spawn.SpawnArea;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSpawnAreasS2CPacket(ResourceLocation dimension,
                                      int yA, List<BlockPos> vertsA,
                                      int yV, List<BlockPos> vertsV,
                                      int ySiteA, List<BlockPos> vertsSiteA,
                                      int ySiteB, List<BlockPos> vertsSiteB,
                                      int ySiteC, List<BlockPos> vertsSiteC) implements CustomPacketPayload {
    public static final Type<SyncSpawnAreasS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "sync_spawn_areas"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSpawnAreasS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeResourceLocation(pkt.dimension);
                writeArea(buf, pkt.yA, pkt.vertsA);
                writeArea(buf, pkt.yV, pkt.vertsV);
                writeArea(buf, pkt.ySiteA, pkt.vertsSiteA);
                writeArea(buf, pkt.ySiteB, pkt.vertsSiteB);
                writeArea(buf, pkt.ySiteC, pkt.vertsSiteC);
            },
            buf -> new SyncSpawnAreasS2CPacket(
                    buf.readResourceLocation(),
                    readY(buf), readVerts(buf),
                    readY(buf), readVerts(buf),
                    readY(buf), readVerts(buf),
                    readY(buf), readVerts(buf),
                    readY(buf), readVerts(buf))
    );

    private static void writeArea(RegistryFriendlyByteBuf buf, int y, List<BlockPos> verts) {
        buf.writeVarInt(y);
        if (verts == null) { buf.writeVarInt(0); return; }
        buf.writeVarInt(verts.size());
        for (BlockPos p : verts) {
            buf.writeVarInt(p.getX());
            buf.writeVarInt(p.getZ());
        }
    }

    private static int readY(RegistryFriendlyByteBuf buf) { return buf.readVarInt(); }
    private static List<BlockPos> readVerts(RegistryFriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<BlockPos> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int x = buf.readVarInt();
            int z = buf.readVarInt();
            list.add(new BlockPos(x, 0, z));
        }
        return list;
    }

    public static SyncSpawnAreasS2CPacket of(ResourceLocation dim, SpawnArea a, SpawnArea v, SpawnArea siteA, SpawnArea siteB, SpawnArea siteC) {
        int ya = a != null ? a.y : 0;
        int yv = v != null ? v.y : 0;
        List<BlockPos> va = a != null ? a.vertices : java.util.List.of();
        List<BlockPos> vv = v != null ? v.vertices : java.util.List.of();
        int ysa = siteA != null ? siteA.y : 0;
        int ysb = siteB != null ? siteB.y : 0;
        int ysc = siteC != null ? siteC.y : 0;
        List<BlockPos> vsa = siteA != null ? siteA.vertices : java.util.List.of();
        List<BlockPos> vsb = siteB != null ? siteB.vertices : java.util.List.of();
        List<BlockPos> vsc = siteC != null ? siteC.vertices : java.util.List.of();
        return new SyncSpawnAreasS2CPacket(dim, ya, va, yv, vv, ysa, vsa, ysb, vsb, ysc, vsc);
    }

    public static void handle(SyncSpawnAreasS2CPacket pkt, IPayloadContext ctx) {
        com.bobby.valorant.spawn.client.SpawnAreaClientState.update(pkt.dimension, pkt.yA, pkt.vertsA, pkt.yV, pkt.vertsV,
                pkt.ySiteA, pkt.vertsSiteA, pkt.ySiteB, pkt.vertsSiteB, pkt.ySiteC, pkt.vertsSiteC);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}


