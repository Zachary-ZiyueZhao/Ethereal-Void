package com.mjzaymi.etherealvoid.common.util.fluid;

import net.minecraftforge.fluids.FluidStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class FluidSorter {
    public static final Map<String, Float> DENSITY_MAP = new HashMap<>();
    public static final Comparator<FluidStack> AMOUNT_SORTER = (o1, o2) -> o2.getAmount() - o1.getAmount();
    public static final Comparator<FluidStack> DENSITY_SORTER = (o1, o2) -> Math.round(
            1000000f*(
                    getDensityById(FluidUtil.getNameFromFluid(o2.getFluid()))-
                            getDensityById(FluidUtil.getNameFromFluid(o1.getFluid()))));

    static {
        DENSITY_MAP.put("water", 1f);
        DENSITY_MAP.put("lava", 3.3f);
    }

    public static float getDensityById(String id) {
        return DENSITY_MAP.getOrDefault(id, -1f);
    }
}
