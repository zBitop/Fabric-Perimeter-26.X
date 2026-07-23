package zbitop.perimeter.block.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import zbitop.perimeter.Perimeter;
import zbitop.perimeter.block.ModBlocks;

public class ModBlockEntities {

    public static final BlockEntityType<PerimeterBlockEntity> PERIMETER_BE =
            register("perimeter", PerimeterBlockEntity::new, ModBlocks.PERIMETER);

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
            Block... blocks) {

        Identifier id = Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, name);
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id,
                FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build()
        );
    }

    public static void register() {
        // fuerza la carga de la clase
    }
}