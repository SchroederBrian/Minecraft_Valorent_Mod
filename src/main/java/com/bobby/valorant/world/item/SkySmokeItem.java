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
		var mc = net.minecraft.client.Minecraft.getInstance();
		if (mc == null) return;
		mc.setScreen(new com.bobby.valorant.client.SkySmokeScreen());
	}
}


