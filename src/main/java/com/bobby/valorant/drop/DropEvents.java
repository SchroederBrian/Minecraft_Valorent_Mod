package com.bobby.valorant.drop;

import java.util.List;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

@EventBusSubscriber(modid = Valorant.MODID)
public final class DropEvents {
    private DropEvents() {}

    // Disabled - drops are now handled via G key press instead of Q (ItemTossEvent)
    // @SubscribeEvent
    // public static void onItemToss(ItemTossEvent event) {
    //     ... drop logic moved to DropWeaponPacket handler
    // }

    private static boolean isDroppableUsingConfig(ItemStack stack) {
        Object raw = Config.COMMON.droppableWhitelist.get();
        if (!(raw instanceof List<?> list) || list.isEmpty()) return false;
        for (Object o : list) {
            if (!(o instanceof String s)) continue;
            s = s.trim();
            if (s.isEmpty()) continue;
            if (s.startsWith("#")) {
                String tagStr = s.substring(1);
                ResourceLocation id = ResourceLocation.tryParse(tagStr);
                if (id == null) continue;
                TagKey<Item> key = ItemTags.create(id);
                if (stack.is(key)) return true;
            } else {
                ResourceLocation id = ResourceLocation.tryParse(s);
                if (id == null) continue;
                ResourceLocation stackId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (id.equals(stackId)) return true;
            }
        }
        return false;
    }
}


