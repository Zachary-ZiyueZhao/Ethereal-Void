package com.mjzaymi.etherealvoid.fluidpipe;

import com.mjzaymi.etherealvoid.blockentity.FluidPipeBlockEntity;
import com.mjzaymi.etherealvoid.blockentity.ReactionPoolFluidIOBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class FluidPipeNetwork {
    public final UUID networkId;
    public final Set<BlockPos> pipePositions = new HashSet<>();
    public final Set<ReactionPoolFluidIOBlockEntity> inputs = new HashSet<>();
    public final Set<ReactionPoolFluidIOBlockEntity> outputs = new HashSet<>();

    public boolean isValid = true;
    public boolean hasSelfLoop = false;
    private long lastTickTime = -1;

    private ItemStack installedFilter = ItemStack.EMPTY;

    public FluidPipeNetwork() {
        this.networkId = UUID.randomUUID();
    }

    public ItemStack getInstalledFilter() { return installedFilter; }

    public boolean hasFilter() {
        return !installedFilter.isEmpty() && installedFilter.getItem() == ModItems.FLUID_PIPE_FILTER.get();
    }

    public void setFilter(ItemStack filter) {
        this.installedFilter = filter.copy();
        this.installedFilter.setCount(1);
    }

    public boolean isFluidAllowed(FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.isEmpty()) return false;
        if (!hasFilter()) return false;

        if (installedFilter.hasTag() && installedFilter.getTag().contains("FilterFluid")) {
            String allowedFluidId = installedFilter.getTag().getString("FilterFluid");
            ResourceLocation currentFluidId = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
            return currentFluidId != null && currentFluidId.toString().equals(allowedFluidId);
        }
        return false;
    }

    public void popFilter(Level level, BlockPos pos, Player player) {
        ItemStack toPop = ItemStack.EMPTY;

        // 💡 遍历管网内的所有管道，彻底清空所有可能残留的 savedFilter，防止 HashSet 遍历顺序导致遗漏
        for (BlockPos p : pipePositions) {
            if (level.getBlockEntity(p) instanceof FluidPipeBlockEntity be) {
                if (!be.savedFilter.isEmpty()) {
                    if (toPop.isEmpty()) {
                        toPop = be.savedFilter.copy(); // 记录要退还给玩家的物品
                    }
                    be.savedFilter = ItemStack.EMPTY;
                    be.setChanged();

                    BlockState state = level.getBlockState(p);
                    level.sendBlockUpdated(p, state, state, 3); // 强制同步给客户端
                }
            }
        }

        // 将收集到的 Filter 物品返还给玩家或掉落
        if (!toPop.isEmpty()) {
            if (player != null) {
                if (!player.getInventory().add(toPop)) {
                    player.drop(toPop, false);
                }
            } else {
                Block.popResource(level, pos, toPop);
            }
        }

        this.installedFilter = ItemStack.EMPTY;
    }

    public void invalidate() {
        this.isValid = false;
        for (ReactionPoolFluidIOBlockEntity io : inputs) io.setNetwork(null, 0);
        for (ReactionPoolFluidIOBlockEntity io : outputs) io.setNetwork(null, 0);
        this.inputs.clear();
        this.outputs.clear();
    }

    public void tickTransfer(Level level, int ratePerTick) {
        if (!this.isValid || this.hasSelfLoop || !this.hasFilter()) return;

        long currentTime = level.getGameTime();
        if (this.lastTickTime == currentTime) return;
        this.lastTickTime = currentTime;

        if (inputs.isEmpty() || outputs.isEmpty()) return;

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

        FluidStack networkFluid = null;
        for (IOHandlerData outData : validOutputs) {
            int availableInThisOut = 0;
            for (int i = 0; i < outData.handler.getTanks(); i++) {
                FluidStack fluidInTank = outData.handler.getFluidInTank(i);
                if (!fluidInTank.isEmpty() && fluidInTank.getAmount() > 0 && isFluidAllowed(fluidInTank)) {
                    if (networkFluid == null) {
                        networkFluid = fluidInTank.copy();
                        networkFluid.setAmount(1);
                    } else if (!networkFluid.isFluidEqual(fluidInTank)) {
                        return;
                    }
                    availableInThisOut += fluidInTank.getAmount();
                }
            }
            outData.amountOrSpace = availableInThisOut;
        }

        if (networkFluid == null) return;

        for (IOHandlerData inData : validInputs) {
            FluidStack testStack = networkFluid.copy();
            testStack.setAmount(Integer.MAX_VALUE);
            inData.amountOrSpace = inData.handler.fill(testStack, IFluidHandler.FluidAction.SIMULATE);
        }

        int totalAvailable = validOutputs.stream().mapToInt(d -> d.amountOrSpace).sum();
        int totalSpace = validInputs.stream().mapToInt(d -> d.amountOrSpace).sum();

        int targetTransfer = Math.min(ratePerTick, Math.min(totalAvailable, totalSpace));
        if (targetTransfer <= 0) return;

        FluidStack transferTemplate = networkFluid.copy();
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
        final ReactionPoolFluidIOBlockEntity io;
        final IFluidHandler handler;
        int amountOrSpace;

        IOHandlerData(ReactionPoolFluidIOBlockEntity io, IFluidHandler handler) {
            this.io = io;
            this.handler = handler;
        }
    }
}