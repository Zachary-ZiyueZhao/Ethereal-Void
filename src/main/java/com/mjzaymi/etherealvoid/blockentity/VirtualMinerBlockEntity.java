package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VirtualMinerBlockEntity extends BlockEntity {
    public VirtualMinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VIRTUAL_MINER.get(), pos, state);
    }
}