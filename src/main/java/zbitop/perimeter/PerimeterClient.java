package zbitop.perimeter;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import zbitop.perimeter.screen.ModMenuTypes;
import zbitop.perimeter.screen.PerimeterScreen;

public class PerimeterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.PERIMETER_MENU, PerimeterScreen::new);
    }
}