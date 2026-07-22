package zbitop.perimeter;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zbitop.perimeter.block.ModBlocks;
import zbitop.perimeter.creativemodetab.ModCreativeModeTabs;
import zbitop.perimeter.item.ModItems;

public class Perimeter implements ModInitializer {
	public static final String MOD_ID = "perimeter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModCreativeModeTabs.registerModCretiveModeTabs();

		ModItems.registerModItems();
		ModBlocks.registerModBlock();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
