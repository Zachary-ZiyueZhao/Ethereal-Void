package com.mjzaymi.etherealvoid.common.electricity;

import com.mjzaymi.etherealvoid.common.blockentity.electricity.CableBlockEntity;
import com.mjzaymi.etherealvoid.common.blockentity.electricity.ConsumerBlockEntity;
import com.mjzaymi.etherealvoid.common.blockentity.electricity.GeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class ElectricNetwork {
    //private final Level level;
    private final Set<BlockPos> wirePositions = new HashSet<>();
    public CurrentType networkType; // 这个子网是 AC 还是 DC？

    // 拓扑构建：当玩家放置电线时，通过广度优先搜索 (BFS) 找出所有相连的电线和机器端子
    public GridNetwork rebuildTopology(Level level, BlockPos startPos) {
        GridNetwork newGrid = new GridNetwork();

        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            BlockEntity be = level.getBlockEntity(currentPos);

            // 检查相邻的 6 个方向
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(dir);
                if (visited.contains(neighborPos)) continue;

                BlockEntity neighborBe = level.getBlockEntity(neighborPos);
                if (neighborBe == null) continue;

                // 核心逻辑：接口合法性检测！
                if (isValidConnection(be, neighborBe, dir)) {
                    visited.add(neighborPos);
                    queue.add(neighborPos);

                    // 根据相邻方块的类型归类并加入电网
                    if (neighborBe instanceof CableBlockEntity) {
                        newGrid.addCable(neighborPos);
                    } else if (neighborBe instanceof GeneratorBlockEntity) {
                        newGrid.addGenerator(neighborPos);
                    } else if (neighborBe instanceof ConsumerBlockEntity) {
                        newGrid.addConsumer(neighborPos);
                    }
                }
            }
        }
        return newGrid;
    }

    // 检查连接是否合法（例如：拒绝单相线连三相发电机）
    private static boolean isValidConnection(BlockEntity from, BlockEntity to, Direction dir) {
        // 如果目标是电线，检查它的相数是否匹配当前电网
        /*if (to instanceof CableBlockEntity cable) {
            return cable.getPhaseType() == networkPhase;
        }

        // 如果目标是机器，检查该机器在该面上是否允许这种相数接入
        if (to instanceof IACMachine machine) {
            return machine.canConnectPhase(dir.getOpposite(), networkPhase);
        }*/
        return false;
    }
}