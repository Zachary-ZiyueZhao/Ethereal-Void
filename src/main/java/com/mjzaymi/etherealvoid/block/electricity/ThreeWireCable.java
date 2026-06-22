package com.mjzaymi.etherealvoid.block.electricity;

import com.mjzaymi.etherealvoid.blockentity.electricity.ThreeWireCableBlockEntity;
import com.mjzaymi.etherealvoid.common.block.electriccity.CableBlock;
import com.mjzaymi.etherealvoid.common.blockentity.electricity.CableBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ThreeWireCable extends CableBlock<ThreeWireCableBlockEntity> {
    public ThreeWireCable() {
        super(ModBlockEntities.THREE_WIRE_CABLE_BE.get());
    }
}
