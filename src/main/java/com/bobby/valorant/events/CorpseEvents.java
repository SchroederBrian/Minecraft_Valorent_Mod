package com.bobby.valorant.events;

import com.bobby.valorant.Config;
import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.AgentData;
import com.bobby.valorant.registry.ModEntityTypes;
import com.bobby.valorant.world.entity.CorpseEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.entity.EntitySpawnReason;

@EventBusSubscriber(modid = Valorant.MODID)
public final class CorpseEvents {
    private CorpseEvents() {}

    private static final java.util.Set<java.util.UUID> CORPSE_IDS = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) {
            return;
        }
        if (!Boolean.TRUE.equals(Config.COMMON.corpseEnabled.get())) {
            return;
        }
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }

        CorpseEntity corpse = ModEntityTypes.CORPSE.get().create(level, EntitySpawnReason.TRIGGERED);
        if (corpse == null) return;
        String agentId = AgentData.getSelectedAgent(victim).getId();
        corpse.setAgentId(agentId);
        corpse.setDeathYaw(victim.getYRot());
        corpse.setPos(victim.getX(), victim.getY(), victim.getZ());
        corpse.setYRot(victim.getYRot());
        corpse.setXRot(victim.getXRot());
        level.addFreshEntity(corpse);
        com.bobby.valorant.Valorant.LOGGER.info("[Corpse] Spawned corpse for {} at ({}, {}, {}) agent={} uuid={}"
                , victim.getGameProfile().getName()
                , String.format(java.util.Locale.ROOT, "%.2f", corpse.getX())
                , String.format(java.util.Locale.ROOT, "%.2f", corpse.getY())
                , String.format(java.util.Locale.ROOT, "%.2f", corpse.getZ())
                , agentId
                , corpse.getUUID());
        CORPSE_IDS.add(corpse.getUUID());
    }

    public static void despawnAll(ServerLevel level) {
        java.util.Iterator<java.util.UUID> it = CORPSE_IDS.iterator();
        while (it.hasNext()) {
            java.util.UUID id = it.next();
            var e = level.getEntity(id);
            if (e instanceof CorpseEntity corpse) {
                corpse.discard();
            }
            it.remove();
        }
    }
}


