package zbitop.perimeter.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import zbitop.perimeter.Perimeter;

import java.util.function.Function;

public class ModItems {
    public static final Item PERIMETER_WAND = registerItem("perimeter_wand", Item::new);




    private static Item registerItem(String name, Function<Item.Properties, Item> function) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, name),
                function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, name)))));
    }

    public static void registerModItems() {
        Perimeter.LOGGER.info("Registering Mod Items for" + Perimeter.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(PERIMETER_WAND);
        });
    }

}
