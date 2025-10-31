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
import com.bobby.valorant.network.LockAgentRequestPacket;
import com.bobby.valorant.network.SyncAgentLocksPacket;
import com.bobby.valorant.network.ShootGunPacket;
import com.bobby.valorant.network.EquipFireballPacket;
import com.bobby.valorant.network.SyncFireballChargesPacket;
import com.bobby.valorant.network.PlantSpikePacket;
import com.bobby.valorant.network.PlantingProgressPacket;
import com.bobby.valorant.network.ReloadWeaponPacket;
import com.bobby.valorant.network.SyncWeaponAmmoPacket;
import com.bobby.valorant.network.DefuseSpikePacket;
import com.bobby.valorant.network.DefuseProgressPacket;
import com.bobby.valorant.network.EquipSpikePacket;
import com.bobby.valorant.network.ShowTitleOverlayPacket;

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
                .playToServer(ShootGunPacket.TYPE, ShootGunPacket.STREAM_CODEC, ShootGunPacket::handle)
                .playToServer(LockAgentRequestPacket.TYPE, LockAgentRequestPacket.STREAM_CODEC, LockAgentRequestPacket::handle)
                .playToServer(EquipFireballPacket.TYPE, EquipFireballPacket.STREAM_CODEC, EquipFireballPacket::handle)
                .playToServer(PlantSpikePacket.TYPE, PlantSpikePacket.STREAM_CODEC, PlantSpikePacket::handle)
                .playToServer(DefuseSpikePacket.TYPE, DefuseSpikePacket.STREAM_CODEC, DefuseSpikePacket::handle)
                .playToServer(EquipSpikePacket.TYPE, EquipSpikePacket.STREAM_CODEC, EquipSpikePacket::handle)
                .playToServer(ReloadWeaponPacket.TYPE, ReloadWeaponPacket.STREAM_CODEC, ReloadWeaponPacket::handle)
                .playToClient(SyncWeaponAmmoPacket.TYPE, SyncWeaponAmmoPacket.STREAM_CODEC, SyncWeaponAmmoPacket::handle)
                .playToClient(SyncCurveballChargesPacket.TYPE, SyncCurveballChargesPacket.STREAM_CODEC, SyncCurveballChargesPacket::handle)
                .playToClient(SyncRoundStatePacket.TYPE, SyncRoundStatePacket.STREAM_CODEC, SyncRoundStatePacket::handle)
                .playToClient(SyncCreditsPacket.TYPE, SyncCreditsPacket.STREAM_CODEC, SyncCreditsPacket::handle)
                .playToClient(TriggerFlashPacket.TYPE, TriggerFlashPacket.STREAM_CODEC, TriggerFlashPacket::handle)
                .playToClient(SyncUltimatePointsPacket.TYPE, SyncUltimatePointsPacket.STREAM_CODEC, SyncUltimatePointsPacket::handle)
                .playToClient(SyncAgentLocksPacket.TYPE, SyncAgentLocksPacket.STREAM_CODEC, SyncAgentLocksPacket::handle)
                .playToClient(SyncFireballChargesPacket.TYPE, SyncFireballChargesPacket.STREAM_CODEC, SyncFireballChargesPacket::handle)
                .playToClient(ShowTitleOverlayPacket.TYPE, ShowTitleOverlayPacket.STREAM_CODEC, ShowTitleOverlayPacket::handle)
                .playToClient(PlantingProgressPacket.TYPE, PlantingProgressPacket.STREAM_CODEC, (pkt, ctx) -> {})
                .playToClient(DefuseProgressPacket.TYPE, DefuseProgressPacket.STREAM_CODEC, (pkt, ctx) -> {});
    }
}

