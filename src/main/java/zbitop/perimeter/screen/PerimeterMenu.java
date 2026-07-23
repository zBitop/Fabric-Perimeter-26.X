package zbitop.perimeter.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import zbitop.perimeter.block.entity.PerimeterBlockEntity;

public class PerimeterMenu extends AbstractContainerMenu {

    private final PerimeterBlockEntity blockEntity;

    // Constructor del lado del CLIENTE: se llama cuando llega el paquete de "abrir menú" con el BlockPos
    public PerimeterMenu(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    // Constructor del lado del SERVIDOR: se llama directamente desde el bloque, ya con el block entity a mano
    public PerimeterMenu(int syncId, Inventory playerInventory, PerimeterBlockEntity blockEntity) {
        super(ModMenuTypes.PERIMETER_MENU, syncId);
        this.blockEntity = blockEntity;
    }

    private static PerimeterBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof PerimeterBlockEntity perimeterBE) {
            return perimeterBE;
        }
        throw new IllegalStateException("No se encontró PerimeterBlockEntity en " + pos);
    }

    public PerimeterBlockEntity getBlockEntityRef() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // por ahora simple; más adelante podemos chequear distancia al bloque
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}