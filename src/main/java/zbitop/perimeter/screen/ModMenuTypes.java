package zbitop.perimeter.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import zbitop.perimeter.Perimeter;

public class ModMenuTypes {

    public static final MenuType<PerimeterMenu> PERIMETER_MENU =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, "perimeter_menu"),
                    new ExtendedMenuType<>(PerimeterMenu::new, BlockPos.STREAM_CODEC)
            );

    public static void register() {
        // fuerza la carga de la clase
    }
}