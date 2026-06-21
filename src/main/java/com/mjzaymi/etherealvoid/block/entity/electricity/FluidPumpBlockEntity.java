package com.mjzaymi.etherealvoid.block.entity.electricity;

import com.mjzaymi.etherealvoid.block.electricity.FluidPump;
import com.mjzaymi.etherealvoid.common.block.entity.UpdateBaseBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;

public class FluidPumpBlockEntity extends UpdateBaseBlockEntity {

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

    public FluidPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PUMP_BE.get(), pos, state);
    }

    // 4. Tick 逻辑
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (level.getGameTime() % 20 != 1) return;

        boolean wasSpinning = isSpinning;

        BlockPos posAbove = pos.above();
        FluidState fluidAbove = level.getFluidState(posAbove);

        boolean hasWaterPower = fluidAbove.is(Fluids.WATER);

        if (hasWaterPower != wasSpinning) {
            isSpinning = hasWaterPower;
            level.setBlock(pos, state.setValue(FluidPump.ACTIVE, isSpinning), 3);
            updateChangeState(true);
        }
    }
}