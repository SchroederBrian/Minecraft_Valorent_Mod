package com.bobby.valorant.registry;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.entity.CurveballOrb;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntityTypes {
    private ModEntityTypes() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Valorant.MODID);
    private static final ResourceKey<EntityType<?>> CURVEBALL_ORB_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "curveball_orb"));

    public static final DeferredHolder<EntityType<?>, EntityType<CurveballOrb>> CURVEBALL_ORB = ENTITY_TYPES.register("curveball_orb",
            () -> EntityType.Builder.<CurveballOrb>of(CurveballOrb::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(CURVEBALL_ORB_KEY));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}

