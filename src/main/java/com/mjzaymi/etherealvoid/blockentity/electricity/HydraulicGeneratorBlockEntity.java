package com.mjzaymi.etherealvoid.blockentity.electricity;

import com.mjzaymi.etherealvoid.block.electricity.HydraulicGenerator;
import com.mjzaymi.etherealvoid.common.blockentity.electricity.GeneratorBlockEntity;
import com.mjzaymi.etherealvoid.common.electricity.CurrentType;
import com.mjzaymi.etherealvoid.common.electricity.ElectricalSpec;
import com.mjzaymi.etherealvoid.common.util.fluid.FluidUtil;
import com.mjzaymi.etherealvoid.common.util.math.Range;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;

public class HydraulicGeneratorBlockEntity extends GeneratorBlockEntity {
    public static final float NOMINAL_VOLTAGE = 220;
    public static final float BROKEN_DOWN_VOLTAGE = 2200;
    public static final float MAX_POWER_OUTPUT = 2000;
    private int waterHeight = 0;

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.putInt("waterHeight", waterHeight);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        waterHeight = pTag.getInt("waterHeight");
    }

    public HydraulicGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYDRAULIC_GENERATOR_BE.get(), pos, state, new ElectricalSpec(
                CurrentType.AC, NOMINAL_VOLTAGE, new Range(NOMINAL_VOLTAGE, BROKEN_DOWN_VOLTAGE)), MAX_POWER_OUTPUT);
    }

    // 4. Tick 逻辑
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (level.getGameTime() % 20 != 2) return;

        boolean wasSpinning = waterHeight!=0;

        BlockPos posAbove = pos.above();
        FluidState fluidAbove = level.getFluidState(posAbove);

        boolean hasWaterPower = fluidAbove.is(Fluids.FLOWING_WATER) && !fluidAbove.isSource();
        System.out.println(hasWaterPower);
        if (!hasWaterPower) {
            if (!wasSpinning)
                return;
            waterHeight = 0;
            level.setBlock(pos, state.setValue(HydraulicGenerator.ACTIVE, false), 3);
            updateChangeState(true);
            return;
        }

        BlockPos sourcePos = FluidUtil.findWaterSource(level, posAbove);
        if (sourcePos==null) {
            if (!wasSpinning)
                return;
            waterHeight = 0;
            level.setBlock(pos, state.setValue(HydraulicGenerator.ACTIVE, false), 3);
            updateChangeState(true);
            return;
        }
        int height = sourcePos.getY()-posAbove.getY();
        if (waterHeight==height) return;
        waterHeight = height;
        setPower((float)waterHeight*NOMINAL_VOLTAGE);
        if (!wasSpinning) level.setBlock(pos, state.setValue(HydraulicGenerator.ACTIVE, true), 3);
        updateChangeState(true);
    }
}