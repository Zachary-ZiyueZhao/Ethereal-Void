package com.mjzaymi.etherealvoid.virtualminer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class VeinRecipe {
    private final ResourceLocation id;
    private final int weight; // 生成权重，比如普通铁矿脉 100，金矿脉 5
    private final List<MiningDrop> drops; // 包含的掉落物列表

    public VeinRecipe(ResourceLocation id, int weight, List<MiningDrop> drops) {
        this.id = id;
        this.weight = weight;
        this.drops = drops;
    }

    public ResourceLocation getId() { return id; }

    public int getWeight() { return weight; }
    public List<MiningDrop> getDrops() { return drops; }

    // 内部类：定义单种矿物的掉落率
    public static class MiningDrop {
        public final ItemStack item;
        public final float chance; // 概率，如 0.1F 代表 10%

        public MiningDrop(ItemStack item, float chance) {
            this.item = item;
            this.chance = chance;
        }
    }
}