package com.bobby.valorant.registry;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.entity.CurveballOrb;
import com.bobby.valorant.world.entity.FireballEntity;
import com.bobby.valorant.world.entity.PlantedSpikeEntity;

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
    private static final ResourceKey<EntityType<?>> FIREBALL_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "fireball"));
    private static final ResourceKey<EntityType<?>> PLANTED_SPIKE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Valorant.MODID, "plantedspike"));
    public static final DeferredHolder<EntityType<?>, EntityType<CurveballOrb>> CURVEBALL_ORB = ENTITY_TYPES.register("curveball_orb",
            () -> EntityType.Builder.<CurveballOrb>of(CurveballOrb::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(CURVEBALL_ORB_KEY));

    public static final DeferredHolder<EntityType<?>, EntityType<FireballEntity>> FIREBALL = ENTITY_TYPES.register("fireball",
            () -> EntityType.Builder.<FireballEntity>of(FireballEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(FIREBALL_KEY));

    public static final DeferredHolder<EntityType<?>, EntityType<PlantedSpikeEntity>> PLANTED_SPIKE = ENTITY_TYPES.register("plantedspike",
            () -> EntityType.Builder.<PlantedSpikeEntity>of(PlantedSpikeEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.8F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(PLANTED_SPIKE_KEY));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}

