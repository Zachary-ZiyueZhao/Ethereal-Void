package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.block.FluidPipe;
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

    // 💡 彻底移除了原先的 bufferTank 和 neighborCache！管道现在是纯虚拟路径，零 tick 负载。
    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE_BE.get(), pos, state);
    }

    /**
     * 💡 核心寻路算法：每当管网变化，由发生改变的节点发起广度优先搜索 (BFS)
     */
    public static void updateVirtualNetwork(Level level, BlockPos startPos) {
        if (level.isClientSide()) return;

        Set<BlockPos> visited = new HashSet<>();
        Set<ReactionPoolFluidIOBlockEntity> foundIOs = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(startPos);
        visited.add(startPos);

        // 1. 广度优先搜索探测整条管道能触及的所有方块
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState currentState = level.getBlockState(current);

            if (visited.size() > 1024) break;

            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = current.relative(dir);

                // 检查当前方块如果为管道，其朝向该邻居的接口是否开放
                if (currentState.getBlock() instanceof FluidPipe) {
                    var prop = FluidPipe.getPropertyForDirection(dir);
                    if (currentState.hasProperty(prop) && !currentState.getValue(prop)) {
                        continue;
                    }
                }

                if (visited.contains(neighborPos)) continue;

                BlockState neighborState = level.getBlockState(neighborPos);

                // 情况 A：邻居也是管道，继续延伸搜索
                if (neighborState.getBlock() instanceof FluidPipe) {
                    // 双向验证：邻居连向当前方块的口也必须是开着的
                    var oppProp = FluidPipe.getPropertyForDirection(dir.getOpposite());
                    if (neighborState.hasProperty(oppProp) && neighborState.getValue(oppProp)) {
                        visited.add(neighborPos);
                        queue.add(neighborPos);
                    }
                }
                // 情况 B：邻居是 IO 口，抓取它
                else {
                    BlockEntity be = level.getBlockEntity(neighborPos);
                    if (be instanceof ReactionPoolFluidIOBlockEntity ioBE) {
                        visited.add(neighborPos);
                        foundIOs.add(ioBE);
                    }
                }
            }
        }

        // 2. 预先断开所有在这条管网覆盖范围内的旧虚拟连接（洗牌复位）
        for (ReactionPoolFluidIOBlockEntity io : foundIOs) {
            io.breakVirtualLink();
        }

        // 3. 筛选合法的唯一 I 口和唯一 O 口
        ReactionPoolFluidIOBlockEntity inputIO = null;
        ReactionPoolFluidIOBlockEntity outputIO = null;

        for (ReactionPoolFluidIOBlockEntity io : foundIOs) {
            // 💡 核心安全验证：必须属于一个完整合法的多方块水槽结构 (由你们的多方块逻辑决定)
            if (!io.hasValidPool()) continue;

            if (io.isInputMode()) {
                if (inputIO == null) inputIO = io;
                else return; // 发现多个输入口，不合法，不建立连接
            } else if (io.isOutputMode()) {
                if (outputIO == null) outputIO = io;
                else return; // 发现多个输出口，不合法，不建立连接
            }
        }

        // 4. 验证最终条件：必须同时存在一个I口一个O口
        if (inputIO != null && outputIO != null) {
            // 如果允许跨水槽传输，去掉这个 if 条件（或者改为不等于）
            if (inputIO.getPoolController() != null && outputIO.getPoolController() != null) {
                outputIO.establishVirtualLink(inputIO, 50);
            }
        }
    }
}