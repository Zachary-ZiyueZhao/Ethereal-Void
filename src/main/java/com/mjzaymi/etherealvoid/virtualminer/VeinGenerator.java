package com.mjzaymi.etherealvoid.virtualminer;

import com.mjzaymi.etherealvoid.registration.ModVeins;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.List;

public class VeinGenerator {

    // 根据中心方块的位置，计算当前区块里究竟有什么虚拟矿脉
    public static VeinRecipe getVeinForChunk(Level level, BlockPos centerPos) {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        ChunkPos chunkPos = new ChunkPos(centerPos);
        long worldSeed = serverLevel.getSeed();

        // 将世界种子与区块的 X, Z 坐标进行哈希混淆
        // 这样可以确保：只要种子和坐标不变，算出来的 chunkSeed 永远唯一
        long chunkSeed = Mth.getSeed(chunkPos.x, 0, chunkPos.z) ^ worldSeed;
        RandomSource random = RandomSource.create(chunkSeed);

        // 获取当前游戏中所有注册了的“矿脉配方”
        // (这里假设你已经把所有 VeinRecipe 存在了一个全局 List 里，或者通过 RecipeManager 获取)
        List<VeinRecipe> allVeins = ModVeins.getAllVeins();
        if (allVeins.isEmpty()) return null;

        // 计算总权重
        int totalWeight = 0;
        for (VeinRecipe vein : allVeins) {
            totalWeight += vein.getWeight();
        }

        // 开始根据区块随机数摇号
        int roll = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (VeinRecipe vein : allVeins) {
            currentWeight += vein.getWeight();
            if (roll < currentWeight) {
                return vein; // 恭喜，这个区块命中该矿脉！
            }
        }

        return null;
    }
}