package zbitop.perimeter;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zbitop.perimeter.block.ModBlocks;
import zbitop.perimeter.block.entity.ModBlockEntities;
import zbitop.perimeter.screen.ModMenuTypes;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.level.block.entity.BlockEntity;
import zbitop.perimeter.block.entity.PerimeterBlockEntity;
import zbitop.perimeter.network.PerimeterSizePayload;
import zbitop.perimeter.network.PerimeterWithdrawPayload;


public class Perimeter implements ModInitializer {
	public static final String MOD_ID = "perimeter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModMenuTypes.register();

		ModBlocks.register();
		ModBlockEntities.register();


		PayloadTypeRegistry.serverboundPlay().register(PerimeterSizePayload.TYPE, PerimeterSizePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PerimeterWithdrawPayload.TYPE, PerimeterWithdrawPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(PerimeterSizePayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				BlockEntity be = context.player().level().getBlockEntity(payload.pos());
				if (be instanceof PerimeterBlockEntity perimeterBE) {
					perimeterBE.setSize(payload.size());
					perimeterBE.startMining();
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(PerimeterWithdrawPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				BlockEntity be = context.player().level().getBlockEntity(payload.pos());
				if (be instanceof PerimeterBlockEntity perimeterBE) {
					perimeterBE.withdrawAllTo(context.player());
				}
			});
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}