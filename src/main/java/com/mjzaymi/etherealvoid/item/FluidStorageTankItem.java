package com.mjzaymi.etherealvoid.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluidStorageTankItem extends Item {
    public static final int CAPACITY = 1000; // 最大容量 1000 mB

    public FluidStorageTankItem(Properties properties) {
        super(properties.stacksTo(1)); // 储罐必须是单分堆叠
    }

    // 💡 为物品注入流体处理能力
    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, CAPACITY);
    }

    // 💡 动态显示储罐内的流体信息
    // 💡 动态显示储罐内的流体信息
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // 💡 加上 .orElse(FluidStack.EMPTY) 来解包 Optional 类型
        FluidStack fluidStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);

        if (fluidStack.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.ethereal_void.fluid_storage_tank.empty"));
        } else {
            // 显示示例：硫酸 (450 / 1000 mB)
            String fluidName = fluidStack.getDisplayName().getString();
            tooltip.add(Component.literal(fluidName + ": " + fluidStack.getAmount() + " / " + CAPACITY + " mB"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}