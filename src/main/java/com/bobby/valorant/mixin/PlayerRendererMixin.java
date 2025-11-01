package com.bobby.valorant.mixin;

import com.bobby.valorant.Config;
import com.bobby.valorant.client.lock.PlayerAgentState;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void valorant$overridePlayerTexture(AbstractClientPlayer player, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!Boolean.TRUE.equals(Config.COMMON.agentSkinsEnabled.get())) return;
        Agent agent = PlayerAgentState.getAgentForPlayer(player);
        if (agent == null || agent == Agent.UNSELECTED) {
            String def = Config.COMMON.agentSkinsDefaultAgent.get();
            agent = Agent.byId(def);
        }
        if (agent != null && agent != Agent.UNSELECTED) {
            cir.setReturnValue(agent.getTextureLocation());
        }
    }
}


