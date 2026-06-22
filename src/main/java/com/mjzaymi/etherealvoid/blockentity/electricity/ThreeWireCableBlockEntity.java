package com.mjzaymi.etherealvoid.blockentity.electricity;

import com.mjzaymi.etherealvoid.common.blockentity.electricity.CableBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ThreeWireCableBlockEntity extends CableBlockEntity {
    public ThreeWireCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THREE_WIRE_CABLE_BE.get(), pos, state);
    }
}
