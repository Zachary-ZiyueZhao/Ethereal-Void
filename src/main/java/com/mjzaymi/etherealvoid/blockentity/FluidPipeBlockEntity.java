package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.block.FluidPipe;
import com.mjzaymi.etherealvoid.fluidpipe.FluidPipeNetwork;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import com.mjzaymi.etherealvoid.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class FluidPipeBlockEntity extends BlockEntity {

    public FluidPipeNetwork currentNetwork = null;
    // 🟢 只有被玩家亲自放入 Filter 的那个 BE 才会持有这个物品实体！
    public ItemStack savedFilter = ItemStack.EMPTY;

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE_BE.get(), pos, state);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // 假设你存 Filter 用的键名叫 "SavedFilter"，请根据你实际的键名修改
        this.savedFilter = tag.contains("SavedFilter") ? ItemStack.of(tag.getCompound("SavedFilter")) : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.savedFilter.isEmpty()) {
            tag.put("SavedFilter", this.savedFilter.save(new CompoundTag()));
        }
    }

    public static void updateVirtualNetwork(Level level, BlockPos startPos) {
        if (level.isClientSide()) return;

        Set<BlockPos> visitedPipes = new HashSet<>();
        Set<ReactionPoolFluidIOBlockEntity> foundIOs = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(startPos);
        visitedPipes.add(startPos);

        // 1. BFS 搜索相连的所有管道与 IO 口
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState currentState = level.getBlockState(current);

            if (visitedPipes.size() > 2048) break;

            for (Direction dir : Direction.values()) {
                if (currentState.getBlock() instanceof FluidPipe) {
                    var prop = FluidPipe.getPropertyForDirection(dir);
                    if (currentState.hasProperty(prop) && !currentState.getValue(prop)) continue;
                }

                BlockPos neighborPos = current.relative(dir);
                if (visitedPipes.contains(neighborPos)) continue;

                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.getBlock() instanceof FluidPipe) {
                    var oppProp = FluidPipe.getPropertyForDirection(dir.getOpposite());
                    if (neighborState.hasProperty(oppProp) && neighborState.getValue(oppProp)) {
                        visitedPipes.add(neighborPos);
                        queue.add(neighborPos);
                    }
                } else if (level.getBlockEntity(neighborPos) instanceof ReactionPoolFluidIOBlockEntity ioBE) {
                    foundIOs.add(ioBE);
                }
            }
        }

        // 2. 解绑所有 IO 口旧网络
        foundIOs.forEach(io -> io.setNetwork(null, 0));

        // 3. 构建新管网对象
        FluidPipeNetwork newNetwork = new FluidPipeNetwork();
        newNetwork.pipePositions.addAll(visitedPipes);

        boolean hasInput = false;
        boolean hasOutput = false;

        for (ReactionPoolFluidIOBlockEntity io : foundIOs) {
            if (!io.hasValidPool()) continue;
            if (io.isInputMode()) {
                newNetwork.inputs.add(io);
                hasInput = true;
            } else if (io.isOutputMode()) {
                newNetwork.outputs.add(io);
                hasOutput = true;
            }
        }

        // 🟢 4. 收集管网内真正存有实体 Filter 的管道 BlockEntity
        List<FluidPipeBlockEntity> filterContainerBEs = new ArrayList<>();
        Set<String> uniqueFluidIds = new HashSet<>();

        for (BlockPos p : visitedPipes) {
            if (level.getBlockEntity(p) instanceof FluidPipeBlockEntity pipeBE) {
                ItemStack filter = pipeBE.savedFilter;

                if (!filter.isEmpty() && filter.getItem() == ModItems.FLUID_PIPE_FILTER.get()
                        && filter.hasTag() && filter.getTag().contains("FilterFluid")) {
                    uniqueFluidIds.add(filter.getTag().getString("FilterFluid"));
                    filterContainerBEs.add(pipeBE); // 记录持有真实 Filter 物品的 BE
                }

                if (pipeBE.currentNetwork != null && pipeBE.currentNetwork != newNetwork) {
                    pipeBE.currentNetwork.invalidate();
                }
                pipeBE.currentNetwork = newNetwork;
            }
        }

        // 🟢 5. 处理 Filter 冲突、同化与多余弹出
        if (uniqueFluidIds.size() > 1) {
            // 冲突：把所有 BE 里存的实体 Filter 统统吐出来，并彻底清空它们的 savedFilter！
            for (FluidPipeBlockEntity be : filterContainerBEs) {
                Block.popResource(level, startPos, be.savedFilter.copy());
                be.savedFilter = ItemStack.EMPTY;
                be.setChanged();
            }
            level.playSound(null, startPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F);
        } else if (uniqueFluidIds.size() == 1) {
            // 无冲突（同化）：保留第 1 个 BE 里的 Filter 给新网络使用
            FluidPipeBlockEntity primaryBE = filterContainerBEs.get(0);
            newNetwork.setFilter(primaryBE.savedFilter.copy());

            // 关键补全：显式通知系统，触发同步数据包发送给客户端！
            primaryBE.setChanged();

            // 多的同款 Filter 吐出来，并清空对应 BE 的 savedFilter
            if (filterContainerBEs.size() > 1) {
                for (int i = 1; i < filterContainerBEs.size(); i++) {
                    FluidPipeBlockEntity extraBE = filterContainerBEs.get(i);
                    Block.popResource(level, startPos, extraBE.savedFilter.copy());
                    extraBE.savedFilter = ItemStack.EMPTY;
                    extraBE.setChanged();
                }
                level.playSound(null, startPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F);
            }
        }

        // 6. 检查自环
        boolean selfLoop = false;
        for (ReactionPoolFluidIOBlockEntity inIO : newNetwork.inputs) {
            var inStructOpt = com.mjzaymi.etherealvoid.reactionpool.CuboidStructure.findFromWallAndCorner(level, inIO.getBlockPos());
            if (inStructOpt.isEmpty()) continue;

            for (ReactionPoolFluidIOBlockEntity outIO : newNetwork.outputs) {
                var outStructOpt = com.mjzaymi.etherealvoid.reactionpool.CuboidStructure.findFromWallAndCorner(level, outIO.getBlockPos());
                if (outStructOpt.isEmpty()) continue;

                if (inStructOpt.get().min().equals(outStructOpt.get().min()) &&
                        inStructOpt.get().max().equals(outStructOpt.get().max())) {
                    selfLoop = true;
                    break;
                }
            }
            if (selfLoop) break;
        }

        newNetwork.hasSelfLoop = selfLoop;

        // 7. 绑定新网络
        for (BlockPos p : visitedPipes) {
            if (level.getBlockEntity(p) instanceof FluidPipeBlockEntity pipeBE) {
                if (pipeBE.currentNetwork != null && pipeBE.currentNetwork != newNetwork) {
                    pipeBE.currentNetwork.invalidate();
                }
                pipeBE.currentNetwork = newNetwork;
            }
        }

        // 8. 激活 IO
        if (!selfLoop && hasInput && hasOutput) {
            newNetwork.inputs.forEach(io -> io.setNetwork(newNetwork, 50));
            newNetwork.outputs.forEach(io -> io.setNetwork(newNetwork, 50));
        }
    }

    public boolean hasFilter() {
        return !savedFilter.isEmpty()
                && savedFilter.getItem() == ModItems.FLUID_PIPE_FILTER.get()
                && savedFilter.hasTag()
                && savedFilter.getTag().contains("FilterFluid");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt != null && pkt.getTag() != null) {
            load(pkt.getTag());
        }
    }
}