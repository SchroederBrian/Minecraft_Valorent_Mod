package com.bobby.valorant.compat.fancymenu;

import com.bobby.valorant.Valorant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class FancyMenuIntegration {
    private FancyMenuIntegration() {}

    public static void openCustomMenu(String screenId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        try {
            // Try to use FancyMenu API if available
            Class<?> fancyMenuApiClass = Class.forName("de.keksuccino.fancymenu.api.FancyMenuAPI");
            java.lang.reflect.Method getCustomScreenMethod = fancyMenuApiClass.getMethod("getCustomScreen", String.class);
            Screen screen = (Screen) getCustomScreenMethod.invoke(null, screenId);

            if (screen != null) {
                minecraft.setScreen(screen);
            } else {
                Valorant.LOGGER.warn("FancyMenu: Screen ID \"{}\" not found.", screenId);
            }
        } catch (ClassNotFoundException e) {
            Valorant.LOGGER.warn("FancyMenu not found, cannot open custom menu: {}", screenId);
        } catch (Exception e) {
            Valorant.LOGGER.warn("Failed to open FancyMenu screen \"{}\": {}", screenId, e.toString());
        }
    }
}
