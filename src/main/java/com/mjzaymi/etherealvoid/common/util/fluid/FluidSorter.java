package com.mjzaymi.etherealvoid.common.util.fluid;

import net.minecraftforge.fluids.FluidStack;

import java.util.Comparator;

public class FluidSorter {
    public static final Comparator<FluidStack> AMOUNT_SORTER = (o1, o2) -> o2.getAmount() - o1.getAmount();
    public static final Comparator<FluidStack> DENSITY_SORTER = (o1, o2) -> o2.getFluid().getFluidType().getDensity() - o1.getFluid().getFluidType().getDensity();
}
