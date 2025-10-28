package com.bobby.valorant.setup;

import com.bobby.valorant.network.ChangeHotbarSlotPacket;
import com.bobby.valorant.network.GiveCurveballPacket;
import com.bobby.valorant.network.RemoveCurveballPacket;
import com.bobby.valorant.network.SyncCurveballChargesPacket;
import com.bobby.valorant.network.ThrowCurveballPacket;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetworking {
    private ModNetworking() {}

    private static final String NETWORK_VERSION = "1";

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar(NETWORK_VERSION)
                .playToServer(ThrowCurveballPacket.TYPE, ThrowCurveballPacket.STREAM_CODEC, ThrowCurveballPacket::handle)
                .playToServer(ChangeHotbarSlotPacket.TYPE, ChangeHotbarSlotPacket.STREAM_CODEC, ChangeHotbarSlotPacket::handle)
                .playToServer(GiveCurveballPacket.TYPE, GiveCurveballPacket.STREAM_CODEC, GiveCurveballPacket::handle)
                .playToServer(RemoveCurveballPacket.TYPE, RemoveCurveballPacket.STREAM_CODEC, RemoveCurveballPacket::handle)
                .playToClient(SyncCurveballChargesPacket.TYPE, SyncCurveballChargesPacket.STREAM_CODEC, SyncCurveballChargesPacket::handle);
    }
}

