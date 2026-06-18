package com.mjzaymi.etherealvoid.block.entity;

import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import com.mjzaymi.etherealvoid.screen.PoolMonitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PoolMonitorBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2);

    private static final int INPUT_SLOT = 0;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 78;

    public PoolMonitorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.POOL_MONITOR_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> PoolMonitorBlockEntity.this.progress;
                    case 1 -> PoolMonitorBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> PoolMonitorBlockEntity.this.progress = pValue;
                    case 1 -> PoolMonitorBlockEntity.this.maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ethereal_void.pool_monitor");
    }

    public boolean isInStructure() {
        return CuboidStructure.findFromWall(level, worldPosition).isPresent();
    }

    public ReactionPoolBlockEntity getPoolBlockEntity() {
        Optional<CuboidStructure> structure = CuboidStructure.findFromWall(level, worldPosition);
        if (structure.isEmpty()) return null;
        BlockEntity be = level.getBlockEntity(structure.get().min());
        if (!(be instanceof ReactionPoolBlockEntity pool)) return null;
        return pool;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PoolMonitorMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("pool_monitor.progress", progress);

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("pool_monitor.progress");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pLevel.isClientSide) return;
        if (!(pLevel instanceof ServerLevel serverLevel)) return;
        ItemStack inputItem = itemHandler.getStackInSlot(INPUT_SLOT);
        if (inputItem.isEmpty()) return;
        ReactionPoolBlockEntity blockEntity = getPoolBlockEntity();
        if (blockEntity==null) return;
        CuboidStructure structure = blockEntity.getStructure();
        if (structure==null) return;
        itemHandler.extractItem(INPUT_SLOT, Integer.MAX_VALUE, false);
        BlockPos min = structure.min().offset(1 ,1 ,1);
        BlockPos max = structure.max().offset(-1, 0, -1).atY(min.getY());
        spawnItemRandomlyInArea(serverLevel, min, max, inputItem);
        /*if(hasRecipe()) {
            increaseCraftingProgress();
            setChanged(pLevel, pPos, pState);

            if(hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }*/
    }

    public void spawnItemRandomlyInArea(ServerLevel level, BlockPos min, BlockPos max, ItemStack stack) {
        if (stack.isEmpty()) return;
        RandomSource random = level.getRandom();

        double padding = 0.5;
        double minX = min.getX() + padding;
        double maxX = (max.getX() + 1.0) - padding;
        double minY = min.getY() + padding;
        double maxY = (max.getY() + 1.0) - padding;
        double minZ = min.getZ() + padding;
        double maxZ = (max.getZ() + 1.0) - padding;

        double randomX = minX + random.nextDouble() * (maxX - minX);
        double randomY = minY + random.nextDouble() * (maxY - minY);
        double randomZ = minZ + random.nextDouble() * (maxZ - minZ);

        ItemEntity itemEntity = new ItemEntity(level, randomX, randomY, randomZ, stack.copy());
        // 【可选】设置物品的初始速度
        // 默认情况下，new ItemEntity 会自带一点随机散射的速度。
        // 如果你希望物品静止生成（比如平移或做特定动画），可以强行清空速度：
        // itemEntity.setDeltaMovement(0, 0, 0);
        // 或者给它一个微微向上的喷射速度（原版打碎方块的效果）：
        itemEntity.setDeltaMovement(
                (random.nextFloat() - 0.5) * 0.1,
                0.2,
                (random.nextFloat() - 0.5) * 0.1
        );
        level.addFreshEntity(itemEntity);
    }
}