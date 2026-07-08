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

        // =========================================================================
        // 【T5 等级：工业垃圾/地壳表层】—— 极其常见，基本上是纯污染或垫脚石
        // =========================================================================

        // 1. 废石基岩带 (权重: 500)
        List<VeinRecipe.MiningDrop> wasteRockDrops = new ArrayList<>();
        wasteRockDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.COBBLESTONE), 0.40F));   // 40% 原石
        wasteRockDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.GRANITE), 0.20F));       // 20% 花岗岩
        wasteRockDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.DIORITE), 0.20F));       // 20% 闪长岩
        wasteRockDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.ANDESITE), 0.20F));      // 20% 安山岩
        VEINS.add(new VeinRecipe(createId("waste_rock_vein"), 500, wasteRockDrops));

        // 2. 砂土沉积带 (权重: 300)
        List<VeinRecipe.MiningDrop> sedimentDrops = new ArrayList<>();
        sedimentDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.GRAVEL), 0.50F));         // 50% 沙砾
        sedimentDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.TUFF), 0.30F));           // 30% 凝灰岩
        sedimentDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.CLAY_BALL), 0.20F));      // 20% 粘土球
        VEINS.add(new VeinRecipe(createId("sediment_vein"), 300, sedimentDrops));


        // =========================================================================
        // 【T4 等级：基础能源】—— 维持工厂前期运转的燃料大户
        // =========================================================================

        // 3. 浅层煤炭富集带 (权重: 150)
        List<VeinRecipe.MiningDrop> coalDrops = new ArrayList<>();
        coalDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.COAL), 0.45F));              // 45% 煤炭
        coalDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.COBBLESTONE), 0.45F));       // 45% 原石
        coalDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.RAW_IRON), 0.10F));          // 10% 混杂一些粗铁
        VEINS.add(new VeinRecipe(createId("coal_vein"), 150, coalDrops));


        // =========================================================================
        // 【T3 等级：基础金属】—— 最经典的矿机主产区
        // =========================================================================

        // 4. 铁铜共生矿脉 (权重: 100)
        List<VeinRecipe.MiningDrop> ironCopperDrops = new ArrayList<>();
        ironCopperDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.RAW_IRON), 0.25F));     // 25% 粗铁
        ironCopperDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.RAW_COPPER), 0.15F));   // 15% 粗铜
        ironCopperDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.GRAVEL), 0.20F));       // 20% 沙砾
        ironCopperDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.COBBLESTONE), 0.40F));  // 40% 原石
        VEINS.add(new VeinRecipe(createId("iron_copper_vein"), 100, ironCopperDrops));

        // 5. 锌与白云石矿床 (权重: 80) —— 💡 模组特色基础金属脉
        List<VeinRecipe.MiningDrop> zincDolomiteDrops = new ArrayList<>();
        zincDolomiteDrops.add(new VeinRecipe.MiningDrop(new ItemStack(ModItems.ZINC_CONCENTRATE.get()), 0.20F)); // 20% 锌精矿
        zincDolomiteDrops.add(new VeinRecipe.MiningDrop(new ItemStack(ModItems.DOLOMITE.get()), 0.30F));         // 30% 白云石
        zincDolomiteDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.DEEPSLATE), 0.50F));                 // 50% 深层碎石
        VEINS.add(new VeinRecipe(createId("zinc_dolomite_vein"), 80, zincDolomiteDrops));


        // =========================================================================
        // 【T2 等级：高级工业】—— 中后期发展电解、合金工艺的核心来源
        // =========================================================================

        // 6. 铝土冰晶石矿脉 (权重: 50) —— 💡 模组核心炼铝矿脉
        List<VeinRecipe.MiningDrop> bauxiteDrops = new ArrayList<>();
        bauxiteDrops.add(new VeinRecipe.MiningDrop(new ItemStack(ModItems.BAUXITE.get()), 0.25F));    // 25% 铝土矿
        bauxiteDrops.add(new VeinRecipe.MiningDrop(new ItemStack(ModItems.CRYOLITE.get()), 0.10F));   // 10% 冰晶石（助熔剂）
        bauxiteDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.DIORITE), 0.65F));             // 65% 伴生闪长岩
        VEINS.add(new VeinRecipe(createId("bauxite_cryolite_vein"), 50, bauxiteDrops));

        // 7. 红石青金石深脉 (权重: 45) —— 科技自动化不可或缺的红蓝线
        List<VeinRecipe.MiningDrop> techDrops = new ArrayList<>();
        techDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.REDSTONE), 0.30F));           // 30% 红石粉
        techDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.LAPIS_LAZULI), 0.15F));       // 15% 青金石
        techDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.DEEPSLATE), 0.55F));          // 55% 深层碎石
        VEINS.add(new VeinRecipe(createId("redstone_lapis_vein"), 45, techDrops));


        // =========================================================================
        // 【T1 等级：稀有贵重】—— 极低概率摇出来的黄金与宝藏区块
        // =========================================================================

        // 8. 深层黄金富金脉 (权重: 20)
        List<VeinRecipe.MiningDrop> goldDrops = new ArrayList<>();
        goldDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.RAW_GOLD), 0.20F));           // 20% 粗金
        goldDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.DEEPSLATE), 0.80F));          // 80% 深层碎石
        VEINS.add(new VeinRecipe(createId("gold_vein"), 20, goldDrops));

        // 9. 璀璨宝石矿脉 (权重: 8) —— 挖矿机一开，钻石滚滚来
        List<VeinRecipe.MiningDrop> gemDrops = new ArrayList<>();
        gemDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.DIAMOND), 0.05F));             // 5% 钻石
        gemDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.EMERALD), 0.03F));             // 3% 绿宝石
        gemDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.DEEPSLATE), 0.92F));          // 92% 深层碎石
        VEINS.add(new VeinRecipe(createId("diamond_emerald_vein"), 8, gemDrops));

        // 10. 远古镁盐沉淀层 (权重: 15) —— 💡 提取原镁的高级原材料脉
        List<VeinRecipe.MiningDrop> magnesiumDrops = new ArrayList<>();
        magnesiumDrops.add(new VeinRecipe.MiningDrop(new ItemStack(ModItems.IMPURE_MAGNESIUM_SALT.get()), 0.15F)); // 15% 粗镁盐
        magnesiumDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.TUFF), 0.85F));                          // 85% 凝灰岩
        VEINS.add(new VeinRecipe(createId("magnesium_salt_vein"), 15, magnesiumDrops));

        // 11. 奇迹综合富集带 (权重: 3) —— 全图最稀有的奇迹区块，什么都有，综合大丰收
        List<VeinRecipe.MiningDrop> miracleDrops = new ArrayList<>();
        miracleDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.DIAMOND), 0.08F));
        miracleDrops.add(new VeinRecipe.MiningDrop(new ItemStack(ModItems.BAUXITE.get()), 0.15F));
        miracleDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.RAW_GOLD), 0.15F));
        miracleDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.RAW_IRON), 0.22F));
        miracleDrops.add(new VeinRecipe.MiningDrop(new ItemStack(Items.DEEPSLATE), 0.40F));
        VEINS.add(new VeinRecipe(createId("miracle_rich_vein"), 3, miracleDrops));
    }

    public static List<VeinRecipe> getAllVeins() {
        if (VEINS.isEmpty()) init();
        return VEINS;
    }

    // 辅助工具方法：快速生成 ResourceLocation
    private static ResourceLocation createId(String path) {
        return ResourceLocation.tryParse(EtherealVoid.MOD_ID + ":" + path);
    }
}