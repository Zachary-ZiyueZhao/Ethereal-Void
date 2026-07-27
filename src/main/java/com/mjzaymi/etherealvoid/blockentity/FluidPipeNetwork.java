package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.blockentity.ReactionPoolFluidIOBlockEntity;
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
        if (!this.isValid) return;

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

        // ==========================================
        // 2. 流体纯净度检测 & 统计总可用量
        // ==========================================
        FluidStack networkFluid = null;

        for (IOHandlerData outData : validOutputs) {
            int availableInThisOut = 0;
            for (int i = 0; i < outData.handler.getTanks(); i++) {
                FluidStack fluidInTank = outData.handler.getFluidInTank(i);
                if (!fluidInTank.isEmpty() && fluidInTank.getAmount() > 0) {

                    if (networkFluid == null) {
                        // 记录管网中的第一种流体
                        networkFluid = fluidInTank.copy();
                        networkFluid.setAmount(1);
                    } else if (!networkFluid.isFluidEqual(fluidInTank)) {
                        // ❌ 核心要求：检测到多种流体混合，立刻拒接任何传输！
                        return;
                    }
                    availableInThisOut += fluidInTank.getAmount();
                }
            }
            outData.amountOrSpace = availableInThisOut;
        }

        // 没找到任何流体，直接结束
        if (networkFluid == null) return;

        // ==========================================
        // 3. 统计输入端的总容纳空间
        // ==========================================
        for (IOHandlerData inData : validInputs) {
            FluidStack testStack = networkFluid.copy();
            testStack.setAmount(Integer.MAX_VALUE);
            // 模拟注入，获取该水槽能吃下多少这种流体
            inData.amountOrSpace = inData.handler.fill(testStack, IFluidHandler.FluidAction.SIMULATE);
        }

        // ==========================================
        // 4. 木桶效应：确定本次Tick真实的传输量
        // ==========================================
        int totalAvailable = validOutputs.stream().mapToInt(d -> d.amountOrSpace).sum();
        int totalSpace = validInputs.stream().mapToInt(d -> d.amountOrSpace).sum();

        // 绝对安全值：不会超过带宽，不会抽干输出端，也不会溢出输入端
        int targetTransfer = Math.min(ratePerTick, Math.min(totalAvailable, totalSpace));
        if (targetTransfer <= 0) return;

        FluidStack transferTemplate = networkFluid.copy();

        // ==========================================
        // 5. 动态均分提取 (Outputs)
        // ==========================================
        // 按可用量升序排序：优先抽取量少的水槽，保证量大的水槽兜底吃掉余数
        validOutputs.sort(Comparator.comparingInt(d -> d.amountOrSpace));
        int remainingToExtract = targetTransfer;

        for (int i = 0; i < validOutputs.size(); i++) {
            // 核心均分魔法：剩余量 / 剩余节点数
            int fairShare = remainingToExtract / (validOutputs.size() - i);
            IOHandlerData out = validOutputs.get(i);

            int amountToTake = Math.min(fairShare, out.amountOrSpace);
            if (amountToTake > 0) {
                transferTemplate.setAmount(amountToTake);
                out.handler.drain(transferTemplate, IFluidHandler.FluidAction.EXECUTE);
                remainingToExtract -= amountToTake;
            }
        }

        // 真实抽出来的总量
        int actualExtracted = targetTransfer - remainingToExtract;

        // ==========================================
        // 6. 动态均分注入 (Inputs)
        // ==========================================
        // 按可用空间升序排序：空间小的先喂，空间大的兜底吃掉余数
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

    /**
     * 辅助数据类：用于在本次 Tick 缓存各个 IO 口的可用容量/空间，避免反复调用
     */
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