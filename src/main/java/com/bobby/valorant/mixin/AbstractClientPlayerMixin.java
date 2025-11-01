package com.bobby.valorant.mixin;

import com.bobby.valorant.Config;
import com.bobby.valorant.client.lock.PlayerAgentState;
import com.bobby.valorant.world.agent.Agent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    // 1.21.8: AbstractClientPlayer#getSkin() returns PlayerSkin. Replace returned skin's base texture via a reflective copy.
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void valorant$overrideSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        if (!Boolean.TRUE.equals(Config.COMMON.agentSkinsEnabled.get())) return;
        AbstractClientPlayer self = (AbstractClientPlayer)(Object)this;
        Agent agent = PlayerAgentState.getAgentForPlayer(self);
        if (agent == null || agent == Agent.UNSELECTED) {
            String def = Config.COMMON.agentSkinsDefaultAgent.get();
            agent = Agent.byId(def);
        }
        if (agent == null || agent == Agent.UNSELECTED) return;
        PlayerSkin original = cir.getReturnValue();
        if (original == null) return;
        ResourceLocation newSkin = agent.getTextureLocation();
        try {
            Class<?> cls = original.getClass();
            // If PlayerSkin is a record, rebuild with modified ResourceLocation components
            if (cls.isRecord()) {
                var components = cls.getRecordComponents();
                Object[] args = new Object[components.length];
                for (int i = 0; i < components.length; i++) {
                    var comp = components[i];
                    Object value = comp.getAccessor().invoke(original);
                    String name = comp.getName();
                    String lname = name == null ? "" : name.toLowerCase();
                    if (comp.getType() == ResourceLocation.class) {
                        boolean isCapeish = lname.contains("cape") || lname.contains("elytra");
                        if (isCapeish && Boolean.TRUE.equals(Config.COMMON.agentSkinsDisableCapes.get())) {
                            value = null; // remove cape/elytra
                        } else if (!isCapeish) {
                            value = newSkin; // apply base skin
                        }
                    } else if (comp.getType() == boolean.class || comp.getType() == Boolean.class) {
                        if (Boolean.TRUE.equals(Config.COMMON.agentSkinsDisableCapes.get()) && (lname.contains("cape") || lname.contains("elytra"))) {
                            value = false;
                        }
                    } else if (comp.getType() == java.util.Optional.class) {
                        if (Boolean.TRUE.equals(Config.COMMON.agentSkinsDisableCapes.get()) && (lname.contains("cape") || lname.contains("elytra"))) {
                            value = java.util.Optional.empty();
                        }
                    }
                    args[i] = value;
                }
                var paramTypes = java.util.Arrays.stream(components).map(java.lang.reflect.RecordComponent::getType).toArray(Class[]::new);
                var ctor = cls.getDeclaredConstructor(paramTypes);
                ctor.setAccessible(true);
                Object rebuilt = ctor.newInstance(args);
                cir.setReturnValue((PlayerSkin)rebuilt);
                return;
            }
            // Non-record fallback: best-effort clone via default constructor, else leave original
        } catch (Throwable ignored) {
            // Fall through to leave original if reflection fails
        }
    }
}


