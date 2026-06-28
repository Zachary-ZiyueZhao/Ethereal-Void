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
    public static final int CAPACITY = 1000;

    public FluidStorageTankItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, CAPACITY);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        FluidStack fluidStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);

        if (fluidStack.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.ethereal_void.fluid_storage_tank.empty"));
        } else {
            String fluidName = fluidStack.getDisplayName().getString();
            tooltip.add(Component.literal(fluidName + ": " + fluidStack.getAmount() + " / " + CAPACITY + " mB"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        FluidStack fluidStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        Component baseName = Component.translatable(this.getDescriptionId());

        if (fluidStack.isEmpty()) {
            return baseName;
        }

        return Component.literal("").append(baseName).append(" (").append(fluidStack.getDisplayName()).append(" ").append(String.valueOf(fluidStack.getAmount())).append("mB)");
    }
}