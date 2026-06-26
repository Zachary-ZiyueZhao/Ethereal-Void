package com.mjzaymi.etherealvoid.block;

import com.mjzaymi.etherealvoid.blockentity.ReactionPoolFluidIOBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

public class ReactionPoolFluidIO extends BaseEntityBlock {
    public static final EnumProperty<FluidIOMode> MODE = EnumProperty.create("mode", FluidIOMode.class);
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public ReactionPoolFluidIO() {
        super(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(MODE, FluidIOMode.INPUT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        // 核心改动：先尝试进行流体容器（桶）的交互
        // FluidUtil 会自动帮我们处理：扣除流体、扣除/替换玩家手里的桶、播放声音等。
        // 它会根据我们在 BlockEntity 里暴露的 Capability 自动判断是填入还是抽进去。
        if (FluidUtil.interactWithFluidHandler(pPlayer, pHand, pLevel, pPos, pHit.getDirection())) {
            // 如果桶交互成功（比如成功把水倒进去了，或者成功抽出来了），直接返回成功，不切换模式
            return InteractionResult.sidedSuccess(pLevel.isClientSide());
        }

        // 如果玩家手里拿的不是流体容器（或者是空手、普通方块），才触发“切换输入/输出模式”的逻辑
        if (!pLevel.isClientSide()) {
            FluidIOMode currentMode = pState.getValue(MODE);
            FluidIOMode nextMode = currentMode == FluidIOMode.INPUT ? FluidIOMode.OUTPUT : FluidIOMode.INPUT;

            pLevel.setBlock(pPos, pState.setValue(MODE, nextMode), 3);

            String modeText = nextMode == FluidIOMode.INPUT ? "输入 (INPUT)" : "输出 (OUTPUT)";
            pPlayer.displayClientMessage(Component.literal("反应池流体I/O模式已切换为: " + modeText), true);
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ReactionPoolFluidIOBlockEntity(pPos, pState);
    }
}