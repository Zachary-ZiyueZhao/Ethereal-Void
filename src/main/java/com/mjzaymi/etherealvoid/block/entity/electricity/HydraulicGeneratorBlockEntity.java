package com.mjzaymi.etherealvoid.block.entity.electricity;

import com.mjzaymi.etherealvoid.block.electricity.HydraulicGenerator;
import com.mjzaymi.etherealvoid.common.electricity.CurrentType;
import com.mjzaymi.etherealvoid.common.electricity.ElectricalSpec;
import com.mjzaymi.etherealvoid.common.electricity.IElectricalTerminal;
import com.mjzaymi.etherealvoid.common.electricity.WireRole;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;

import java.util.Collections;
import java.util.List;

public class HydraulicGeneratorBlockEntity extends BlockEntity {

    // 1. 定义发电机的输出规格：标准单相交流电 220V
    public static final ElectricalSpec SPEC_LIVE = new ElectricalSpec(CurrentType.AC, WireRole.LIVE, 220.0);
    public static final ElectricalSpec SPEC_NEUTRAL = new ElectricalSpec(CurrentType.AC, WireRole.NEUTRAL, 220.0);

    public static final double MAX_POWER_OUTPUT = 2000.0;
    private boolean isSpinning = false;

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.putBoolean("isSpinning", isSpinning);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        isSpinning = pTag.getBoolean("isSpinning");
    }

    // 2. 实例化物理引脚
    private final IElectricalTerminal liveTerminal = new IElectricalTerminal() {
        @Override public ElectricalSpec getSpec() { return SPEC_LIVE; }
        @Override public double getPotential() { return isSpinning ? 220.0 : 0.0; }
        @Override public double getResistance() { return 0.5; }
        @Override public double getCurrent() { return 0; } // 留给电网管理器覆写/注入
    };

    private final IElectricalTerminal neutralTerminal = new IElectricalTerminal() {
        @Override public ElectricalSpec getSpec() { return SPEC_NEUTRAL; }
        @Override public double getPotential() { return 0.0; }
        @Override public double getResistance() { return 0.5; }
        @Override public double getCurrent() { return 0; }
    };

    public HydraulicGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYDRAULIC_GENERATOR_BE.get(), pos, state);
    }

    // 🌟 3. 核心修改：将单引脚变更为“引脚集合（端口）”
    public List<IElectricalTerminal> getTerminals(Direction side) {
        // 假设我们把 NORTH 面设计成一个“双孔插座”，同时暴露火线和零线
        if (side == Direction.NORTH) {
            return List.of(liveTerminal, neutralTerminal);
        }
        // 如果以后有三相电，你甚至可以 return List.of(phaseA, phaseB, phaseC, neutral);

        // 其他面绝缘，不暴露任何电气接口
        return Collections.emptyList();
    }

    // 4. Tick 逻辑
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // 优秀的性能优化！每秒只检测一次水流
        if (level.getGameTime() % 20 != 0) return;

        boolean wasSpinning = isSpinning;

        BlockPos posAbove = pos.above();
        FluidState fluidAbove = level.getFluidState(posAbove);

        boolean hasWaterPower = fluidAbove.is(Fluids.WATER);

        if (hasWaterPower != wasSpinning) {
            isSpinning = hasWaterPower;
            System.out.println("CHANGED!!");
            setChanged();
            level.setBlock(pos, state.setValue(HydraulicGenerator.ACTIVE, isSpinning), 3);
            level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
        }
    }
}