package com.bobby.valorant.network;

import com.bobby.valorant.player.AbilityEquipData;
import com.bobby.valorant.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveStimBeaconPacket() implements CustomPacketPayload {
    public static final Type<RemoveStimBeaconPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(com.bobby.valorant.Valorant.MODID, "remove_stim_beacon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveStimBeaconPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new RemoveStimBeaconPacket()
    );

    public static void handle(RemoveStimBeaconPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                var inv = sp.getInventory();
                try {
                    var field = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
                    field.setAccessible(true);
                    int selectedSlot = field.getInt(inv);
                    ItemStack held = inv.getItem(selectedSlot);
                    if (held.is(ModItems.STIMBEACONHAND.get())) {
                        ItemStack restore = AbilityEquipData.takeSaved(sp);
                        inv.setItem(selectedSlot, restore);
                        Integer prev = AbilityEquipData.takeSavedSelectedSlot(sp);
                        if (prev != null) {
                            field.setInt(inv, prev);
                        }
                        inv.setChanged();
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    @Override
    public Type<RemoveStimBeaconPacket> type() { return TYPE; }
}


