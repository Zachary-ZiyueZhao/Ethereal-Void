package com.mjzaymi.etherealvoid.common.util.fluid;

import net.minecraftforge.fluids.FluidStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class FluidSorter {
    public static final Map<String, Float> DENSITY_MAP = new HashMap<>();
    public static final Comparator<FluidStack> AMOUNT_SORTER = (o1, o2) -> o2.getAmount() - o1.getAmount();
    public static final Comparator<FluidStack> DENSITY_SORTER = (o1, o2) ->
            Math.round(
                    100f*( //乘以100f以提升两位小数的精度
                            getDensityById(o2.getFluid().getFluidType().getDescriptionId())-
                                    getDensityById(o1.getFluid().getFluidType().getDescriptionId())));

    public static float getDensityById(String id) {
        return DENSITY_MAP.getOrDefault(id, -1f);
    }
}
