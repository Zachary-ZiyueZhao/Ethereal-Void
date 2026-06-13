package com.mjzaymi.etherealvoid.blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class AntiCorrosionGlass extends Block {
    public AntiCorrosionGlass() {
        super(BlockBehaviour.Properties.copy(Blocks.GLASS)
                .strength(3f, 1f)
                .sound(SoundType.METAL));
    }
}
