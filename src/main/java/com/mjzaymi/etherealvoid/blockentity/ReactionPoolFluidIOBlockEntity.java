package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.block.FluidIOMode;
import com.mjzaymi.etherealvoid.block.ReactionPoolFluidIO;
import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReactionPoolFluidIOBlockEntity extends BlockEntity {

    private final LazyOptional<IFluidHandler> fluidHandlerProxy = LazyOptional.of(() -> new IFluidHandler() {

        // 动态获取多方块核心主控
        @Nullable
        private ReactionPoolBlockEntity getMaster() {
            if (level == null) return null;
            return CuboidStructure.findFromWallAndCorner(level, worldPosition)
                    .map(structure -> level.getBlockEntity(structure.min()))
                    .filter(be -> be instanceof ReactionPoolBlockEntity)
                    .map(be -> (ReactionPoolBlockEntity) be)
                    .orElse(null);
        }

        @Override
        public int getTanks() {
            ReactionPoolBlockEntity master = getMaster();
            if (master == null || master.getTank() == null) return 0;
            // 关键点：返回当前已有的流体种类数量 + 1。
            // 永远多留一个“虚拟空位”，外部管道（如 Mekanism）才能把新种类的流体推进来。
            return master.getTank().getFluids().size() + 1;
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            ReactionPoolBlockEntity master = getMaster();
            if (master == null || master.getTank() == null) return FluidStack.EMPTY;

            List<FluidStack> fluids = master.getTank().getFluids();
            if (tank >= 0 && tank < fluids.size()) {
                return fluids.get(tank);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            ReactionPoolBlockEntity master = getMaster();
            if (master == null || master.getTank() == null) return 0;

            // 因为你的反应池是“共享总容量”的
            // 对于某个特定的流体槽，它的最大可用极限 = 该流体当前量 + 整个反应池的剩余空闲空间
            int space = master.getTank().getSpace();
            FluidStack existing = getFluidInTank(tank);
            return existing.getAmount() + space;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            // 反应池来者不拒，只要有空间都可以进
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            // 只有当方块状态为 INPUT 时才允许注入流体
            BlockState state = getBlockState();
            if (state.hasProperty(ReactionPoolFluidIO.MODE) && state.getValue(ReactionPoolFluidIO.MODE) == FluidIOMode.INPUT) {
                ReactionPoolBlockEntity master = getMaster();
                if (master != null && master.getTank() != null) {
                    int filled = master.getTank().fill(resource, action);
                    if (filled > 0 && action.execute()) {
                        master.updateChangeState(true);
                    }
                    return filled;
                }
            }
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            // 只有当方块状态为 OUTPUT 时才允许抽走流体
            BlockState state = getBlockState();
            if (state.hasProperty(ReactionPoolFluidIO.MODE) && state.getValue(ReactionPoolFluidIO.MODE) == FluidIOMode.OUTPUT) {
                ReactionPoolBlockEntity master = getMaster();
                if (master != null && master.getTank() != null) {
                    FluidStack drained = master.getTank().drain(resource, action);
                    if (!drained.isEmpty() && action.execute()) {
                        master.updateChangeState(true);
                    }
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            // 当外部管道不指定流体类型，只说“我要抽走最多 maxDrain 容积的流体”时触发
            BlockState state = getBlockState();
            if (state.hasProperty(ReactionPoolFluidIO.MODE) && state.getValue(ReactionPoolFluidIO.MODE) == FluidIOMode.OUTPUT) {
                ReactionPoolBlockEntity master = getMaster();
                if (master != null && master.getTank() != null) {
                    List<FluidStack> fluids = master.getTank().getFluids();
                    if (fluids.isEmpty()) return FluidStack.EMPTY;

                    // 默认抽取列表里的第一种流体
                    FluidStack firstFluid = fluids.get(0);
                    int drainAmount = Math.min(maxDrain, firstFluid.getAmount());
                    FluidStack toDrain = new FluidStack(firstFluid, drainAmount);

                    FluidStack drained = master.getTank().drain(toDrain, action);
                    if (!drained.isEmpty() && action.execute()) {
                        master.updateChangeState(true);
                    }
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }
    });

    public ReactionPoolFluidIOBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTION_POOL_FLUID_IO_BE.get(), pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandlerProxy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandlerProxy.invalidate();
    }
}