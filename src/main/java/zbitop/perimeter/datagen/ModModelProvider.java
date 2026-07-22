package zbitop.perimeter.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import zbitop.perimeter.block.ModBlocks;
import zbitop.perimeter.item.ModItems;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        blockModelGenerators.createTrivialCube(ModBlocks.PERIMETER);

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(ModItems.PERIMETER_WAND, ModelTemplates.FLAT_ITEM);
    }
}
