package com.mjzaymi.etherealvoid.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Blocks;

public class ResistiveHeater extends Block {
    public ResistiveHeater() {
        super(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .strength(5f, 3f)
                .sound(SoundType.METAL));
    }
}