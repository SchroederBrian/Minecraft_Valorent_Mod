package com.bobby.valorant.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class SkySmokeItem extends Item {
	public SkySmokeItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide) {
			openMapScreen();
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}

	private static void openMapScreen() {
		try {
			// Use reflection to avoid direct client class reference
			Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
			Object mc = minecraftClass.getMethod("getInstance").invoke(null);
			
			Class<?> screenClass = Class.forName("com.bobby.valorant.client.SkySmokeScreen");
			Object screen = screenClass.getConstructor().newInstance();
			
			minecraftClass.getMethod("setScreen", Class.forName("net.minecraft.client.gui.screens.Screen")).invoke(mc, screen);
		} catch (Exception e) {
			// Handle reflection errors gracefully
			e.printStackTrace();
		}
	}
}


