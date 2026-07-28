package com.mjzaymi.etherealvoid.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluidPipeFilterItem extends Item {

    public FluidPipeFilterItem(Properties pProperties) {
        super(pProperties);
    }

    // 💡 1. 检查如果有流体 Tag，就散发附魔光效
    @Override
    public boolean isFoil(ItemStack pStack) {
        return pStack.hasTag() && pStack.getTag().contains("FilterFluid");
    }

    // 💡 2. 在 Tooltip 中显示当前过滤的流体名称
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pStack.hasTag() && pStack.getTag().contains("FilterFluid")) {
            String fluidRegName = pStack.getTag().getString("FilterFluid");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidRegName));

            if (fluid != null && fluid != Fluids.EMPTY) {
                // 显示如: "过滤流体: 水"
                pTooltipComponents.add(Component.translatable("tooltip.etherealvoid.filtered_fluid")
                        .append(": ")
                        .append(Component.translatable(fluid.getFluidType().getDescriptionId()))
                        .withStyle(ChatFormatting.AQUA));
            }
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.etherealvoid.filter_empty").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    // 💡 3. 当右键方块（如你的管道网络）时的逻辑
    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        if (!level.isClientSide()) {
            ItemStack stack = pContext.getItemInHand();
            // 假设你的管道 BlockEntity 是 FluidPipeBlockEntity
            // BlockEntity be = level.getBlockEntity(pContext.getClickedPos());
            // if (be instanceof FluidPipeBlockEntity pipe) {
            //     if (stack.hasTag() && stack.getTag().contains("FilterFluid")) {
            //         String fluidName = stack.getTag().getString("FilterFluid");
            //         pipe.setFilter(fluidName); // 调用你管道的方法
            //         pContext.getPlayer().displayClientMessage(Component.literal("管道已配置为只传输: " + fluidName), true);
            //         return InteractionResult.SUCCESS;
            //     }
            // }
        }
        return super.useOn(pContext);
    }
}