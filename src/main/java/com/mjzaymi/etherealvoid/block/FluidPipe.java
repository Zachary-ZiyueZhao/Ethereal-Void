package com.mjzaymi.etherealvoid.block;

import com.mjzaymi.etherealvoid.blockentity.FluidPipeBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FluidPipe extends Block implements EntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final Map<BlockState, VoxelShape> SHAPE_CACHE = new HashMap<>();
    private static final VoxelShape CENTER_SHAPE = Block.box(6, 6, 6, 10, 10, 10);

    public FluidPipe() {
        super(BlockBehaviour.Properties.of()
                .strength(6f, 5f)
                .sound(SoundType.METAL)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false));
    }

    private boolean canConnectTo(LevelAccessor level, BlockPos currentPos, Direction dir) {
        BlockPos neighborPos = currentPos.relative(dir);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof FluidPipe) return true;

        BlockEntity be = level.getBlockEntity(neighborPos);
        if (be != null) {
            return be.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).isPresent();
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        boolean canConnect = canConnectTo(level, pos, dir);

        // 💡 修复：使用标准的 scheduleTick 替换不兼容的 getBlockTickQueue
        if (!level.isClientSide() && level instanceof Level realLevel) {
            realLevel.scheduleTick(pos, this, 1);
        }

        BooleanProperty property = getPropertyForDirection(dir);
        return state.setValue(property, canConnect);
    }

    // 💡 必须重写此方法，用于承接上面 scheduleTick 分发的计划刻度
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        // 刻度到达时，安全地刷新当处网络位置，完美防死锁
        FluidPipeBlockEntity.updateVirtualNetwork(level, pos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            FluidPipeBlockEntity.updateVirtualNetwork(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            super.onRemove(state, level, pos, newState, isMoving);
            if (!level.isClientSide()) {
                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = pos.relative(dir);
                    if (level.getBlockState(neighborPos).getBlock() instanceof FluidPipe) {
                        FluidPipeBlockEntity.updateVirtualNetwork(level, neighborPos);
                    }
                }
                FluidPipeBlockEntity.updateVirtualNetwork(level, pos);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_CACHE.computeIfAbsent(state, s -> {
            VoxelShape shape = CENTER_SHAPE;
            if (s.getValue(NORTH)) shape = Shapes.or(shape, Block.box(6, 6, 0, 10, 10, 6));
            if (s.getValue(SOUTH)) shape = Shapes.or(shape, Block.box(6, 6, 10, 10, 10, 16));
            if (s.getValue(EAST))  shape = Shapes.or(shape, Block.box(10, 6, 6, 16, 10, 10));
            if (s.getValue(WEST))  shape = Shapes.or(shape, Block.box(0, 6, 6, 6, 10, 10));
            if (s.getValue(UP))    shape = Shapes.or(shape, Block.box(6, 10, 6, 10, 16, 10));
            if (s.getValue(DOWN))  shape = Shapes.or(shape, Block.box(6, 0, 6, 10, 6, 10));
            return shape;
        });
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    public static BooleanProperty getPropertyForDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH; case SOUTH -> SOUTH;
            case EAST -> EAST; case WEST -> WEST;
            case UP -> UP; case DOWN -> DOWN;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPipeBlockEntity(pos, state);
    }
}