package com.mjzaymi.etherealvoid.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

public class FluidPipeNetwork {
    public final UUID networkId;
    public final Set<BlockPos> pipePositions = new HashSet<>();
    public final Set<ReactionPoolFluidIOBlockEntity> inputs = new HashSet<>();
    public final Set<ReactionPoolFluidIOBlockEntity> outputs = new HashSet<>();

    public boolean isValid = true;
    public boolean hasSelfLoop = false; // 💡 新增：自环标记
    private long lastTickTime = -1;

    public FluidPipeNetwork() {
        this.networkId = UUID.randomUUID();
    }

    public void invalidate() {
        this.isValid = false;
        for (ReactionPoolFluidIOBlockEntity io : inputs) io.setNetwork(null, 0);
        for (ReactionPoolFluidIOBlockEntity io : outputs) io.setNetwork(null, 0);
        this.inputs.clear();
        this.outputs.clear();
    }

    public void tickTransfer(Level level, int ratePerTick) {
        // 💡 核心要求：如果网络失效或者存在自环，立即禁止传输！
        if (!this.isValid || this.hasSelfLoop) return;

        long currentTime = level.getGameTime();
        if (this.lastTickTime == currentTime) return;
        this.lastTickTime = currentTime;

        if (inputs.isEmpty() || outputs.isEmpty()) return;

        // 1. 收集当前合法且有 Handler 的 IO 节点
        List<IOHandlerData> validOutputs = new ArrayList<>();
        List<IOHandlerData> validInputs = new ArrayList<>();

        for (ReactionPoolFluidIOBlockEntity out : outputs) {
            if (out.isRemoved() || !out.hasValidPool() || !out.isOutputMode()) continue;
            IFluidHandler handler = out.getFluidHandlerProxy().orElse(null);
            if (handler != null) validOutputs.add(new IOHandlerData(out, handler));
        }

        for (ReactionPoolFluidIOBlockEntity in : inputs) {
            if (in.isRemoved() || !in.hasValidPool() || !in.isInputMode()) continue;
            IFluidHandler handler = in.getFluidHandlerProxy().orElse(null);
            if (handler != null) validInputs.add(new IOHandlerData(in, handler));
        }

        if (validOutputs.isEmpty() || validInputs.isEmpty()) return;

        // 2. 流体纯净度检测 & 统计总可用量
        FluidStack networkFluid = null;

        for (IOHandlerData outData : validOutputs) {
            int availableInThisOut = 0;
            for (int i = 0; i < outData.handler.getTanks(); i++) {
                FluidStack fluidInTank = outData.handler.getFluidInTank(i);
                if (!fluidInTank.isEmpty() && fluidInTank.getAmount() > 0) {
                    if (networkFluid == null) {
                        networkFluid = fluidInTank.copy();
                        networkFluid.setAmount(1);
                    } else if (!networkFluid.isFluidEqual(fluidInTank)) {
                        return; // 多种流体混合拒接传输
                    }
                    availableInThisOut += fluidInTank.getAmount();
                }
            }
            outData.amountOrSpace = availableInThisOut;
        }

        if (networkFluid == null) return;

        // 3. 统计输入端总容纳空间
        for (IOHandlerData inData : validInputs) {
            FluidStack testStack = networkFluid.copy();
            testStack.setAmount(Integer.MAX_VALUE);
            inData.amountOrSpace = inData.handler.fill(testStack, IFluidHandler.FluidAction.SIMULATE);
        }

        // 4. 木桶效应确定传输量
        int totalAvailable = validOutputs.stream().mapToInt(d -> d.amountOrSpace).sum();
        int totalSpace = validInputs.stream().mapToInt(d -> d.amountOrSpace).sum();

        int targetTransfer = Math.min(ratePerTick, Math.min(totalAvailable, totalSpace));
        if (targetTransfer <= 0) return;

        FluidStack transferTemplate = networkFluid.copy();

        // 5. 动态均分提取
        validOutputs.sort(Comparator.comparingInt(d -> d.amountOrSpace));
        int remainingToExtract = targetTransfer;

        for (int i = 0; i < validOutputs.size(); i++) {
            int fairShare = remainingToExtract / (validOutputs.size() - i);
            IOHandlerData out = validOutputs.get(i);

            int amountToTake = Math.min(fairShare, out.amountOrSpace);
            if (amountToTake > 0) {
                transferTemplate.setAmount(amountToTake);
                out.handler.drain(transferTemplate, IFluidHandler.FluidAction.EXECUTE);
                remainingToExtract -= amountToTake;
            }
        }

        int actualExtracted = targetTransfer - remainingToExtract;

        // 6. 动态均分注入
        validInputs.sort(Comparator.comparingInt(d -> d.amountOrSpace));
        int remainingToInsert = actualExtracted;

        for (int i = 0; i < validInputs.size(); i++) {
            int fairShare = remainingToInsert / (validInputs.size() - i);
            IOHandlerData in = validInputs.get(i);

            int amountToGive = Math.min(fairShare, in.amountOrSpace);
            if (amountToGive > 0) {
                transferTemplate.setAmount(amountToGive);
                in.handler.fill(transferTemplate, IFluidHandler.FluidAction.EXECUTE);
                remainingToInsert -= amountToGive;
            }
        }
    }

    private static class IOHandlerData {
        ReactionPoolFluidIOBlockEntity io;
        IFluidHandler handler;
        int amountOrSpace;

        IOHandlerData(ReactionPoolFluidIOBlockEntity io, IFluidHandler handler) {
            this.io = io;
            this.handler = handler;
        }
    }
}