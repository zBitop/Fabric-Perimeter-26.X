package zbitop.perimeter.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import zbitop.perimeter.block.ValuableBlocks;
import zbitop.perimeter.screen.PerimeterMenu;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


public class PerimeterBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos>  {
    private int size = 5;

    // Almacenamiento "infinito": no son ItemStacks reales, solo un contador por tipo de ítem.
    // Así no hay riesgo de desbordar ni de perder ítems por cofres llenos durante un AFK largo.
    private final Map<Item, Long> storage = new LinkedHashMap<>();

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

    public Map<Item, Long> getStorageView() {
        return storage;
    }

    private void collect(Item item, long amount) {
        if (amount <= 0) return;
        storage.merge(item, amount, Long::sum);
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Size", size);
        output.putBoolean("Mining", mining);
        output.putLong("Progress", progress);
        output.putString("Storage", serializeStorage());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.size = input.getIntOr("Size", 5);
        this.mining = input.getBooleanOr("Mining", false);
        this.progress = input.getLongOr("Progress", 0L);
        deserializeStorage(input.getStringOr("Storage", ""));
    }

    // Formato simple: "modid:item=cantidad;modid:item=cantidad;..."
    private String serializeStorage() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Item, Long> entry : storage.entrySet()) {
            Identifier id = BuiltInRegistries.ITEM.getKey(entry.getKey());
            if (sb.length() > 0) sb.append(';');
            sb.append(id).append('=').append(entry.getValue());
        }
        return sb.toString();
    }

    private void deserializeStorage(String raw) {
        storage.clear();
        if (raw == null || raw.isEmpty()) return;
        for (String part : raw.split(";")) {
            int eq = part.indexOf('=');
            if (eq <= 0) continue;
            try {
                Identifier id = Identifier.parse(part.substring(0, eq));
                long amount = Long.parseLong(part.substring(eq + 1));
                Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM.get(id);
                if (holder.isPresent() && amount > 0) {
                    storage.put(holder.get().value(), amount);
                }
            } catch (Exception ignored) {
                // línea corrupta, la saltamos en vez de romper la carga del bloque
            }
        }
    }

    /**
     * Le entrega todo lo almacenado al jugador (inventario, y lo que no entre cae al piso
     * en la posición del bloque). Vacía el almacenamiento al terminar.
     */
    public void withdrawAllTo(Player player) {
        for (Map.Entry<Item, Long> entry : storage.entrySet()) {
            Item item = entry.getKey();
            long remaining = entry.getValue();
            int maxStack = new ItemStack(item).getMaxStackSize();

            while (remaining > 0) {
                int stackSize = (int) Math.min(remaining, maxStack);
                ItemStack stack = new ItemStack(item, stackSize);

                if (!player.getInventory().add(stack)) {
                    // no entró (o entró parcial): lo que sobre se tira en el piso del bloque
                    if (!stack.isEmpty() && this.level != null) {
                        net.minecraft.world.Containers.dropItemStack(
                                this.level,
                                this.worldPosition.getX() + 0.5,
                                this.worldPosition.getY() + 0.5,
                                this.worldPosition.getZ() + 0.5,
                                stack
                        );
                    }
                }

                remaining -= stackSize;
            }
        }
        storage.clear();
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    private boolean mining = false;
    private long progress = 0;

    public void startMining() {
        this.mining = true;
        this.progress = 0;
        setChanged();
    }

    public boolean isMining() {
        return mining;
    }

    /**
     * Mientras el bloque está "en espera" (sin minar), muestra dos líneas de partículas
     * saliendo del bloque: llamas naranjas hacia +X y llamas azules (soul fire) hacia +Z.
     * Esas son exactamente las direcciones en las que va a crecer el perímetro al confirmar,
     * así se puede ver hacia dónde va a excavar sin necesidad de F3.
     */
    private static final int INDICATOR_LENGTH = 6;

    private void showDirectionIndicator(Level level, BlockPos originPos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (level.getGameTime() % 10 != 0) return; // cada medio segundo, no hace falta todos los ticks

        for (int i = 1; i <= INDICATOR_LENGTH; i++) {
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    originPos.getX() + i + 0.5, originPos.getY() + 0.3, originPos.getZ() + 0.5,
                    1, 0, 0, 0, 0
            );
            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    originPos.getX() + 0.5, originPos.getY() + 0.3, originPos.getZ() + i + 0.5,
                    1, 0, 0, 0, 0
            );
        }
    }


    /**
     * Progreso del minado actual, de 0 a 100. Se usa para sincronizar la barra
     * de progreso al cliente a través de un DataSlot en PerimeterMenu.
     */
    public int getProgressPercent() {
        if (level == null || size <= 0) return 0;

        int height = worldPosition.getY() - level.getMinY();
        long totalBlocks = (long) size * size * height;
        if (totalBlocks <= 0) return mining ? 0 : 100;

        long pct = (progress * 100) / totalBlocks;
        return (int) Math.min(100, Math.max(0, pct));
    }

    public void tick(Level level, BlockPos originPos) {
        if (!mining) {
            showDirectionIndicator(level, originPos);
            return;
        }

        int height = originPos.getY() - level.getMinY();
        long totalBlocks = (long) size * size * height;

        long tickStart = System.nanoTime();
        int sinceTimeCheck = 0;

        while (true) {
            if (progress >= totalBlocks) {
                mining = false;
                break;
            }

            int x = (int) (progress % size);
            int z = (int) ((progress / size) % size);
            int y = (int) (progress / ((long) size * size));

            BlockPos target = originPos.offset(x, -(y + 1), z);
            BlockState targetState = level.getBlockState(target);

            if (!targetState.isAir()) {
                if (!targetState.getFluidState().isEmpty()) {
                    // Agua o lava justo en la zona de minado: la vaciamos directo, sin generar drop.
                    level.setBlock(target, AIR_STATE, RAW_SET_FLAGS);
                } else if (targetState.getDestroySpeed(level, target) >= 0) {
                    if (ValuableBlocks.isValuable(targetState.getBlock())) {
                        // "silk touch" simulado: guardamos el ítem del bloque tal cual, no su loot table
                        collect(targetState.getBlock().asItem(), 1);
                    }
                    // setBlock "crudo": sin sonido, sin partículas de rotura, sin update de vecinos.
                    // Mucho más barato que destroyBlock() a este volumen de bloques.
                    level.setBlock(target, AIR_STATE, RAW_SET_FLAGS);
                }
                // si no es líquido y destroySpeed < 0 (ej: bedrock), lo dejamos intacto
            }

            // Sellar líquidos solo tiene sentido en el borde del área: en el interior,
            // el "vecino con líquido" es una celda que vamos a minar nosotros mismos
            // un instante después, así que revisarla ahí es trabajo tirado a la basura.
            boolean isEdge = (x == 0 || x == size - 1 || z == 0 || z == size - 1);
            if (isEdge) {
                sealAdjacentLiquids(level, target);
            }

            progress++;

            // Revisamos el reloj cada 256 bloques en vez de en cada uno: System.nanoTime()
            // también tiene un costo, y llamarlo por bloque sería desperdiciar presupuesto.
            if (++sinceTimeCheck >= 256) {
                sinceTimeCheck = 0;
                if (System.nanoTime() - tickStart >= TICK_TIME_BUDGET_NS) {
                    break;
                }
            }
        }

        setChanged();
    }

    private static final Direction[] SEAL_DIRECTIONS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();

    // flag 2 = UPDATE_CLIENTS (avisa a los jugadores para que se vea el cambio)
    // Sin flag 1 (UPDATE_NEIGHBORS): no dispara updates de física/redstone/forma en cada bloque vecino.
    // Es justamente ese trabajo en cascada el que hacía lento esto a escala de miles de bloques.
    private static final int RAW_SET_FLAGS = 2;

    // Presupuesto de tiempo de CPU por tick dedicado a minar. Subilo si tu servidor aguanta
    // más (ej. 6_000_000 = 6ms) o bajalo si notás lag en otras cosas mientras mina.
    private static final long TICK_TIME_BUDGET_NS = 4_000_000; // 4ms de 50ms disponibles por tick

    /**
     * Tapa con cobblestone cualquier líquido pegado a los costados del bloque recién minado,
     * para que bolsas de agua/lava en las paredes no se filtren hacia el pozo ya excavado.
     * Solo se llama para bloques del borde del área (ver tick()).
     */
    private void sealAdjacentLiquids(Level level, BlockPos minedPos) {
        for (Direction dir : SEAL_DIRECTIONS) {
            BlockPos neighbor = minedPos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (!neighborState.getFluidState().isEmpty() && neighborState.getBlock() != Blocks.COBBLESTONE) {
                level.setBlock(neighbor, COBBLESTONE_STATE, RAW_SET_FLAGS);
            }
        }
    }

    private static final BlockState COBBLESTONE_STATE = Blocks.COBBLESTONE.defaultBlockState();

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.getBlockPos();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PerimeterMenu(containerId, inventory, this);
    }
}