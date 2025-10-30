package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.player.SpectatorData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class RespawnServerEvents {
    private RespawnServerEvents() {}

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!SpectatorData.hasDeath(sp)) return;

        String dim = SpectatorData.dimId(sp);
        double x = SpectatorData.x(sp);
        double y = SpectatorData.y(sp);
        double z = SpectatorData.z(sp);

        // Only teleport if still in the same dimension
        String currentDim = ((ServerLevel) sp.level()).dimension().location().toString();
        if (currentDim.equals(dim) && sp.connection != null) {
            sp.connection.teleport(x, y, z, sp.getYRot(), sp.getXRot());
        }

        // Defer ability and vanish application to next server tick so vanilla respawn can't overwrite it
        sp.getServer().execute(() -> applySpectatorState(sp));
    }

    private static void applySpectatorState(ServerPlayer sp) {
        // Flight and invulnerability
        sp.getAbilities().mayfly = true;
        sp.getAbilities().flying = true;
        sp.onUpdateAbilities();
        sp.setInvulnerable(true);
        sp.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 60 * 60, 0, false, false));
        sp.setInvisible(true);
        sp.setDeltaMovement(0, 0, 0);
        sp.fallDistance = 0;

        // Ensure nametag hidden and no collisions using a scoreboard team
        Scoreboard sb = sp.getServer().getScoreboard();
        PlayerTeam team = sb.getPlayerTeam("VAL_SPECTATOR");
        if (team == null) {
            team = sb.addPlayerTeam("VAL_SPECTATOR");
            team.setSeeFriendlyInvisibles(false);
            team.setCollisionRule(PlayerTeam.CollisionRule.NEVER);
            team.setNameTagVisibility(PlayerTeam.Visibility.NEVER);
        }
        PlayerTeam existing = sb.getPlayersTeam(sp.getScoreboardName());
        if (existing != team) {
            if (existing != null) sb.removePlayerFromTeam(sp.getScoreboardName(), existing);
            sb.addPlayerToTeam(sp.getScoreboardName(), team);
        }
    }
}
