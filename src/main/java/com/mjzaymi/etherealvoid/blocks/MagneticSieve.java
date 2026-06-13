package com.mjzaymi.etherealvoid.blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MagneticSieve extends Block {
    public MagneticSieve() {
		super(BlockBehaviour.Properties.of()
                .strength(3f)
                .requiresCorrectToolForDrops());
    }
}
