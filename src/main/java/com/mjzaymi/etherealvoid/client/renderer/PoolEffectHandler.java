package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.multiblock.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.common.util.fluid.MultiFluidTank;
import com.mjzaymi.etherealvoid.common.util.fluid.FluidSorter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class PoolEffectHandler {

    // 实时维护多方块内部不同温度阶段的日常环境粒子表现
    public static void tickAmbientEffects(Level level, CuboidStructure structure, float temperature) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel) || structure == null) return;

        RandomSource random = serverLevel.getRandom();
        if (random.nextFloat() >= 0.25f) return;

        BlockPos intMin = structure.interiorMin();
        BlockPos intMax = structure.interiorMax();

        // 根据绝对温度范围精细化匹配环境粒子
        if (temperature >= 273.15f + 1500f) {
            spawnRandomParticle(serverLevel, intMin, intMax, random, ParticleTypes.LAVA, 1);
            spawnRandomParticle(serverLevel, intMin, intMax, random, ParticleTypes.FLAME, 1);
            spawnRandomParticle(serverLevel, intMin, intMax, random, ParticleTypes.SMOKE, 1);
        } else if (temperature >= 273.15f + 1000f) {
            spawnRandomParticle(serverLevel, intMin, intMax, random, ParticleTypes.FLAME, 1);
            spawnRandomParticle(serverLevel, intMin, intMax, random, ParticleTypes.SMOKE, 1);
        } else if (temperature >= 273.15f + 500f) {
            spawnRandomParticle(serverLevel, intMin, intMax, random, ParticleTypes.SMOKE, 2);
        } else if (temperature >= 273.15f + 100f) {
            spawnRandomParticle(serverLevel, intMin, intMax, random, ParticleTypes.SMOKE, 1);
        } else if (temperature < 273.15f && temperature >= 173.15f) {
            spawnRandomParticle(serverLevel, intMin, intMax, random, ParticleTypes.SNOWFLAKE, 1);
        } else if (temperature < 173.15f) {
            spawnRandomParticle(serverLevel, intMin, intMax, random, ParticleTypes.SNOWFLAKE, 3);
        }
    }

    // 多方块碎裂时，内部流体的气化与蒸发粒子效果
    public static void spawnEvaporationParticles(Level level, CuboidStructure structure, MultiFluidTank tank) {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel) || structure == null) return;

        List<FluidStack> fluids = tank.getFluids();
        if (fluids == null || fluids.isEmpty() || fluids.stream().allMatch(fs -> fs.getAmount() <= 0)) return;

        BlockPos min = structure.interiorMin();
        BlockPos max = structure.interiorMax();
        float capacity = tank.getCapacity();
        if (capacity <= 0) return;

        float totalHeight = max.getY() - min.getY() + 1.0f;
        float currentAirDensity = 0.0012f;
        float totalLiquidHeight = 0;
        float totalGasHeight = 0;

        for (FluidStack fluidStack : fluids) {
            float amount = fluidStack.getAmount();
            if (amount <= 0) continue;
            float fillPercentage = Math.min(1.0f, amount / capacity);
            float height = fillPercentage * totalHeight;

            net.minecraft.world.level.material.Fluid fluid = fluidStack.getFluid();
            float density = 1.0f;

            ResourceLocation rl = ForgeRegistries.FLUIDS.getKey(fluid);
            if (rl != null) {
                String path = rl.getPath();
                if (path.endsWith("_flowing")) {
                    path = path.substring(0, path.length() - 8);
                }
                if (FluidSorter.DENSITY_MAP.containsKey(path)) {
                    density = FluidSorter.DENSITY_MAP.get(path);
                }
            }

            if (density < currentAirDensity) {
                totalGasHeight += height;
            } else {
                totalLiquidHeight += height;
            }
        }

        RandomSource random = level.random;
        int totalFluidAmount = fluids.stream().mapToInt(FluidStack::getAmount).sum();
        int baseCount = Math.max(200, Math.min(1000, totalFluidAmount / 50));

        if (totalLiquidHeight > 0) {
            double liquidMinY = min.getY();
            double liquidMaxY = min.getY() + totalLiquidHeight;
            int liquidCount = Math.round(baseCount * (totalLiquidHeight / totalHeight));

            for (int i = 0; i < Math.max(20, liquidCount); i++) {
                double x = min.getX() + random.nextDouble() * (max.getX() - min.getX() + 1);
                double z = min.getZ() + random.nextDouble() * (max.getZ() - min.getZ() + 1);
                double y = liquidMinY + random.nextDouble() * (liquidMaxY - liquidMinY);

                if (random.nextFloat() < 0.7f) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0.08, 0.03, 0.08, 0.01);
                } else {
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0.06, 0.03, 0.06, 0.02);
                }
            }
        }

        if (totalGasHeight > 0) {
            double gasMaxY = max.getY() + 1.0;
            double gasMinY = gasMaxY - totalGasHeight;
            int gasCount = Math.round(baseCount * (totalGasHeight / totalHeight));

            for (int i = 0; i < Math.max(20, gasCount); i++) {
                double x = min.getX() + random.nextDouble() * (max.getX() - min.getX() + 1);
                double z = min.getZ() + random.nextDouble() * (max.getZ() - min.getZ() + 1);
                double y = gasMinY + random.nextDouble() * (gasMaxY - gasMinY);

                if (random.nextFloat() < 0.7f) {
                    serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 1, 0.1, 0.05, 0.1, 0.005);
                } else {
                    serverLevel.sendParticles(ParticleTypes.ASH, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
                }
            }
        }
    }

    // 多方块破裂时外壳边界的冷凝贴地光环与灭火音效（带温度加成）
    public static void spawnCoolingParticle(Level level, CuboidStructure structure, float temperature) {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel) || structure == null) return;

        BlockPos min = structure.min();
        BlockPos max = structure.max();
        RandomSource random = level.random;

        // 播放灭火声
        double soundX = min.getX() + (max.getX() - min.getX() + 1.0) / 2.0;
        double soundY = min.getY() + (max.getY() - min.getY() + 1.0) / 2.0;
        double soundZ = min.getZ() + (max.getZ() - min.getZ() + 1.0) / 2.0;
        serverLevel.playSound(null, soundX, soundY, soundZ,
                net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.5F, 0.85F);

        double minX = min.getX() - 0.1;
        double maxX = max.getX() + 1.1;
        double minZ = min.getZ() - 0.1;
        double maxZ = max.getZ() + 1.1;
        double minY = min.getY();
        double maxY = max.getY() + 1.0;

        int sideHeight = (int) (maxY - minY);
        int sideLengthX = (int) (maxX - minX);
        int sideLengthZ = (int) (maxZ - minZ);

        // 1. 基础粒子数量计算
        int particlesPerFace = Math.max(3, (sideHeight + sideLengthX + sideLengthZ) / 3);

        // 2. 根据温度增幅系数
        // 超过 1000°C (1273.15K) 时，粒子数量翻 2.5 倍，下沉速度加剧
        double speedMultiplier = 1.0;
        if (temperature >= 273.15f + 1000f) {
            particlesPerFace = (int) (particlesPerFace * 2.5);
            speedMultiplier = 1.5; // 让超高热状态下的冷凝下沉显得更沉重、更有压迫感
        }

        // 四个外侧面零散随机打点
        for (int i = 0; i < particlesPerFace; i++) {
            double rX1 = minX + random.nextDouble() * (maxX - minX);
            double rY1 = minY + random.nextDouble() * (maxY - minY);
            spawnDynamicParticle(serverLevel, rX1, rY1, minZ, random, speedMultiplier);

            double rX2 = minX + random.nextDouble() * (maxX - minX);
            double rY2 = minY + random.nextDouble() * (maxY - minY);
            spawnDynamicParticle(serverLevel, rX2, rY2, maxZ, random, speedMultiplier);

            double rZ1 = minZ + random.nextDouble() * (maxZ - minZ);
            double rY3 = minY + random.nextDouble() * (maxY - minY);
            spawnDynamicParticle(serverLevel, minX, rY3, rZ1, random, speedMultiplier);

            double rZ2 = minZ + random.nextDouble() * (maxZ - minZ);
            double rY4 = minY + random.nextDouble() * (maxY - minY);
            spawnDynamicParticle(serverLevel, maxX, rY4, rZ2, random, speedMultiplier);
        }
    }

    private static void spawnDynamicParticle(ServerLevel level, double x, double y, double z, RandomSource random, double speedMultiplier) {
        double motionX = (random.nextDouble() - 0.5) * 0.03 * speedMultiplier;
        double motionZ = (random.nextDouble() - 0.5) * 0.03 * speedMultiplier;
        double motionY = (-0.03 - random.nextDouble() * 0.03) * speedMultiplier;
        level.sendParticles(ParticleTypes.CLOUD, x, y, z, 0, motionX, motionY, motionZ, 1.0);
    }

    private static void spawnRandomParticle(ServerLevel level, BlockPos min, BlockPos max, RandomSource random, ParticleOptions type, int count) {
        double x = min.getX() + random.nextDouble() * (max.getX() - min.getX() + 1);
        double y = min.getY() + random.nextDouble() * (max.getY() - min.getY() + 1);
        double z = min.getZ() + random.nextDouble() * (max.getZ() - min.getZ() + 1);
        level.sendParticles(type, x, y, z, count, 0.02, 0.02, 0.02, 0.005);
    }
}