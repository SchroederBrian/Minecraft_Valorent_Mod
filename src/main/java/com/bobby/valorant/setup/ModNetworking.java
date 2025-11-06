package com.bobby.valorant.setup;

import com.bobby.valorant.network.BuyAbilityRequestPacket;
import com.bobby.valorant.network.BuyRequestPacket;
import com.bobby.valorant.network.ChangeHotbarSlotPacket;
import com.bobby.valorant.network.DefuseProgressPacket;
import com.bobby.valorant.network.DefuseSpikePacket;
import com.bobby.valorant.network.DropWeaponPacket;
import com.bobby.valorant.network.EquipCurveballPacket;
import com.bobby.valorant.network.EquipFireWallPacket;
import com.bobby.valorant.network.EquipFireballPacket;
import com.bobby.valorant.network.EquipSpikePacket;
import com.bobby.valorant.network.GiveCurveballPacket;
import com.bobby.valorant.network.LockAgentRequestPacket;
import com.bobby.valorant.network.PickupWeaponPacket;
import com.bobby.valorant.network.PlantSpikePacket;
import com.bobby.valorant.network.PlantingProgressPacket;
import com.bobby.valorant.network.ReloadWeaponPacket;
import com.bobby.valorant.network.RemoveBlastPackPacket;
import com.bobby.valorant.network.RemoveCurveballPacket;
import com.bobby.valorant.network.RemoveFireWallPacket;
import com.bobby.valorant.network.RemoveFireballPacket;
import com.bobby.valorant.network.RemoveSkySmokePacket;
import com.bobby.valorant.network.RemoveStimBeaconPacket;
import com.bobby.valorant.network.SelectAgentC2SPacket;
import com.bobby.valorant.network.ShootGunPacket;
import com.bobby.valorant.network.OpenCalibrationPickPointS2CPacket;
import com.bobby.valorant.network.ShowTitleOverlayPacket;
import com.bobby.valorant.network.SubmitCalibrationPointC2SPacket;
import com.bobby.valorant.network.SyncSkySmokeCalibrationS2CPacket;
import com.bobby.valorant.network.SyncSkySmokeAreasS2CPacket;
import com.bobby.valorant.network.SyncSkySmokeRecordingPointsS2CPacket;
import com.bobby.valorant.network.SyncSkySmokeTransformS2CPacket;
import com.bobby.valorant.network.SyncAbilityStateS2CPacket;
import com.bobby.valorant.network.SyncAgentLocksPacket;
import com.bobby.valorant.network.SyncAgentS2CPacket;
import com.bobby.valorant.network.SyncCreditsPacket;
import com.bobby.valorant.network.SyncCurveballChargesPacket;
import com.bobby.valorant.network.SyncFireWallChargesPacket;
import com.bobby.valorant.network.SyncFireballChargesPacket;
import com.bobby.valorant.network.SyncReloadStatePacket;
import com.bobby.valorant.network.SyncKnifeAnimationStatePacket;
import com.bobby.valorant.network.KnifeAttackPacket;
import com.bobby.valorant.network.SyncRoundStatePacket;
import com.bobby.valorant.network.SyncUltimatePointsPacket;
import com.bobby.valorant.network.SyncWeaponAmmoPacket;
import com.bobby.valorant.network.ThrowCurveballPacket;
import com.bobby.valorant.network.TriggerFlashPacket;
import com.bobby.valorant.network.UseAbilityC2SPacket;

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
                .playToServer(BuyAbilityRequestPacket.TYPE, BuyAbilityRequestPacket.STREAM_CODEC, BuyAbilityRequestPacket::handle)
                .playToServer(EquipCurveballPacket.TYPE, EquipCurveballPacket.STREAM_CODEC, EquipCurveballPacket::handle)
                .playToServer(ShootGunPacket.TYPE, ShootGunPacket.STREAM_CODEC, ShootGunPacket::handle)
                .playToServer(KnifeAttackPacket.TYPE, KnifeAttackPacket.STREAM_CODEC, KnifeAttackPacket::handle)
                .playToServer(LockAgentRequestPacket.TYPE, LockAgentRequestPacket.STREAM_CODEC, LockAgentRequestPacket::handle)
                .playToServer(EquipFireballPacket.TYPE, EquipFireballPacket.STREAM_CODEC, EquipFireballPacket::handle)
                .playToServer(EquipFireWallPacket.TYPE, EquipFireWallPacket.STREAM_CODEC, EquipFireWallPacket::handle)
                .playToServer(RemoveFireWallPacket.TYPE, RemoveFireWallPacket.STREAM_CODEC, RemoveFireWallPacket::handle)
                .playToServer(RemoveFireballPacket.TYPE, RemoveFireballPacket.STREAM_CODEC, RemoveFireballPacket::handle)
                .playToServer(RemoveBlastPackPacket.TYPE, RemoveBlastPackPacket.STREAM_CODEC, RemoveBlastPackPacket::handle)
                .playToServer(RemoveStimBeaconPacket.TYPE, RemoveStimBeaconPacket.STREAM_CODEC, RemoveStimBeaconPacket::handle)
                .playToServer(RemoveSkySmokePacket.TYPE, RemoveSkySmokePacket.STREAM_CODEC, RemoveSkySmokePacket::handle)
                .playToServer(PlantSpikePacket.TYPE, PlantSpikePacket.STREAM_CODEC, PlantSpikePacket::handle)
                .playToServer(DefuseSpikePacket.TYPE, DefuseSpikePacket.STREAM_CODEC, DefuseSpikePacket::handle)
                .playToServer(EquipSpikePacket.TYPE, EquipSpikePacket.STREAM_CODEC, EquipSpikePacket::handle)
                .playToServer(ReloadWeaponPacket.TYPE, ReloadWeaponPacket.STREAM_CODEC, ReloadWeaponPacket::handle)
                .playToServer(SelectAgentC2SPacket.TYPE, SelectAgentC2SPacket.STREAM_CODEC, SelectAgentC2SPacket::handle)
                .playToServer(UseAbilityC2SPacket.TYPE, UseAbilityC2SPacket.STREAM_CODEC, UseAbilityC2SPacket::handle)
                .playToServer(DropWeaponPacket.TYPE, DropWeaponPacket.STREAM_CODEC, DropWeaponPacket::handle)
                .playToServer(PickupWeaponPacket.TYPE, PickupWeaponPacket.STREAM_CODEC, PickupWeaponPacket::handle)
                .playToClient(SyncWeaponAmmoPacket.TYPE, SyncWeaponAmmoPacket.STREAM_CODEC, SyncWeaponAmmoPacket::handle)
                .playToClient(SyncCurveballChargesPacket.TYPE, SyncCurveballChargesPacket.STREAM_CODEC, SyncCurveballChargesPacket::handle)
                .playToClient(SyncRoundStatePacket.TYPE, SyncRoundStatePacket.STREAM_CODEC, SyncRoundStatePacket::handle)
                .playToClient(SyncCreditsPacket.TYPE, SyncCreditsPacket.STREAM_CODEC, SyncCreditsPacket::handle)
                .playToClient(TriggerFlashPacket.TYPE, TriggerFlashPacket.STREAM_CODEC, TriggerFlashPacket::handle)
                .playToClient(SyncUltimatePointsPacket.TYPE, SyncUltimatePointsPacket.STREAM_CODEC, SyncUltimatePointsPacket::handle)
                .playToClient(SyncAgentLocksPacket.TYPE, SyncAgentLocksPacket.STREAM_CODEC, SyncAgentLocksPacket::handle)
                .playToClient(SyncFireballChargesPacket.TYPE, SyncFireballChargesPacket.STREAM_CODEC, SyncFireballChargesPacket::handle)
                .playToClient(SyncFireWallChargesPacket.TYPE, SyncFireWallChargesPacket.STREAM_CODEC, SyncFireWallChargesPacket::handle)
                .playToClient(SyncReloadStatePacket.TYPE, SyncReloadStatePacket.STREAM_CODEC, SyncReloadStatePacket::handle)
                .playToClient(SyncKnifeAnimationStatePacket.TYPE, SyncKnifeAnimationStatePacket.STREAM_CODEC, SyncKnifeAnimationStatePacket::handle)
                .playToClient(ShowTitleOverlayPacket.TYPE, ShowTitleOverlayPacket.STREAM_CODEC, ShowTitleOverlayPacket::handle)
				.playToClient(com.bobby.valorant.network.SyncSpawnAreasS2CPacket.TYPE, com.bobby.valorant.network.SyncSpawnAreasS2CPacket.STREAM_CODEC, com.bobby.valorant.network.SyncSpawnAreasS2CPacket::handle)
                .playToClient(SyncAgentS2CPacket.TYPE, SyncAgentS2CPacket.STREAM_CODEC, SyncAgentS2CPacket::handle)
                .playToClient(SyncAbilityStateS2CPacket.TYPE, SyncAbilityStateS2CPacket.STREAM_CODEC, SyncAbilityStateS2CPacket::handle)
                .playToClient(com.bobby.valorant.network.KillfeedMessageS2CPacket.TYPE, com.bobby.valorant.network.KillfeedMessageS2CPacket.STREAM_CODEC, com.bobby.valorant.network.KillfeedMessageS2CPacket::handle)
                .playToClient(SyncSkySmokeCalibrationS2CPacket.TYPE, SyncSkySmokeCalibrationS2CPacket.STREAM_CODEC, SyncSkySmokeCalibrationS2CPacket::handle)
                .playToClient(SyncSkySmokeAreasS2CPacket.TYPE, SyncSkySmokeAreasS2CPacket.STREAM_CODEC, SyncSkySmokeAreasS2CPacket::handle)
                .playToClient(SyncSkySmokeRecordingPointsS2CPacket.TYPE, SyncSkySmokeRecordingPointsS2CPacket.STREAM_CODEC, SyncSkySmokeRecordingPointsS2CPacket::handle)
                .playToClient(OpenCalibrationPickPointS2CPacket.TYPE, OpenCalibrationPickPointS2CPacket.STREAM_CODEC, OpenCalibrationPickPointS2CPacket::handle)
                .playToClient(SyncSkySmokeTransformS2CPacket.TYPE, SyncSkySmokeTransformS2CPacket.STREAM_CODEC, SyncSkySmokeTransformS2CPacket::handle)
                .playToClient(PlantingProgressPacket.TYPE, PlantingProgressPacket.STREAM_CODEC, (pkt, ctx) -> {})
				.playToClient(DefuseProgressPacket.TYPE, DefuseProgressPacket.STREAM_CODEC, (pkt, ctx) -> {})
				.playToServer(com.bobby.valorant.network.PlaceSkySmokesC2SPacket.TYPE, com.bobby.valorant.network.PlaceSkySmokesC2SPacket.STREAM_CODEC, com.bobby.valorant.network.PlaceSkySmokesC2SPacket::handle)
				.playToServer(SubmitCalibrationPointC2SPacket.TYPE, SubmitCalibrationPointC2SPacket.STREAM_CODEC, SubmitCalibrationPointC2SPacket::handle);
        // Register late (separate chain) to keep diff minimal
        event.registrar(NETWORK_VERSION)
                .playToClient(com.bobby.valorant.network.ApplyRecoilS2CPacket.TYPE, com.bobby.valorant.network.ApplyRecoilS2CPacket.STREAM_CODEC, com.bobby.valorant.network.ApplyRecoilS2CPacket::handle);
    }
}

