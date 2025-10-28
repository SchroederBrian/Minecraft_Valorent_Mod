package com.bobby.valorant.registry;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.CurveballItem;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Valorant.MODID);

    // Use registerItem so DeferredRegister injects the id into Item.Properties before construction
    public static final DeferredItem<Item> CURVEBALL = ITEMS.registerItem("curveball", props -> new CurveballItem(props.stacksTo(1)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}

