package com.bobby.valorant.compat.fancymenu;

import com.bobby.valorant.Valorant;
import java.lang.reflect.Method;

public final class FancyMenuIntegration {
    private static final String[] CANDIDATE_CLASSES = new String[] {
        "de.keksuccino.fancymenu.api.FancyMenuAPI",
        "de.keksuccino.fancymenu.api.FancyMenuApi",
        "de.keksuccino.fancymenu.FancyMenu"
    };
    private static final String[] CANDIDATE_METHODS = new String[] {
        "openMenuByIdentifier", "openMenu", "openCustomGui", "openLayout", "openByIdentifier"
    };

    private FancyMenuIntegration() {}

    public static boolean openCustomGui(String identifier) {
        try {
            for (String className : CANDIDATE_CLASSES) {
                Class<?> cls = Class.forName(className);
                for (String methodName : CANDIDATE_METHODS) {
                    // Try String only
                    Method m = findMethod(cls, methodName, String.class);
                    if (m != null) return invokeBoolean(m, null, identifier);
                }
                for (String methodName : CANDIDATE_METHODS) {
                    // Try (String, boolean)
                    Method m = findMethod(cls, methodName, String.class, boolean.class);
                    if (m != null) return invokeBoolean(m, null, identifier, true);
                }
            }
        } catch (Throwable t) {
            Valorant.LOGGER.warn("FancyMenu integration failed: {}", t.toString());
        }
        return false;
    }

    private static Method findMethod(Class<?> cls, String name, Class<?>... params) {
        try { return cls.getMethod(name, params); } catch (Exception ignored) { return null; }
    }

    private static boolean invokeBoolean(Method m, Object target, Object... args) {
        try {
            Object r = m.invoke(target, args);
            return !(r instanceof Boolean) || (Boolean) r; // treat void or non-boolean as success
        } catch (Exception e) {
            Valorant.LOGGER.debug("FancyMenu call {} failed: {}", m.getName(), e.toString());
            return false;
        }
    }
}
