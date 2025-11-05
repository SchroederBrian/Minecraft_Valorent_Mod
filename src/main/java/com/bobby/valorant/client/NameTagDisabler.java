// src/main/java/com/bobby/valorant/client/NameTagDisabler.java
package com.bobby.valorant.client;

import com.bobby.valorant.Valorant;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@EventBusSubscriber(modid = Valorant.MODID, value = Dist.CLIENT)
public final class NameTagDisabler {

    private NameTagDisabler() {}

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent.CanRender e) {
        if (e.getEntity() instanceof Player) {
            disableNameTagReflective(e);
        }
    }

    /**
     * Versucht in Reihenfolge:
     * 1) setCanRender(TriState.FALSE)
     * 2) setCanceled(true)
     * 3) setResult(Result.DENY)
     */
    private static void disableNameTagReflective(Object event) {
        // 1) TriState-API: e.setCanRender(TriState.FALSE)
        try {
            Class<?> triState = Class.forName("net.neoforged.neoforge.common.util.TriState");
            Field fFalse = triState.getField("FALSE");
            Object FALSE = fFalse.get(null);
            Method m = event.getClass().getMethod("setCanRender", triState);
            m.invoke(event, FALSE);
            return;
        } catch (Throwable ignored) { /* nächste Strategie */ }

        // 2) Cancel-API: e.setCanceled(true)
        try {
            Method m = event.getClass().getMethod("setCanceled", boolean.class);
            m.invoke(event, true);
            return;
        } catch (Throwable ignored) { /* nächste Strategie */ }

        // 3) Result-API: e.setResult(Result.DENY)
        try {
            Class<?> resultCls = Class.forName("net.neoforged.bus.api.Event$Result");
            Field fDeny = resultCls.getField("DENY");
            Object DENY = fDeny.get(null);
            Method m = event.getClass().getMethod("setResult", resultCls);
            m.invoke(event, DENY);
        } catch (Throwable ignored) {
            // Fallback: nichts weiter möglich
        }
    }
}
