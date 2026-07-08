package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.client.renderer.VirtualMinerItemRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class VirtualMinerItem extends BlockItem {
    public static final HumanoidModel.ArmPose HANDS_OVER_HEAD_POSE =
            HumanoidModel.ArmPose.create("HANDS_OVER_HEAD", true, (model, entity, arm) -> {
                // X轴旋转为负数代表向上抬起，-3.0F 接近 -π，即手臂完全竖直向上
                model.rightArm.xRot = -3.0F;
                model.rightArm.yRot = 0F;

                model.leftArm.xRot = -3.0F;
                model.leftArm.yRot = 0F;
            });

    public VirtualMinerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return VirtualMinerItemRenderer.INSTANCE;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return HANDS_OVER_HEAD_POSE;
            }
        });
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos centerPos = context.getClickedPos();

        // 检查 3x3x2 空间内是否有无法被替换的方块
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos checkPos = centerPos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    if (!state.canBeReplaced(context)) {
                        return InteractionResult.FAIL; // 空间被占用，拒绝放置
                    }
                }
            }
        }

        // 检查 3x3x2 空间内是否有任何实体/玩家卡位
        AABB bounds = new AABB(
                centerPos.getX() - 1, centerPos.getY(), centerPos.getZ() - 1,
                centerPos.getX() + 2, centerPos.getY() + 2, centerPos.getZ() + 2
        );
        if (!level.getEntities(null, bounds).isEmpty()) {
            return InteractionResult.FAIL; // 有生物或玩家卡位，拒绝放置
        }

        return super.place(context);
    }
}