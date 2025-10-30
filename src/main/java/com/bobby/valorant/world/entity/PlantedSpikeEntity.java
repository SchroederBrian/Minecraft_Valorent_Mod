package com.bobby.valorant.world.entity;

import com.bobby.valorant.registry.ModEntityTypes;
import com.bobby.valorant.registry.ModItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class PlantedSpikeEntity extends ThrowableItemProjectile {
    public PlantedSpikeEntity(EntityType<? extends PlantedSpikeEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public PlantedSpikeEntity(Level level) {
        super(ModEntityTypes.PLANTED_SPIKE.get(), level);
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.PLANTEDSPIKE.get();
    }

    @Override
    public void tick() {
        super.tick();
        // Keep it stationary
        setDeltaMovement(0.0D, 0.0D, 0.0D);
    }
}


