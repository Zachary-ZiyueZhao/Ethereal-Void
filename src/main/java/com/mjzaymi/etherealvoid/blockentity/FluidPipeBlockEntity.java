package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.block.FluidPipe;
import com.mjzaymi.etherealvoid.blockentity.FluidPipeNetwork;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class FluidPipeBlockEntity extends BlockEntity {

    // 💡 存储当前管道所属的网络组
    public FluidPipeNetwork currentNetwork = null;

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE_BE.get(), pos, state);
    }

    public static void updateVirtualNetwork(Level level, BlockPos startPos) {
        if (level.isClientSide()) return;

        Set<BlockPos> visitedPipes = new HashSet<>();
        Set<ReactionPoolFluidIOBlockEntity> foundIOs = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(startPos);
        visitedPipes.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState currentState = level.getBlockState(current);

            if (visitedPipes.size() > 2048) break; // 防止死循环或超大管网卡顿

            for (Direction dir : Direction.values()) {
                // 1. 检查当前方块朝该方向的口是否处于开放状态
                if (currentState.getBlock() instanceof FluidPipe) {
                    var prop = FluidPipe.getPropertyForDirection(dir);
                    if (currentState.hasProperty(prop) && !currentState.getValue(prop)) {
                        continue; // 该面未连接，跳过
                    }
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
                } else {
                    BlockEntity be = level.getBlockEntity(neighborPos);
                    if (be instanceof ReactionPoolFluidIOBlockEntity ioBE) {
                        foundIOs.add(ioBE);
                    }
                }
            }
        }

        // 2. 解绑所有涉及到的 IO 口的旧网络
        for (ReactionPoolFluidIOBlockEntity io : foundIOs) {
            io.setNetwork(null, 0);
        }

        // 3. 构建新的管网对象 (Network Group)
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

        // 4. 将新的管网组下发给所有的管道和 IO (这里设定传输速率为 50)
        for (BlockPos pos : visitedPipes) {
            if (level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipeBE) {
                pipeBE.currentNetwork = newNetwork;
            }
        }

        // 只有在至少存在一个Input和一个Output时，IO才加入网络
        if (hasInput && hasOutput) {
            for (ReactionPoolFluidIOBlockEntity io : newNetwork.inputs) {
                io.setNetwork(newNetwork, 50);
            }
            for (ReactionPoolFluidIOBlockEntity io : newNetwork.outputs) {
                io.setNetwork(newNetwork, 50);
            }
        }
    }
}