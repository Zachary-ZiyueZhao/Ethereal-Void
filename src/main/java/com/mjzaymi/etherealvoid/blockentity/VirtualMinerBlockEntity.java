package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.virtualminer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VirtualMinerBlockEntity extends BlockEntity {

    private int progress = 0;
    private final int MAX_PROGRESS = 40; // 2秒

    // 🌟 核心：创建一个拥有 27 格（和箱子一样大）的内部物品栏
    private final ItemStackHandler inventory = new ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged(); // 当物品栏改变时，标记方块需要保存数据
        }
    };

    // Forge 的自动化能力接口延迟加载对象
    private final LazyOptional<IItemHandler> inventoryOptional = LazyOptional.of(() -> inventory);

    public VirtualMinerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Ticker 每刻运行
    public static void serverTick(Level level, BlockPos pos, BlockState state, VirtualMinerBlockEntity blockEntity) {
        if (level.isClientSide) return;

        blockEntity.progress++;
        if (blockEntity.progress >= blockEntity.MAX_PROGRESS) {
            blockEntity.progress = 0;

            VeinRecipe currentVein = VeinGenerator.getVeinForChunk(level, pos);
            if (currentVein != null) {
                blockEntity.executeMining(level, currentVein);
            }
        }
    }

    private void executeMining(Level level, VeinRecipe vein) {
        double roll = level.random.nextDouble();
        ItemStack minedResult = ItemStack.EMPTY;
        float currentChance = 0.0F;

        for (VeinRecipe.MiningDrop drop : vein.getDrops()) {
            currentChance += drop.chance;
            if (roll < currentChance) {
                minedResult = drop.item.copy();
                break;
            }
        }

        if (!minedResult.isEmpty()) {
            // 🌟 核心修改：不再直接喷在地上，而是使用 Forge 工具类安全地塞进我们刚才定义的 inventory 物品栏里
            // 如果满了，ItemHandlerHelper 会自动返回塞不下的剩余物品
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(this.inventory, minedResult, false);

            // 如果连内部物品栏都满了，再把塞不下的东西喷到方块上方，防止机器罢工
            if (!remainder.isEmpty()) {
                Block.popResource(level, this.worldPosition.above(), remainder);
            }
        }
    }

    // 💾 必须重写：保存数据到存档（比如退出存档时保存物品栏）
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT()); // 把物品栏序列化为NBT
        tag.putInt("Progress", progress);
    }

    // 💾 必须重写：从存档读取数据
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        progress = tag.getInt("Progress");
    }

    // 向外界（比如管道、漏斗）暴露这个物品栏能力
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    // 当方块实体被卸载时释放内存，防止内存泄漏
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryOptional.invalidate();
    }
}