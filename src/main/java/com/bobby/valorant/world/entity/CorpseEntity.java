package com.bobby.valorant.world.entity;

import javax.annotation.Nonnull;

import com.bobby.valorant.client.render.state.CorpseRenderState;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;

public class CorpseEntity extends ArmorStand {
    private static final EntityDataAccessor<String> DATA_AGENT_ID = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float>  DATA_DEATH_YAW = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);

    // Head X/Y/Z (°)
    private static final EntityDataAccessor<Float>  DATA_HEAD_X = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>  DATA_HEAD_Y = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>  DATA_HEAD_Z = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);

    // Right Arm X/Y/Z (°)
    private static final EntityDataAccessor<Float>  DATA_RARM_X = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>  DATA_RARM_Y = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>  DATA_RARM_Z = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);

    // Left Arm X/Y/Z (°)
    private static final EntityDataAccessor<Float>  DATA_LARM_X = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>  DATA_LARM_Y = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>  DATA_LARM_Z = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);

    // Right Leg Y/Z (°)
    private static final EntityDataAccessor<Float>  DATA_RLEG_Y = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>  DATA_RLEG_Z = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);

    // Left Leg Y/Z (°)
    private static final EntityDataAccessor<Float>  DATA_LLEG_Y = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>  DATA_LLEG_Z = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);

    private boolean posedInitialized = false;

    public CorpseEntity(EntityType<? extends CorpseEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(false);
        this.setNoBasePlate(true);
        this.setShowArms(false);
        this.setInvulnerable(true);
        this.setSilent(true);
    }

    @Override
    protected void defineSynchedData(@Nonnull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AGENT_ID, "");
        builder.define(DATA_DEATH_YAW, 0.0F);

        builder.define(DATA_HEAD_X, 0.0F);
        builder.define(DATA_HEAD_Y, 0.0F);
        builder.define(DATA_HEAD_Z, 0.0F);

        builder.define(DATA_RARM_X, 0.0F);
        builder.define(DATA_RARM_Y, 0.0F);
        builder.define(DATA_RARM_Z, 0.0F);

        builder.define(DATA_LARM_X, 0.0F);
        builder.define(DATA_LARM_Y, 0.0F);
        builder.define(DATA_LARM_Z, 0.0F);

        builder.define(DATA_RLEG_Y, 0.0F);
        builder.define(DATA_RLEG_Z, 0.0F);

        builder.define(DATA_LLEG_Y, 0.0F);
        builder.define(DATA_LLEG_Z, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();

        // Nur einmal serverseitig initialisieren, dann über EntityData syncen.
        if (!this.level().isClientSide && !posedInitialized) {
            RandomSource rnd = RandomSource.create(this.getUUID().getLeastSignificantBits());

            // Helper
            float headMax = 10.0F; // ±10° für Kopf X/Y/Z
            float armMax  =  5.0F; // ±5°  für Arme Y/Z
            float legMax  = 10.0F; // ±10° für Beine Y/Z

            this.entityData.set(DATA_HEAD_X, randRangeDeg(rnd, headMax));
            this.entityData.set(DATA_HEAD_Y, randRangeDeg(rnd, headMax));
            this.entityData.set(DATA_HEAD_Z, randRangeDeg(rnd, headMax));

            this.entityData.set(DATA_RARM_Y, randRangeDeg(rnd, armMax));
            this.entityData.set(DATA_RARM_Z, randRangeDeg(rnd, armMax));

            this.entityData.set(DATA_LARM_Y, randRangeDeg(rnd, armMax));
            this.entityData.set(DATA_LARM_Z, randRangeDeg(rnd, armMax));

            this.entityData.set(DATA_RLEG_Y, randRangeDeg(rnd, legMax));
            this.entityData.set(DATA_RLEG_Z, randRangeDeg(rnd, legMax));

            this.entityData.set(DATA_LLEG_Y, randRangeDeg(rnd, legMax));
            this.entityData.set(DATA_LLEG_Z, randRangeDeg(rnd, legMax));

            posedInitialized = true;
        }
    }

    private static float randRangeDeg(RandomSource r, float maxAbsDeg) {
        // Uniform in [-max, +max]
        return (r.nextFloat() * 2.0F - 1.0F) * maxAbsDeg;
    }

    public void setAgentId(String agentId) {
        this.entityData.set(DATA_AGENT_ID, agentId == null ? "" : agentId);
    }
    public String getAgentId() {
        return this.entityData.get(DATA_AGENT_ID);
    }

    public void setDeathYaw(float yaw) {
        this.entityData.set(DATA_DEATH_YAW, yaw);
    }
    public float getDeathYaw() {
        return this.entityData.get(DATA_DEATH_YAW);
    }

    // Getter für RenderState (°):
    public float getHeadX() { return this.entityData.get(DATA_HEAD_X); }
    public float getHeadY() { return this.entityData.get(DATA_HEAD_Y); }
    public float getHeadZ() { return this.entityData.get(DATA_HEAD_Z); }

    public float getRArmY() { return this.entityData.get(DATA_RARM_Y); }
    public float getRArmZ() { return this.entityData.get(DATA_RARM_Z); }

    public float getLArmY() { return this.entityData.get(DATA_LARM_Y); }
    public float getLArmZ() { return this.entityData.get(DATA_LARM_Z); }

    public float getRLegY() { return this.entityData.get(DATA_RLEG_Y); }
    public float getRLegZ() { return this.entityData.get(DATA_RLEG_Z); }

    public float getLLegY() { return this.entityData.get(DATA_LLEG_Y); }
    public float getLLegZ() { return this.entityData.get(DATA_LLEG_Z); }

    public CorpseRenderState createLivingEntityRenderState() {
        return new CorpseRenderState();
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}