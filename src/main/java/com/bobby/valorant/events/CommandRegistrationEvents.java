package com.bobby.valorant.events;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.command.ValorantCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Valorant.MODID)
public final class CommandRegistrationEvents {
    private CommandRegistrationEvents() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ValorantCommand.register(event.getDispatcher(), event.getBuildContext());
    }
}
