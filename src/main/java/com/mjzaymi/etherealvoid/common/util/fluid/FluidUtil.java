package com.mjzaymi.etherealvoid.common.util.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidUtil {
    public static String getNameFromFluid(Fluid fluid) {
        ResourceLocation registryName = ForgeRegistries.FLUIDS.getKey(fluid);
        if (registryName == null) return "";
        return registryName.getPath();
    }

    public static String getNameFromFluidType(FluidType fluidType) {
        ResourceLocation registryName = ForgeRegistries.FLUID_TYPES.get().getKey(fluidType);
        if (registryName == null) return "";
        return registryName.getPath();
    }

    /**
     * 逆流而上寻找水源方块
     * * @param level    当前世界
     * @param startPos 开始溯源的起始坐标（通常是发电机检测到的水流位置）
     * @return 水源方块的坐标。如果找不到（例如水源刚被破坏），返回 null。
     */
    public static BlockPos findWaterSource(Level level, BlockPos startPos) {
        BlockPos currentPos = startPos;
        // 设置一个最大搜索步数，防止因为复杂的涡流导致死循环卡死服务器
        int maxSteps = 64;

        for (int i = 0; i < maxSteps; i++) {
            FluidState currentFluid = level.getFluidState(currentPos);

            // 1. 终结条件：如果当前方块本身就是水源，直接返回坐标
            if (currentFluid.is(Fluids.WATER) && currentFluid.isSource()) {
                return currentPos;
            }

            // 2. 垂直回溯：优先检查上方。只要上方有水，不管是不是水源，上游肯定在上面
            BlockPos posAbove = currentPos.above();
            FluidState fluidAbove = level.getFluidState(posAbove);
            if (fluidAbove.is(Fluids.WATER) || fluidAbove.is(Fluids.FLOWING_WATER)) {
                currentPos = posAbove;
                continue; // 重新开始下一轮循环
            }

            // 3. 水平回溯：上方没水，说明我们在平地上，需要寻找周围水量更大的方块
            int currentAmount = currentFluid.getAmount();
            BlockPos upstreamPos = null;
            int maxAmountFound = currentAmount;

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos neighborPos = currentPos.relative(dir);
                FluidState neighborFluid = level.getFluidState(neighborPos);

                if (neighborFluid.is(Fluids.WATER) || neighborFluid.is(Fluids.FLOWING_WATER)) {
                    int neighborAmount = neighborFluid.getAmount();
                    // 寻找周围水量最大的方块（水量越大越靠近水源）
                    if (neighborAmount > maxAmountFound) {
                        maxAmountFound = neighborAmount;
                        upstreamPos = neighborPos;
                    }
                }
            }

            // 如果找到了更高水量的上游，就移动过去
            if (upstreamPos != null) {
                currentPos = upstreamPos;
            } else {
                // 既不是水源，上面没水，周围也没有水量更大的水...
                // 说明这块水流成了“死水”（比如玩家用方块堵住了源头，但水流还没来得及消失）
                return null;
            }
        }

        // 如果超过了 64 步还没找到，可能是地形过于复杂，为了保护服务器性能强制中断
        return null;
    }
}