package com.mjzaymi.etherealvoid.block;

import com.mjzaymi.etherealvoid.multiblock.hugeelectrodeplate.HugeElectrodePlateManager;
import com.mjzaymi.etherealvoid.multiblock.reactionpool.CuboidStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

    // 1. 定义新的属性
    public static final EnumProperty<RenderKind> RENDER_KIND = EnumProperty.create("render_kind", RenderKind.class);

    // 2. 在构造函数中注册默认状态（修改你的构造函数）
    public ElectrodePlate() {
        super(BlockBehaviour.Properties.of()
                .strength(5f, 3f)
                .sound(SoundType.METAL)
                .noOcclusion());

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(MODE, ElectrodeMode.UNASSIGNED)
                .setValue(RENDER_KIND, RenderKind.DEFAULT)); // 新增默认值
    }

    // 3. 将新属性加入到定义中
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE, RENDER_KIND); // 确保两个属性都在这里
    }

    // 4. 修改放置时的默认状态
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(MODE, ElectrodeMode.UNASSIGNED)
                .setValue(RENDER_KIND, RenderKind.DEFAULT);
    }

    // 5. 新增一个用于控制方块显示形态的内部枚举
    public enum RenderKind implements StringRepresentable {
        DEFAULT("default"),
        CENTER("center"),
        INVISIBLE("invisible");

        private final String name;
        RenderKind(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
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

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            HugeElectrodePlateManager.markBlockChanged(serverLevel, pos);
        }

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
        // 如果当前方块处于隐形状态，允许渲染相邻的面，防止透视漏洞
        if (state.getValue(RENDER_KIND) == RenderKind.INVISIBLE) {
            return false;
        }
        return super.skipRendering(state, adjacentBlockState, side);
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

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
        // 只有当放下的不是同一种方块（防止右键切换 MODE 时误触发重算）且处于服务端时
        if (!pState.is(pOldState.getBlock()) && !pLevel.isClientSide() && pLevel instanceof ServerLevel serverLevel) {
            HugeElectrodePlateManager.markBlockChanged(serverLevel, pPos);
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoved) {
        // 只有当变成的不是同一种方块（防止右键切换 MODE 时误触发重算）且处于服务端时
        if (!pState.is(pNewState.getBlock())) {
            if (!pLevel.isClientSide() && pLevel instanceof ServerLevel serverLevel) {
                HugeElectrodePlateManager.markBlockChanged(serverLevel, pPos);
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoved);
        }
    }

    /**
     * 当邻近方块发生变化时触发（用来处理外部 5x5x5 被打破，或者由于邻近方块被破坏导致结构失效的逻辑提示）
     */
    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pFromPos, pIsMoving);
        if (!pLevel.isClientSide()) {
            // 如果玩家在原本已经成立的 3x3x3 外部 5x5x5 圈内又放了/拆了东西，可以在这里触发重新检查或失效逻辑。
            // 由于目前是纯观赏作用，上面的 setPlacedBy 已经能完美满足“成型提示”了。
        }
    }
}