package com.mjzaymi.etherealvoid.block;

import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ElectrodePlate extends Block {
    public static final EnumProperty<ElectrodeMode> MODE = EnumProperty.create("mode", ElectrodeMode.class);

    public ElectrodePlate() {
        super(BlockBehaviour.Properties.of()
                .strength(5f, 3f)
                .sound(SoundType.METAL)
                .noOcclusion());

        this.registerDefaultState(this.stateDefinition.any().setValue(MODE, ElectrodeMode.UNASSIGNED));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(MODE, ElectrodeMode.UNASSIGNED);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        var structureOpt = CuboidStructure.findFromWall(pLevel, pPos);
        if (structureOpt.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!pLevel.isClientSide()) {
            ElectrodeMode currentMode = pState.getValue(MODE);

            ElectrodeMode nextMode = switch (currentMode) {
                case UNASSIGNED -> ElectrodeMode.NEGATIVE;
                case NEGATIVE -> ElectrodeMode.POSITIVE;
                case POSITIVE -> ElectrodeMode.UNASSIGNED;
            };

            CuboidStructure structure = structureOpt.get();

            // BFS
            Queue<BlockPos> queue = new LinkedList<>();
            Set<BlockPos> visited = new HashSet<>();

            BlockPos startPos = pPos.immutable();
            queue.add(startPos);
            visited.add(startPos);

            while (!queue.isEmpty()) {
                BlockPos currentPos = queue.poll();
                BlockState currentState = pLevel.getBlockState(currentPos);

                if (currentState.is(this)) {

                    changeMode(pLevel, currentPos, currentState, nextMode);

                    for (Direction dir : Direction.values()) {
                        BlockPos neighborPos = currentPos.relative(dir);

                        if (!visited.contains(neighborPos) && structure.members().contains(neighborPos)) {
                            BlockState neighborState = pLevel.getBlockState(neighborPos);

                            if (neighborState.is(this)) {
                                BlockPos immutableNeighbor = neighborPos.immutable();
                                visited.add(immutableNeighbor);
                                queue.add(immutableNeighbor);
                            }
                        }
                    }
                }
            }

            pPlayer.displayClientMessage(Component.literal("群组电极板模式已同步切换为: " + nextMode.getDisplayName()), true);
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    public static void changeMode(Level level, BlockPos pos, BlockState state, ElectrodeMode nextMode) {
        ElectrodeMode currentMode = state.getValue(MODE);
        if (currentMode == nextMode) return;

        level.setBlock(pos, state.setValue(MODE, nextMode), 3);

        /*
        if (level instanceof ServerLevel serverLevel) {
            // 从 unassigned 切换到 negative (U -> N)
            if (currentMode == ElectrodeMode.UNASSIGNED && nextMode == ElectrodeMode.NEGATIVE) {
                for (int i = 0; i < 25; i++) {
                    double px = pos.getX() + 0.05 + serverLevel.random.nextDouble() * 0.9;
                    double py = pos.getY() + 0.05 + serverLevel.random.nextDouble() * 0.9;
                    double pz = pos.getZ() + 0.05 + serverLevel.random.nextDouble() * 0.9;
                    serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 0, 0.0, 0.1, 0.0, 1.0);
                }
            }
            // 从任意工作状态切换回 unassigned (* -> U)
            else if (currentMode != ElectrodeMode.UNASSIGNED && nextMode == ElectrodeMode.UNASSIGNED) {
                for (int i = 0; i < 30; i++) {
                    double px = pos.getX() + 0.1 + serverLevel.random.nextDouble() * 0.8;
                    double py = pos.getY() + 0.1 + serverLevel.random.nextDouble() * 0.8;
                    double pz = pos.getZ() + 0.1 + serverLevel.random.nextDouble() * 0.8;
                    serverLevel.sendParticles(ParticleTypes.WITCH, px, py, pz, 0, 0.0, -0.08, 0.0, 1.0);
                }
            }
        } */
    }

    // --- 渲染属性配置 ---
    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return false;
    }

    public boolean isEmissiveRendering(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    public enum ElectrodeMode implements StringRepresentable {
        UNASSIGNED("unassigned", "未指定"),
        NEGATIVE("negative", "阴级"),
        POSITIVE("positive", "阳级");

        private final String name;
        private final String displayName;

        ElectrodeMode(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }
}