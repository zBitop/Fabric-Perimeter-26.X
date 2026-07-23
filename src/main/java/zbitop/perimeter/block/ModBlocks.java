package zbitop.perimeter.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import zbitop.perimeter.Perimeter;

public class ModBlocks {

    public static final Block PERIMETER = registerBlockWithItem("perimeter",
            settings -> new PerimeterBlock(settings.strength(3.5f)));

    private static Block registerBlockWithItem(String name,
                                               java.util.function.Function<BlockBehaviour.Properties, Block> factory) {

        ResourceKey<Block> blockKey = keyOf(Registries.BLOCK, name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        ResourceKey<Item> itemKey = keyOf(Registries.ITEM, name);
        Item.Properties itemSettings = new Item.Properties().setId(itemKey);
        Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, itemSettings));

        return block;
    }

    private static <T> ResourceKey<T> keyOf(net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<T>> registry, String name) {
        return ResourceKey.create(registry, Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, name));
    }

    public static void register() {
        // fuerza la carga de la clase para que los static final se registren
    }
}