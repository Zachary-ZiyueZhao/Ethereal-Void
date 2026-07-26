package com.mjzaymi.etherealvoid.block;

import com.mjzaymi.etherealvoid.blockentity.FluidPipeBlockEntity;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.BlockHitResult;
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

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        // 只有当玩家空手时才触发连接/断开逻辑（方便后续如果想添加扳手等工具时不冲突）
        if (heldItem.isEmpty()) {
            // 获取玩家右键点击的管道具体面
            Direction clickedDir = hit.getDirection();
            BooleanProperty prop = getPropertyForDirection(clickedDir);
            boolean currentlyConnected = state.getValue(prop);

            BlockPos neighborPos = pos.relative(clickedDir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (!currentlyConnected) {
                // 💡 尝试【建立连接】
                if (canConnectTo(level, pos, clickedDir)) {
                    if (!level.isClientSide()) {
                        // 1. 设置当前管道该面为连接状态
                        BlockState newState = state.setValue(prop, true);
                        level.setBlock(pos, newState, 3);

                        // 2. 如果邻居也是管道，让邻居也反向连接过来
                        if (neighborState.getBlock() instanceof FluidPipe) {
                            BooleanProperty oppProp = getPropertyForDirection(clickedDir.getOpposite());
                            level.setBlock(neighborPos, neighborState.setValue(oppProp, true), 3);
                        }

                        // 3. 刷新管网状态
                        FluidPipeBlockEntity.updateVirtualNetwork(level, pos);
                    }

                    // 播放金属机械音效
                    level.playSound(player, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.2F);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            } else {
                // 💡 尝试【断开连接】
                if (!level.isClientSide()) {
                    // 立刻作废当前整条网络
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof FluidPipeBlockEntity pipeBE && pipeBE.currentNetwork != null) {
                        pipeBE.currentNetwork.invalidate();
                    }

                    BlockState newState = state.setValue(prop, false);
                    level.setBlock(pos, newState, 3);

                    if (neighborState.getBlock() instanceof FluidPipe) {
                        BooleanProperty oppProp = getPropertyForDirection(clickedDir.getOpposite());
                        level.setBlock(neighborPos, neighborState.setValue(oppProp, false), 3);
                        // 让邻居重新寻路 (形成一半网络)
                        FluidPipeBlockEntity.updateVirtualNetwork(level, neighborPos);
                    }
                    // 让自己重新寻路 (形成另一半网络)
                    FluidPipeBlockEntity.updateVirtualNetwork(level, pos);
                }
                level.playSound(player, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    // 💡 新增：放置时仅连接玩家右键点击的那个面
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        Direction clickedFace = context.getClickedFace();
        // 如果玩家点在方块的上面 (UP)，管道放在上方，管道应该向下 (DOWN) 连接
        Direction connectDir = clickedFace.getOpposite();

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos().relative(connectDir);

        if (canConnectTo(level, context.getClickedPos(), connectDir)) {
            state = state.setValue(getPropertyForDirection(connectDir), true);
        }
        return state;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            // 💡 新增：如果是对着另一个管道放置的，强制让那个旧管道也反向连接过来
            for (Direction dir : Direction.values()) {
                if (state.getValue(getPropertyForDirection(dir))) {
                    BlockPos neighborPos = pos.relative(dir);
                    BlockState neighborState = level.getBlockState(neighborPos);
                    if (neighborState.getBlock() instanceof FluidPipe) {
                        level.setBlock(neighborPos, neighborState.setValue(getPropertyForDirection(dir.getOpposite()), true), 3);
                    }
                }
            }
            FluidPipeBlockEntity.updateVirtualNetwork(level, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // 💡 修复：不再自动连接！只有当邻居被破坏或不合法时，才断开连接。
        if (state.getValue(getPropertyForDirection(dir))) {
            if (!canConnectTo(level, pos, dir)) {
                state = state.setValue(getPropertyForDirection(dir), false);
                if (!level.isClientSide() && level instanceof Level realLevel) {
                    realLevel.scheduleTick(pos, this, 1);
                }
            }
        }
        return state;
    }

    private boolean canConnectTo(LevelAccessor level, BlockPos currentPos, Direction dir) {
        BlockPos neighborPos = currentPos.relative(dir);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof FluidPipe) return true;

        BlockEntity be = level.getBlockEntity(neighborPos);
        if (be != null) {
            // 这里你可以稍微收紧判断，目前是只要有 FluidHandler 就允许
            return be.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).isPresent();
        }
        return false;
    }

    // 💡 必须重写此方法，用于承接上面 scheduleTick 分发的计划刻度
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        // 刻度到达时，安全地刷新当处网络位置，完美防死锁
        FluidPipeBlockEntity.updateVirtualNetwork(level, pos);
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {

            // 1. 在方块消失前，拿到它的 BE，立刻作废它所在的网络！防止虚空传输！
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FluidPipeBlockEntity pipeBE && pipeBE.currentNetwork != null) {
                pipeBE.currentNetwork.invalidate();
            }

            super.onRemove(state, level, pos, newState, isMoving);

            // 2. 通知所有相连的邻居：强制断开连向此处的面，并各自重新组网
            if (!level.isClientSide()) {
                for (Direction dir : Direction.values()) {
                    // 只处理之前连接着的面
                    if (state.getValue(getPropertyForDirection(dir))) {
                        BlockPos neighborPos = pos.relative(dir);
                        BlockState neighborState = level.getBlockState(neighborPos);

                        if (neighborState.getBlock() instanceof FluidPipe) {
                            // 强制关闭邻居连向这里的口，防止产生幽灵连接
                            BooleanProperty oppProp = getPropertyForDirection(dir.getOpposite());
                            level.setBlock(neighborPos, neighborState.setValue(oppProp, false), 3);

                            // 让邻居作为起点，重新寻路生成新网络
                            FluidPipeBlockEntity.updateVirtualNetwork(level, neighborPos);
                        }
                    }
                }
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