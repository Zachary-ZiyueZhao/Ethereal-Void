package com.mjzaymi.etherealvoid.registration;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.virtualminer.VeinRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ModVeins {

    private static final List<VeinRecipe> VEINS = new ArrayList<>();

    public static void init() {
        VEINS.clear();

        // ==================== 矿脉 1：铁铜富矿脉 ====================
        List<VeinRecipe.MiningDrop> ironCopperDrops = new ArrayList<>();
        ironCopperDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.RAW_IRON), 0.10F)); // 10% 粗铁
        ironCopperDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.RAW_COPPER), 0.05F)); // 5% 粗铜
        ironCopperDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.GRAVEL), 0.35F)); // 35% 沙砾
        ironCopperDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.COBBLESTONE), 0.50F)); // 50% 原石

        // 参数：矿脉ID，生成权重(100)
        VEINS.add(new VeinRecipe(ResourceLocation.tryParse(EtherealVoid.MOD_ID + ":iron_copper_vein"), 100, ironCopperDrops));

        // ==================== 矿脉 2：黄金稀有矿脉 ====================
        List<VeinRecipe.MiningDrop> goldDrops = new ArrayList<>();
        goldDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.RAW_GOLD), 0.15F)); // 15% 粗金
        goldDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.DEEPSLATE), 0.85F)); // 85% 深层碎石

        // 权重设为 20，代表它比铁铜矿脉稀有 5 倍
        VEINS.add(new VeinRecipe(ResourceLocation.tryParse(EtherealVoid.MOD_ID + ":gold_vein"), 20, goldDrops));
    }

    public static List<VeinRecipe> getAllVeins() {
        if (VEINS.isEmpty()) init();
        return VEINS;
    }
}