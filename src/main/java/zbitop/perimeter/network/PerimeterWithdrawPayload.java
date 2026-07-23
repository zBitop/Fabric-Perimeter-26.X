package zbitop.perimeter.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import zbitop.perimeter.Perimeter;

public record PerimeterWithdrawPayload(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PerimeterWithdrawPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Perimeter.MOD_ID, "perimeter_withdraw"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PerimeterWithdrawPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PerimeterWithdrawPayload::pos,
                    PerimeterWithdrawPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}