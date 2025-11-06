package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ApplyRecoilS2CPacket(float pitchDeltaDegrees) implements CustomPacketPayload {
    public static final Type<ApplyRecoilS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "apply_recoil"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ApplyRecoilS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> buf.writeFloat(pkt.pitchDeltaDegrees()),
            buf -> new ApplyRecoilS2CPacket(buf.readFloat())
    );

    @Override
    public Type<ApplyRecoilS2CPacket> type() {
        return TYPE;
    }

    public static void handle(ApplyRecoilS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // Move camera up by reducing pitch
            float current = mc.player.getXRot();
            float next = current - packet.pitchDeltaDegrees();
            // Clamp to Minecraft pitch limits
            if (next > 90.0f) next = 90.0f;
            if (next < -90.0f) next = -90.0f;
            mc.player.setXRot(next);
        });
    }
}


