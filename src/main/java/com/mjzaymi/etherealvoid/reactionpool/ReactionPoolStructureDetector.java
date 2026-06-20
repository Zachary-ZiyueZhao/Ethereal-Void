package com.mjzaymi.etherealvoid.reactionpool;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.block.entity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReactionPoolStructureDetector {
    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.isClientSide()) return;

        BlockState placedBlock = event.getPlacedBlock();
        boolean isPoolBlock = placedBlock.is(ModBlocks.ANTI_CORROSION_GLASS.get())
                || placedBlock.is(ModBlocks.POOL_MONITOR.get())
                || placedBlock.is(ModBlocks.STEEL_CASING.get());
        if (!isPoolBlock) return;

        BlockPos pos = event.getPos();
        level.getServer().execute(() -> {
            Optional<CuboidStructure> opt = CuboidStructure.findFromWallAndCorner(level, pos);
            if (opt.isEmpty()) return;

            CuboidStructure structure = opt.get();

            structure.dropInterior(level);

            BlockPos anchor = structure.min();
            level.setBlock(anchor, level.getBlockState(anchor), 3);

            BlockEntity be = level.getBlockEntity(anchor);

            if (be instanceof ReactionPoolBlockEntity pool) {
                pool.setStructure(structure);
                pool.updateChangeState(true);
            }
        });
    }
}
