package com.mjzaymi.etherealvoid.block;

import com.mjzaymi.etherealvoid.block.entity.FluidPipeBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

public class FluidPipe extends BaseEntityBlock {
    // 6个方向的连接状态
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    // 碰撞箱缓存 Map (避免 getShape 时高频重复计算)
    private static final Map<BlockState, VoxelShape> SHAPE_CACHE = new HashMap<>();
    private static final VoxelShape CENTER_SHAPE = Block.box(6, 6, 6, 10, 10, 10); // 中心 6x6x6 的核心

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

    // 判定旁边是不是能连（是同类管道，或者带流体接口的机器）
    private boolean canConnectTo(LevelAccessor level, BlockPos currentPos, Direction dir) {
        BlockPos neighborPos = currentPos.relative(dir);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof FluidPipe) return true;

        BlockEntity be = level.getBlockEntity(neighborPos);
        if (be != null) {
            // 检查对方是否有流体 Capability (NeoForge 对应 lookup，Forge 对应 getCapability)
            return be.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).isPresent();
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // 当邻居改变时，自动更新当前连接臂的布尔值
        BooleanProperty property = getPropertyForDirection(dir);
        return state.setValue(property, canConnectTo(level, pos, dir));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 动态拼装并缓存 VoxelShape
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FluidPipeBlockEntity(pos, state); }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.FLUID_PIPE_BE.get(), FluidPipeBlockEntity::tick);
    }
}