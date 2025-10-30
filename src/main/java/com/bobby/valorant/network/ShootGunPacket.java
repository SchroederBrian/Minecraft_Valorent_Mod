package com.bobby.valorant.network;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.GunItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ShootGunPacket() implements CustomPacketPayload {
    public static final Type<ShootGunPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "shoot_gun"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShootGunPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new ShootGunPacket()
    );

    public static void handle(ShootGunPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sp)) {
            return;
        }
        // Prefer main hand; fallback to offhand
        ItemStack main = sp.getMainHandItem();
        if (main.getItem() instanceof GunItem gun) {
            gun.fire(sp, InteractionHand.MAIN_HAND, main);
            return;
        }
        ItemStack off = sp.getOffhandItem();
        if (off.getItem() instanceof GunItem gun) {
            gun.fire(sp, InteractionHand.OFF_HAND, off);
        }
    }

    @Override
    public Type<ShootGunPacket> type() {
        return TYPE;
    }
}


