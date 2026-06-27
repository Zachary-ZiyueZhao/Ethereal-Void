package com.mjzaymi.etherealvoid.block;

import com.mjzaymi.etherealvoid.blockentity.ReactionPoolFluidIOBlockEntity;
import com.mjzaymi.etherealvoid.common.util.fluid.FluidSorter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class ReactionPoolFluidIO extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<FluidIOMode> MODE = EnumProperty.create("mode", FluidIOMode.class);
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public ReactionPoolFluidIO() {
        super(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(MODE, FluidIOMode.INPUT)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE, FACING);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    // 💡 实现 EntityBlock 必须指定的渲染类型，否则方块在游戏里会变成隐形的
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(MODE, FluidIOMode.INPUT)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack heldItem = pPlayer.getItemInHand(pHand);

        var itemFluidCap = heldItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

        if (itemFluidCap.isPresent()) {
            if (!pLevel.isClientSide()) {
                BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
                if (blockEntity != null) {
                    blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, pHit.getDirection()).ifPresent(blockFluidHandler -> {
                        IFluidHandlerItem itemHandler = itemFluidCap.resolve().get();
                        FluidIOMode currentMode = pState.getValue(MODE);

                        if (currentMode == FluidIOMode.OUTPUT) {
                            extractFluidByDensity(blockFluidHandler, itemHandler, pPlayer, pHand);
                        } else {
                            FluidUtil.interactWithFluidHandler(pPlayer, pHand, blockFluidHandler);
                        }
                    });
                }
            }
            return InteractionResult.sidedSuccess(pLevel.isClientSide());
        }

        if (!pLevel.isClientSide()) {
            FluidIOMode currentMode = pState.getValue(MODE);
            FluidIOMode nextMode = currentMode == FluidIOMode.INPUT ? FluidIOMode.OUTPUT : FluidIOMode.INPUT;

            pLevel.setBlock(pPos, pState.setValue(MODE, nextMode), 3);

            String modeText = nextMode == FluidIOMode.INPUT ? "INPUT" : "OUTPUT";
            pPlayer.displayClientMessage(Component.literal("Fluid I/O mode has switched to: " + modeText), true);
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    private void extractFluidByDensity(IFluidHandler blockHandler, IFluidHandlerItem itemHandler, Player player, InteractionHand hand) {
        FluidStack targetFluid = FluidStack.EMPTY;
        float lowestDensity = Float.MAX_VALUE;

        for (int i = 0; i < blockHandler.getTanks(); i++) {
            FluidStack fluidInTank = blockHandler.getFluidInTank(i);
            if (!fluidInTank.isEmpty() && fluidInTank.getAmount() > 0) {
                String fluidName = ForgeRegistries.FLUIDS.getKey(fluidInTank.getFluid()).getPath();
                float density = FluidSorter.DENSITY_MAP.getOrDefault(fluidName, 1.0f);

                if (density < lowestDensity) {
                    FluidStack simulatedDrain = blockHandler.drain(fluidInTank, IFluidHandler.FluidAction.SIMULATE);
                    if (!simulatedDrain.isEmpty() && itemHandler.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE) > 0) {
                        lowestDensity = density;
                        targetFluid = fluidInTank;
                    }
                }
            }
        }

        if (!targetFluid.isEmpty()) {
            int itemEmptySpace = itemHandler.getTankCapacity(0) - (itemHandler.getFluidInTank(0).isEmpty() ? 0 : itemHandler.getFluidInTank(0).getAmount());
            int maxDrainAmount = Math.min(targetFluid.getAmount(), itemEmptySpace);

            if (maxDrainAmount > 0) {
                FluidStack drained = blockHandler.drain(new FluidStack(targetFluid.getFluid(), maxDrainAmount), IFluidHandler.FluidAction.EXECUTE);
                itemHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                player.setItemInHand(hand, itemHandler.getContainer());
            }
        }
    }

    // 💡 接口 EntityBlock 的标准方法
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ReactionPoolFluidIOBlockEntity(pPos, pState);
    }
}