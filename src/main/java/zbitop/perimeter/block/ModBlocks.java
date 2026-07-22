package zbitop.perimeter.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import zbitop.perimeter.Perimeter;

import java.lang.reflect.Parameter;
import java.util.function.Function;


public class ModBlocks {
    public static final Block PERIMETER = registerBlock("perimeter_block",
            properties -> new Block(properties.strength(2f)
                    .requiresCorrectToolForDrops().sound(SoundType.IRON)));

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> function) {
        Block toRegister = function.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, name))));
        registerModBlockItem(name, toRegister);
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, name), toRegister);
    }

    private static void registerModBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, name),
                new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix()
                        .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, name)))));
    }

    public static void registerModBlock() {
        Perimeter.LOGGER.info("Registering Mod Blocks for" + Perimeter.MOD_ID);
    }
}
