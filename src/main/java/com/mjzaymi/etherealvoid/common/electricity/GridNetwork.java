package com.mjzaymi.etherealvoid.common.electricity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public class GridNetwork {
    private final Set<BlockPos> cables = new HashSet<>();
    private final Set<BlockPos> generators = new HashSet<>();
    private final Set<BlockPos> consumers = new HashSet<>();

    // 当前电网的统一物理属性
    private float currentVoltage = 0.0f;
    private float currentFrequency = 0.0f;

    public void addCable(BlockPos pos) { cables.add(pos); }
    public void addGenerator(BlockPos pos) { generators.add(pos); }
    public void addConsumer(BlockPos pos) { consumers.add(pos); }

    // 每 T 统一计算一次整张网的逻辑
    public void tickNetwork(Level level) {
        // 1. 汇总所有 generator 的输入参数
        // 2. 根据欧姆定律计算整体电压/频率
        // 3. 将参数平均或按需分配给所有 consumers
    }
}