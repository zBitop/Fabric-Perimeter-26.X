package zbitop.perimeter.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import zbitop.perimeter.Perimeter;

public record PerimeterSizePayload(BlockPos pos, int size) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PerimeterSizePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, "perimeter_size"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PerimeterSizePayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PerimeterSizePayload::pos,
                    StreamCodec.of(RegistryFriendlyByteBuf::writeInt, RegistryFriendlyByteBuf::readInt), PerimeterSizePayload::size,
                    PerimeterSizePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}