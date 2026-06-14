package com.mjzaymi.etherealvoid.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SteelCasing extends Block {
    public SteelCasing() {
        super(BlockBehaviour.Properties.of()
                .strength(6f, 5f)
                .sound(SoundType.METAL));
    }
}
