package com.mjzaymi.etherealvoid.common.block.entity.electricity;

import com.mjzaymi.etherealvoid.common.block.entity.UpdateBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ConsumerBlockEntity extends UpdateBaseBlockEntity {
    public ConsumerBlockEntity(BlockEntityType<?> block, BlockPos pos, BlockState state) {
        super(block, pos, state);
    }
}
