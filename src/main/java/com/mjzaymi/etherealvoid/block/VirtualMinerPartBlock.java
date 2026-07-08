package com.mjzaymi.etherealvoid.block;

import com.mjzaymi.etherealvoid.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class VirtualMinerPartBlock extends Block {
    public VirtualMinerPartBlock(Properties properties) {
        super(properties);
    }

    // 当仆从方块被破坏时，反向寻找并破坏主方块
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 0; y++) { // 主方块只可能在同层或下一层
                for (int z = -1; z <= 1; z++) {
                    BlockPos targetPos = pos.offset(x, y, z);
                    if (level.getBlockState(targetPos).is(ModBlocks.VIRTUAL_MINER.get())) {
                        level.destroyBlock(targetPos, !player.isCreative());
                        return;
                    }
                }
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE; // 核心：完全隐形
    }
}