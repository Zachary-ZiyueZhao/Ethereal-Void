package com.mjzaymi.etherealvoid.blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ResistiveHeater extends Block {
    public ResistiveHeater() {
        super(BlockBehaviour.Properties.of()
                .strength(5f, 3f)
                .sound(SoundType.METAL));
    }
}
