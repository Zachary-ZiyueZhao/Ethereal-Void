package com.mjzaymi.etherealvoid.util.fluid;

import net.minecraftforge.fluids.FluidStack;

import java.util.Comparator;

public class FluidSorter {
    public static final Comparator<FluidStack> AMOUNT_SORTER = (o1, o2) -> o1.getAmount() > o2.getAmount() ? 0 : 1;
    public static final Comparator<FluidStack> DENSITY_SORTER = (o1, o2) -> {
        //TODO DENSITY SORTER
        return o2.getAmount() - o1.getAmount();
    };
}
