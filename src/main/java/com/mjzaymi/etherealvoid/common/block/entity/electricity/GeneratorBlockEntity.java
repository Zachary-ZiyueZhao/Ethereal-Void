package com.mjzaymi.etherealvoid.common.block.entity.electricity;

import com.mjzaymi.etherealvoid.common.block.entity.UpdateBaseBlockEntity;
import com.mjzaymi.etherealvoid.common.electricity.ElectricalSpec;
import com.mjzaymi.etherealvoid.common.electricity.IElectricalDevice;
import com.mjzaymi.etherealvoid.common.electricity.IMultimeterDetectable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorBlockEntity extends UpdateBaseBlockEntity implements IElectricalDevice, IMultimeterDetectable {
    protected final ElectricalSpec SPEC;
    protected final float MAX_POWER_OUTPUT;
    protected float power;
    protected boolean isWorking;
    public GeneratorBlockEntity(BlockEntityType<?> block, BlockPos pos, BlockState state, ElectricalSpec spec, float max) {
        super(block, pos, state);
        this.SPEC = spec;
        this.MAX_POWER_OUTPUT = max;
    }

    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void setPower(float power) {
        this.power= power;
    }

    public float getPower() {
        return power;
    }

    @Override
    public ElectricalSpec getSpec(Direction direction) {
        if (isWorking) return SPEC;
        return SPEC.voltage(0);
    }

    @Override
    public ElectricalSpec getSpec() {
        return SPEC;
    }

    @Override
    public void onBreakdown() {
    }
}
