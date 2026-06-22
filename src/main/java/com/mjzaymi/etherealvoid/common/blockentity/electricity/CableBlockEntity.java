package com.mjzaymi.etherealvoid.common.blockentity.electricity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends BlockEntity {

    public CableBlockEntity(BlockEntityType<?> block, BlockPos pos, BlockState state) {
        super(block, pos, state);
    }


    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

    }
}