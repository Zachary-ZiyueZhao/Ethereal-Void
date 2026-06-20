package com.mjzaymi.etherealvoid.block.entity;

import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import com.mjzaymi.etherealvoid.screen.PoolMonitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
    private final ItemStackHandler itemHandler = new ItemStackHandler(1);

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public PoolMonitorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.POOL_MONITOR_BE.get(), pPos, pBlockState);
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
        Containers.dropContents(level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ethereal_void.pool_monitor");
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
        return new PoolMonitorMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pLevel.isClientSide) return;
        ItemStack inputItem = itemHandler.getStackInSlot(0);
        if (inputItem.isEmpty()) return;
        ReactionPoolBlockEntity blockEntity = getPoolBlockEntity();
        if (blockEntity==null) return;
        CuboidStructure structure = blockEntity.getStructure();
        if (structure==null) return;
        itemHandler.extractItem(0, Integer.MAX_VALUE, false);
        blockEntity.addItem(inputItem);
        blockEntity.updateChangeState(true);
    }
}