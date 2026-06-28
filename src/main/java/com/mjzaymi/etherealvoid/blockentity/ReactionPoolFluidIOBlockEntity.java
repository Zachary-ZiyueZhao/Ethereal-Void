package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.block.FluidIOMode;
import com.mjzaymi.etherealvoid.block.ReactionPoolFluidIO;
import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
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

    // 💡 虚拟长连接的核心内部变量
    @Nullable
    private ReactionPoolFluidIOBlockEntity virtualTargetInput = null;
    private int transferRatePerTick = 0;

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

            int space = master.getTank().getSpace();
            FluidStack existing = getFluidInTank(tank);
            return existing.getAmount() + space;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
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
            BlockState state = getBlockState();
            if (state.hasProperty(ReactionPoolFluidIO.MODE) && state.getValue(ReactionPoolFluidIO.MODE) == FluidIOMode.OUTPUT) {
                ReactionPoolBlockEntity master = getMaster();
                if (master != null && master.getTank() != null) {
                    List<FluidStack> fluids = master.getTank().getFluids();
                    if (fluids.isEmpty()) return FluidStack.EMPTY;

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

    // ==========================================
    // 💡 虚拟网络调用的核心交互方法 (管道网络刷新时使用)
    // ==========================================

    /**
     * 判断当前 IO 方块是否绑定了有效的水槽结构
     */
    public boolean hasValidPool() {
        if (level == null) return false;
        return CuboidStructure.findFromWallAndCorner(level, worldPosition).isPresent();
    }

    /**
     * 判断当前是否为 INPUT 模式
     */
    public boolean isInputMode() {
        BlockState state = getBlockState();
        return state.hasProperty(ReactionPoolFluidIO.MODE) && state.getValue(ReactionPoolFluidIO.MODE) == FluidIOMode.INPUT;
    }

    /**
     * 判断当前是否为 OUTPUT 模式
     */
    public boolean isOutputMode() {
        BlockState state = getBlockState();
        return state.hasProperty(ReactionPoolFluidIO.MODE) && state.getValue(ReactionPoolFluidIO.MODE) == FluidIOMode.OUTPUT;
    }

    /**
     * 获取多方块核心主控的位置作为唯一标识符（用于验证是否属于同个水槽）
     */
    @Nullable
    public BlockPos getPoolController() {
        if (level == null) return null;
        return CuboidStructure.findFromWallAndCorner(level, worldPosition)
                .map(CuboidStructure::min) // 直接使用 min 点作为水槽唯一的 Controller 标识
                .orElse(null);
    }

    /**
     * 由管网寻路算法触发：命令此 OUTPUT 口建立到目标的直线直达通道
     */
    public void establishVirtualLink(ReactionPoolFluidIOBlockEntity inputTarget, int ratePerTick) {
        this.virtualTargetInput = inputTarget;
        this.transferRatePerTick = ratePerTick;
    }

    /**
     * 由管网寻路算法触发：管网断开时，彻底解绑长连接
     */
    public void breakVirtualLink() {
        this.virtualTargetInput = null;
        this.transferRatePerTick = 0;
    }

    // ==========================================
    // 💡 核心传输 Tick：由 Ticker 高效驱动虚拟长连接
    // ==========================================
    public static void tick(Level level, BlockPos pos, BlockState state, ReactionPoolFluidIOBlockEntity io) {
        if (level.isClientSide) return;

        if (io.isOutputMode() && io.virtualTargetInput != null) {

            if (io.virtualTargetInput.isRemoved() || !io.virtualTargetInput.isInputMode() || io.getPoolController() != io.virtualTargetInput.getPoolController()) {
                io.breakVirtualLink();
                return;
            }

            IFluidHandler myHandler = io.fluidHandlerProxy.orElse(null);
            IFluidHandler targetHandler = io.virtualTargetInput.fluidHandlerProxy.orElse(null);

            if (myHandler != null && targetHandler != null) {
                boolean transferSuccess = false;

                // 💡 升级：遍历自己储罐里的每一种液体（氯气、氢氧化钠、氢气等），谁能传就传谁
                for (int i = 0; i < myHandler.getTanks(); i++) {
                    FluidStack fluidInTank = myHandler.getFluidInTank(i);
                    if (fluidInTank.isEmpty() || fluidInTank.getAmount() <= 0) continue;

                    // 准备抽取该种液体，最大限制为流速限制
                    int amountToDrain = Math.min(io.transferRatePerTick, fluidInTank.getAmount());
                    FluidStack targetDrain = new FluidStack(fluidInTank.getFluid(), amountToDrain, fluidInTank.getTag());

                    // 模拟抽取与注入
                    FluidStack simulatedDrain = myHandler.drain(targetDrain, IFluidHandler.FluidAction.SIMULATE);
                    if (!simulatedDrain.isEmpty()) {
                        int accepted = targetHandler.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);

                        if (accepted > 0) {
                            // 真正执行扣除与注入
                            FluidStack realDrained = myHandler.drain(new FluidStack(simulatedDrain.getFluid(), accepted), IFluidHandler.FluidAction.EXECUTE);
                            targetHandler.fill(realDrained, IFluidHandler.FluidAction.EXECUTE);

                            transferSuccess = true;
                            break; // 这一 tick 成功传输了，结束本 tick 的传输，等待下一 tick
                        }
                    }
                }
            }
        }
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