package com.mjzaymi.etherealvoid.block.entity.electricity;

import com.mjzaymi.etherealvoid.common.electricity.CurrentType;
import com.mjzaymi.etherealvoid.common.electricity.ElectricalSpec;
import com.mjzaymi.etherealvoid.common.electricity.IElectricalTerminal;
import com.mjzaymi.etherealvoid.common.electricity.WireRole;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;

public class HydraulicGeneratorBlockEntity extends BlockEntity {

    // 1. 定义发电机的输出规格：标准单相交流电 220V
    public static final ElectricalSpec SPEC_LIVE = new ElectricalSpec(CurrentType.AC, WireRole.LIVE, 220.0);
    public static final ElectricalSpec SPEC_NEUTRAL = new ElectricalSpec(CurrentType.AC, WireRole.NEUTRAL, 220.0);

    // 水轮机的最大输出功率 (比如 2000W)
    public static final double MAX_POWER_OUTPUT = 2000.0;

    // 运行状态
    private boolean isSpinning = false;
    private int tickCounter = 0; // 用于降低环境检测频率的计时器

    // 2. 实例化物理引脚（比如火线在背面，零线在底面）
    private final IElectricalTerminal liveTerminal = new IElectricalTerminal() {
        @Override public ElectricalSpec getSpec() { return SPEC_LIVE; }

        // 核心逻辑：只有水轮机在转，才产生 220V 电势差
        @Override public double getPotential() { return isSpinning ? 220.0 : 0.0; }

        @Override public double getResistance() { return 0.5; } // 发电机线圈内阻

        @Override
        public double getCurrent() {
            return 0;
        }
    };

    private final IElectricalTerminal neutralTerminal = new IElectricalTerminal() {
        @Override public ElectricalSpec getSpec() { return SPEC_NEUTRAL; }
        @Override public double getPotential() { return 0.0; } // 零线永远是 0V 基准
        @Override public double getResistance() { return 0.5; }

        @Override
        public double getCurrent() {
            return 0;
        }
    };

    public HydraulicGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYDRAULIC_GENERATOR_BE.get(), pos, state);
    }

    // 3. 供电网路获取端子的接口 (Capability / 侧面朝向判断)
    public IElectricalTerminal getTerminal(Direction side) {
        if (side == Direction.NORTH) return liveTerminal;
        if (side == Direction.SOUTH) return neutralTerminal;
        return null; // 其他面没有电气接口
    }

    // 4. Tick 逻辑：检测水流动力
    public static void tick(Level level, BlockPos pos, BlockState state, HydraulicGeneratorBlockEntity be) {
        if (level.isClientSide) return;

        be.tickCounter++;

        // 【性能优化】不需要每 tick 都检测水流，每 20 ticks (1秒) 检测一次足够了
        if (be.tickCounter >= 20) {
            be.tickCounter = 0;
            boolean wasSpinning = be.isSpinning;

            // 检测动力源：上方是否有水？
            BlockPos posAbove = pos.above();
            FluidState fluidAbove = level.getFluidState(posAbove);

            // 简单判断：只要上面是水（无论是水源还是流水），就认为有动力
            // 你也可以写得更硬核：要求上面是水，且下面是空气（模拟落差做功）
            boolean hasWaterPower = fluidAbove.is(Fluids.WATER);

            if (hasWaterPower != wasSpinning) {
                be.isSpinning = hasWaterPower;
                be.setChanged(); // 标记数据已更改，准备保存到硬盘

                // 可选：更新方块状态，让前端模型的水轮开始转动
                // level.setBlock(pos, state.setValue(HydraulicGeneratorBlock.ACTIVE, be.isSpinning), 3);
            }
        }

        // 注意：这里依然不需要主动输出能量 (Push)。
        // 电压 (Potential) 已经通过 Terminal 暴露。
        // 电网管理器 (Grid Manager) 会自己过来读取 220V 并抽走电流。
    }
}