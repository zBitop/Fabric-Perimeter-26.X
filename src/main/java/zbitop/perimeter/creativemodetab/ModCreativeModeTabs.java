package zbitop.perimeter.creativemodetab;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import zbitop.perimeter.Perimeter;
import zbitop.perimeter.block.ModBlocks;
import zbitop.perimeter.item.ModItems;

public class ModCreativeModeTabs {
    public static final CreativeModeTab PERIMETER_ITEM_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, "perimeter_items"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PERIMETER_WAND))
                    .title(Component.translatable("creativemodetab.perimeter.perimeter_items"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.PERIMETER_WAND);
                    }).build());



    public static final CreativeModeTab PERIMETER_BLOCK_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, "perimeter_blocks"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.PERIMETER))
                    .title(Component.translatable("creativemodetab.perimeter.perimeter_blocks"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.PERIMETER);
                    }).build());


    public static void registerModCretiveModeTabs() {
        Perimeter.LOGGER.info("Registering Creative Mode Tabs for" + Perimeter.MOD_ID);
    }
}
