package com.mjzaymi.etherealvoid.common.block.electriccity;

import com.mjzaymi.etherealvoid.common.blockentity.electricity.CableBlockEntity;
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

import java.util.HashMap;
import java.util.Map;

public class CableBlock <I extends CableBlockEntity> extends BaseEntityBlock {
    // 6个方向的连接状态
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    // 碰撞箱缓存 Map (避免 getShape 时高频重复计算)
    private static final Map<BlockState, VoxelShape> SHAPE_CACHE = new HashMap<>();
    public final float size;
    private final float startPos;
    private final float endPos;
    private final VoxelShape CENTER_SHAPE;
    private final BlockEntityType<I> beType;

    public CableBlock(BlockEntityType<I> beType) {
        super(BlockBehaviour.Properties.of()
                .strength(6f, 5f)
                .sound(SoundType.METAL)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false));
        this.size = 4;
        this.startPos = (16-size) / 2;
        this.endPos = (16+size) / 2;
        this.CENTER_SHAPE = Block.box(startPos, startPos, startPos, endPos, endPos, endPos);
        this.beType = beType;
    }

    public boolean canConnectTo(LevelAccessor level, BlockPos currentPos, Direction dir) {
        BlockPos neighborPos = currentPos.relative(dir);
        BlockState neighborState = level.getBlockState(neighborPos);
        return neighborState.getBlock() instanceof CableBlock;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BooleanProperty property = getPropertyForDirection(dir);
        return state.setValue(property, canConnectTo(level, pos, dir));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_CACHE.computeIfAbsent(state, s -> {
            VoxelShape shape = CENTER_SHAPE;
            if (s.getValue(NORTH)) shape = Shapes.or(shape, Block.box(startPos, startPos, 0, endPos, endPos, startPos));
            if (s.getValue(SOUTH)) shape = Shapes.or(shape, Block.box(startPos, startPos, endPos, endPos, endPos, 16));
            if (s.getValue(EAST))  shape = Shapes.or(shape, Block.box(endPos, startPos, startPos, 16, endPos, endPos));
            if (s.getValue(WEST))  shape = Shapes.or(shape, Block.box(0, startPos, startPos, startPos, endPos, endPos));
            if (s.getValue(UP))    shape = Shapes.or(shape, Block.box(startPos, endPos, startPos, endPos, 16, endPos));
            if (s.getValue(DOWN))  shape = Shapes.or(shape, Block.box(startPos, 0, startPos, endPos, startPos, endPos));
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return beType.create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, beType, (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }
}