package com.bobby.valorant.setup;

import com.bobby.valorant.network.ChangeHotbarSlotPacket;
import com.bobby.valorant.network.GiveCurveballPacket;
import com.bobby.valorant.network.RemoveCurveballPacket;
import com.bobby.valorant.network.SyncCurveballChargesPacket;
import com.bobby.valorant.network.SyncRoundStatePacket;
import com.bobby.valorant.network.SyncCreditsPacket;
import com.bobby.valorant.network.ThrowCurveballPacket;
import com.bobby.valorant.network.BuyRequestPacket;
import com.bobby.valorant.network.EquipCurveballPacket;
import com.bobby.valorant.network.TriggerFlashPacket;
import com.bobby.valorant.network.SyncUltimatePointsPacket;

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
                .playToServer(BuyRequestPacket.TYPE, BuyRequestPacket.STREAM_CODEC, BuyRequestPacket::handle)
                .playToServer(EquipCurveballPacket.TYPE, EquipCurveballPacket.STREAM_CODEC, EquipCurveballPacket::handle)
                .playToClient(SyncCurveballChargesPacket.TYPE, SyncCurveballChargesPacket.STREAM_CODEC, SyncCurveballChargesPacket::handle)
                .playToClient(SyncRoundStatePacket.TYPE, SyncRoundStatePacket.STREAM_CODEC, SyncRoundStatePacket::handle)
                .playToClient(SyncCreditsPacket.TYPE, SyncCreditsPacket.STREAM_CODEC, SyncCreditsPacket::handle)
                .playToClient(TriggerFlashPacket.TYPE, TriggerFlashPacket.STREAM_CODEC, TriggerFlashPacket::handle)
                .playToClient(SyncUltimatePointsPacket.TYPE, SyncUltimatePointsPacket.STREAM_CODEC, SyncUltimatePointsPacket::handle);
    }
}

