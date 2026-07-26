package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.blockentity.ReactionPoolFluidIOBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FluidPipeNetwork {
    public final UUID networkId;
    public final Set<BlockPos> pipePositions = new HashSet<>();
    public final Set<ReactionPoolFluidIOBlockEntity> inputs = new HashSet<>();
    public final Set<ReactionPoolFluidIOBlockEntity> outputs = new HashSet<>();

    // 💡 新增：网络的有效性标志
    public boolean isValid = true;
    private long lastTickTime = -1;

    public FluidPipeNetwork() {
        this.networkId = UUID.randomUUID();
    }

    /**
     * 💡 新增：立刻作废当前网络，解绑所有 IO
     */
    public void invalidate() {
        this.isValid = false;
        for (ReactionPoolFluidIOBlockEntity io : inputs) io.setNetwork(null, 0);
        for (ReactionPoolFluidIOBlockEntity io : outputs) io.setNetwork(null, 0);
        this.inputs.clear();
        this.outputs.clear();
    }

    public void tickTransfer(Level level, int ratePerTick) {
        // 💡 严苛的合法性检测：网络作废则立刻停止一切行为
        if (!this.isValid) return;

        long currentTime = level.getGameTime();
        if (this.lastTickTime == currentTime) return;
        this.lastTickTime = currentTime;

        if (inputs.isEmpty() || outputs.isEmpty()) return;

        for (ReactionPoolFluidIOBlockEntity output : outputs) {
            // 💡 严苛的合法性检测：IO被拆除、水槽失效、或者模式被意外改变，直接跳过
            if (output.isRemoved() || !output.hasValidPool() || !output.isOutputMode()) continue;

            IFluidHandler outHandler = output.getFluidHandlerProxy().orElse(null);
            if (outHandler == null) continue;

            for (int i = 0; i < outHandler.getTanks(); i++) {
                FluidStack fluidInTank = outHandler.getFluidInTank(i);
                if (fluidInTank.isEmpty() || fluidInTank.getAmount() <= 0) continue;

                int amountToExtract = Math.min(ratePerTick, fluidInTank.getAmount());
                FluidStack targetDrain = new FluidStack(fluidInTank.getFluid(), amountToExtract, fluidInTank.getTag());

                FluidStack simulatedDrain = outHandler.drain(targetDrain, IFluidHandler.FluidAction.SIMULATE);
                if (simulatedDrain.isEmpty()) continue;

                int remainingToInsert = simulatedDrain.getAmount();

                for (ReactionPoolFluidIOBlockEntity input : inputs) {
                    // 💡 严苛的合法性检测：检测输入端状态
                    if (input.isRemoved() || !input.hasValidPool() || !input.isInputMode()) continue;
                    IFluidHandler inHandler = input.getFluidHandlerProxy().orElse(null);
                    if (inHandler == null) continue;

                    FluidStack toInsert = new FluidStack(simulatedDrain.getFluid(), remainingToInsert, simulatedDrain.getTag());
                    int accepted = inHandler.fill(toInsert, IFluidHandler.FluidAction.SIMULATE);

                    if (accepted > 0) {
                        FluidStack realDrained = outHandler.drain(new FluidStack(simulatedDrain.getFluid(), accepted, simulatedDrain.getTag()), IFluidHandler.FluidAction.EXECUTE);
                        inHandler.fill(realDrained, IFluidHandler.FluidAction.EXECUTE);
                        remainingToInsert -= accepted;
                    }
                    if (remainingToInsert <= 0) break;
                }

                if (remainingToInsert < simulatedDrain.getAmount()) {
                    return;
                }
            }
        }
    }
}