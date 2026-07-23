package zbitop.perimeter.block.entity;

import net.fabricmc.fabric.api.menu.v1.FabricMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;


public class PerimeterBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos>  {
    private int size = 5;


    public PerimeterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PERIMETER_BE, pos, state);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.max(1, Math.min(size, 500));
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Size", size);
        output.putBoolean("Mining", mining);
        output.putLong("Progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.size = input.getIntOr("Size", 5);
        this.mining = input.getBooleanOr("Mining", false);
        this.progress = input.getLongOr("Progress", 0L);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    private boolean mining = false;
    private long progress = 0;
    private static final int BLOCKS_PER_TICK = 8;

    public void startMining() {
        this.mining = true;
        this.progress = 0;
        setChanged();
    }

    public void tick(Level level, BlockPos originPos) {
        if (!mining) return;

        int height = originPos.getY() - level.getMinY();
        long totalBlocks = (long) size * size * height;

        for (int i = 0; i < BLOCKS_PER_TICK; i++) {
            if (progress >= totalBlocks) {
                mining = false;
                setChanged();
                return;
            }

            int x = (int) (progress % size);
            int z = (int) ((progress / size) % size);
            int y = (int) (progress / ((long) size * size));

            BlockPos target = originPos.offset(x, -(y + 1), z);
            BlockState targetState = level.getBlockState(target);

            if (!targetState.isAir() && targetState.getDestroySpeed(level, target) >= 0) {
                level.destroyBlock(target, true); // true = suelta los ítems al piso (por ahora)
            }

            progress++;
        }

        setChanged();
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return null;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return null;
    }
}