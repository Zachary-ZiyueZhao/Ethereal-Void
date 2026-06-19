package com.mjzaymi.etherealvoid.block.entity;

import com.mjzaymi.etherealvoid.block.FluidPipe;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.HashMap;
import java.util.Map;

public class FluidPipeBlockEntity extends BlockEntity {
    // 1. 管道自身的内部流体小缓冲池 (1000mB)
    private final FluidTank bufferTank = new FluidTank(1000);

    // 2. 性能核心：邻居流体处理器缓存，避免每 tick 都在底层调用 getBlockEntity
    private final Map<Direction, IFluidHandler> neighborCache = new HashMap<>();
    private boolean cacheInvalidated = true; // 触发标记

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FluidPipeBlockEntity pipe) {
        if (level.isClientSide) return;

        // 如果标记了失效（例如周围放了新机器），重新构建缓存
        if (pipe.cacheInvalidated) {
            pipe.rebuildCache(level, pos);
        }

        // 如果管道里根本没水，直接跳过计算
        if (pipe.bufferTank.isEmpty()) return;

        FluidStack currentFluid = pipe.bufferTank.getFluid();
        int availableNeighbors = pipe.neighborCache.size();
        if (availableNeighbors == 0) return;

        // 计算这 1 tick 单个方向最大能推送多少流体 (假设管道最大流速 100mB/tick)
        int maxTransferRate = 100;
        int amountPerNeighbor = Math.min(maxTransferRate, currentFluid.getAmount() / availableNeighbors);
        if (amountPerNeighbor <= 0) return;

        boolean didTransfer = false;

        // 🔄 开始向周围缓存的机器/管道【平分推送】液体
        for (Map.Entry<Direction, IFluidHandler> entry : pipe.neighborCache.entrySet()) {
            Direction dir = entry.getKey();
            IFluidHandler targetHandler = entry.getValue();

            // 提取出准备送给当前邻居的液体份量
            FluidStack toSend = new FluidStack(currentFluid.getFluid(), amountPerNeighbor, currentFluid.getTag());

            // 模拟或直接注入到邻居中
            int accepted = targetHandler.fill(toSend, IFluidHandler.FluidAction.EXECUTE);
            if (accepted > 0) {
                // 扣除管道自身的液体
                pipe.bufferTank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                didTransfer = true;
            }
        }

        if (didTransfer) {
            pipe.setChanged(); // 标记需要保存数据
        }
    }

    /**
     * 重新构建缓存的方法。仅在方块被动更新时触发，拒绝每tick盲目遍历！
     */
    private void rebuildCache(Level level, BlockPos pos) {
        neighborCache.clear();
        BlockState state = this.getBlockState();

        for (Direction dir : Direction.values()) {
            // 通过 BlockState 判断该方向的通道是否打开了
            if (state.hasProperty(FluidPipe.getPropertyForDirection(dir))
                    && state.getValue(FluidPipe.getPropertyForDirection(dir))) {

                BlockEntity targetBE = level.getBlockEntity(pos.relative(dir));
                if (targetBE != null) {
                    // 获取对方这个面暴露出的 IFluidHandler 并塞入缓存
                    targetBE.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).ifPresent(handler -> {
                        neighborCache.put(dir, handler);
                    });
                }
            }
        }
        this.cacheInvalidated = false;
    }

    // 外部调用红石更新或邻居更新时触发这个
    public void invalidateCache() {
        this.cacheInvalidated = true;
    }
}