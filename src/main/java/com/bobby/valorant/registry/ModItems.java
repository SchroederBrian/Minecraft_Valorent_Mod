package com.bobby.valorant.registry;

import com.bobby.valorant.Valorant;
import com.bobby.valorant.world.item.ClassicPistolItem;
import com.bobby.valorant.world.item.CurveballItem;
import com.bobby.valorant.world.item.ValorRifleItem;
import com.bobby.valorant.world.item.GhostPistolItem;
import com.bobby.valorant.world.item.KnifeItem;
import com.bobby.valorant.world.item.FireballItem;
import com.bobby.valorant.world.item.SpikeItem;
import com.bobby.valorant.world.item.DefuserItem;
import com.bobby.valorant.world.item.StimBeaconHandItem;
import com.bobby.valorant.world.item.PlacedStimBeacon;
import com.bobby.valorant.world.item.VandalRifleItem;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Valorant.MODID);

    // Use registerItem so DeferredRegister injects the id into Item.Properties before construction
    public static final DeferredItem<Item> CURVEBALL = ITEMS.registerItem("curveball", props -> new CurveballItem(props.stacksTo(1)));
    public static final DeferredItem<Item> FIREBALL = ITEMS.registerItem("fireball", props -> new FireballItem(props.stacksTo(1)));
    public static final DeferredItem<Item> VALOR_RIFLE = ITEMS.registerItem("valor_rifle", props -> new ValorRifleItem(props.stacksTo(1)));
    public static final DeferredItem<Item> GHOST = ITEMS.registerItem("ghost", props -> new GhostPistolItem(props.stacksTo(1)));
    public static final DeferredItem<Item> KNIFE = ITEMS.registerItem("knife", props -> new KnifeItem(props.stacksTo(1)));
    public static final DeferredItem<Item> CLASSIC = ITEMS.registerItem("classic", props -> new ClassicPistolItem(props.stacksTo(1)));
    public static final DeferredItem<Item> PLANTEDSPIKE = ITEMS.registerItem("plantedspike", props -> new Item(props.stacksTo(1)));
    public static final DeferredItem<Item> SPIKE = ITEMS.registerItem("spike", props -> new SpikeItem(props.stacksTo(1)));
    public static final DeferredItem<Item> DEFUSER = ITEMS.registerItem("defuser", props -> new DefuserItem(props.stacksTo(1)));
    public static final DeferredItem<Item> STIMBEACONHAND = ITEMS.registerItem("stimbeaconhand", props -> new StimBeaconHandItem(props.stacksTo(1)));
    public static final DeferredItem<Item> PLACEDSTIMBEACON = ITEMS.registerItem("placedstimbeacon", props -> new PlacedStimBeacon(props.stacksTo(1)));
    public static final DeferredItem<Item> WALLSEGMENT = ITEMS.registerItem("wallsegment", props -> new Item(props.stacksTo(1)));
    public static final DeferredItem<Item> VANDAL_RIFLE = ITEMS.registerItem("vandal", props -> new VandalRifleItem(props.stacksTo(1)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}

