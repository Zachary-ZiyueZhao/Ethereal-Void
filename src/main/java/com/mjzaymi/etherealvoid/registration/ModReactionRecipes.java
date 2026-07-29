package com.mjzaymi.etherealvoid.registration;

import com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.condition.ElectrodeCountCondition;
import com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.ReactionRecipe;
import com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.ResultOrCost;
import com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.SyncType;
import com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.condition.Condition;
import com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.condition.TemperatureCondition;
import com.mjzaymi.etherealvoid.common.util.GameUtil;
import com.mjzaymi.etherealvoid.common.util.math.Range;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModReactionRecipes {
    public static final List<ReactionRecipe> registeredRecipes = new ArrayList<>();
    public static ReactionRecipe DROP_WATER_BUCKET = register(new ReactionRecipe(
            "drop_water_bucket", SyncType.ASYNC, 0,
            new ResultOrCost(List.of(
                    new ItemStack(Items.WATER_BUCKET, 1))),
            new ResultOrCost(
                    Arrays.asList(new FluidStack(Fluids.WATER, 1000),
                            new ItemStack(Items.BUCKET, 1))),
            Condition.ALWAYS_TRUE
    ));
    public static ReactionRecipe DROP_LAVA_BUCKET = register(new ReactionRecipe(
            "drop_lava_bucket", SyncType.ASYNC, 0,
            new ResultOrCost(List.of(
                    new ItemStack(Items.LAVA_BUCKET, 1))),
            new ResultOrCost(
                    Arrays.asList(new FluidStack(Fluids.LAVA, 1000),
                            new ItemStack(Items.BUCKET, 1))),
            Condition.ALWAYS_TRUE
    ));
    public static ReactionRecipe WATER_TO_ICE = register(new ReactionRecipe(
            "water_to_ice", SyncType.RECIPE_SYNC, 80,
            new ResultOrCost(List.of(
                    new FluidStack(Fluids.WATER, 1000))),
            new ResultOrCost(
                    List.of(new ItemStack(Items.ICE, 1))),
            //An example of and(Condition) function
            Condition.ALWAYS_TRUE.and(new TemperatureCondition(new Range(0, 273.15)))
    ));
    public static ReactionRecipe ALUMINIUM_MELTING = register(new ReactionRecipe(
            "aluminium_melting", SyncType.RECIPE_SYNC, 40,
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.ALUMINIUM_INGOT.get(), 1))),
            new ResultOrCost(
                    List.of(new FluidStack(ModFluids.SOURCE_MOLTEN_ALUMINIUM.get(), 144))),
            Condition.ALWAYS_TRUE.and(new TemperatureCondition(new Range(0, 2743.15)))
    ));
    public static ReactionRecipe ALUMINIUM_SOLIDIFY = register(new ReactionRecipe(
            "aluminium_solidify",
            SyncType.RECIPE_SYNC,
            40,
            new ResultOrCost(List.of(
                    new FluidStack(ModFluids.SOURCE_MOLTEN_ALUMINIUM.get(), 144))),
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.ALUMINIUM_INGOT.get(), 1))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(300, 933.15)))
    ));
    public static ReactionRecipe CRYOLITE_MELTING = register(new ReactionRecipe(
            "cryolite_melting",
            SyncType.RECIPE_SYNC,
            20,
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.CRYOLITE.get(), 1))),
            new ResultOrCost(List.of(
                    new FluidStack(ModFluids.SOURCE_MOLTEN_CRYOLITE.get(), 144))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(1283.15, 1773.15)))
    ));// TODO: temperature > 1773.15 时缓慢分解
    public static ReactionRecipe CRYOLITE_SOLIDIFY = register(new ReactionRecipe(
            "cryolite_solidify",
            SyncType.RECIPE_SYNC,
            20,
            new ResultOrCost(List.of(
                    new FluidStack(ModFluids.SOURCE_MOLTEN_CRYOLITE.get(), 144))),
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.CRYOLITE.get(), 1))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(0, 1283.15)))
    ));
    public static ReactionRecipe ZINC_MELTING = register(new ReactionRecipe(
            "zinc_melting",
            SyncType.RECIPE_SYNC,
            20,
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.ZINC_INGOT.get(), 1))),
            new ResultOrCost(List.of(
                    new FluidStack(ModFluids.SOURCE_MOLTEN_ZINC.get(), 144))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(693.15, 1173.15)))
    ));// TODO: temperature > 1173.15 时蒸发
    public static ReactionRecipe ZINC_SOLIDIFY = register(new ReactionRecipe(
            "zinc_solidify",
            SyncType.RECIPE_SYNC,
            20,
            new ResultOrCost(List.of(
                    new FluidStack(ModFluids.SOURCE_MOLTEN_ZINC.get(), 144))),
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.ZINC_INGOT.get(), 1))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(0, 693.15)))
    ));
    public static ReactionRecipe MAGNESIUM_MELTING = register(new ReactionRecipe(
            "magnesium_melting",
            SyncType.RECIPE_SYNC,
            20,
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.MAGNESIUM_INGOT.get(), 1))),
            new ResultOrCost(List.of(
                    new FluidStack(ModFluids.SOURCE_MOLTEN_MAGNESIUM.get(), 144))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(923.15, 1363.15)))
    ));// TODO: temperature > 1363.15 时蒸发
    public static ReactionRecipe MAGNESIUM_SOLIDIFY = register(new ReactionRecipe(
            "magnesium_solidify",
            SyncType.RECIPE_SYNC,
            20,
            new ResultOrCost(List.of(
                    new FluidStack(ModFluids.SOURCE_MOLTEN_MAGNESIUM.get(), 144))),
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.MAGNESIUM_INGOT.get(), 1))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(0, 923.15)))
    ));

    public static ReactionRecipe BRINE_ELECTROLYSIS = register(new ReactionRecipe(
            "brine_electrolysis",
            SyncType.RECIPE_SYNC,
            20,
            new ResultOrCost(List.of(
                    new FluidStack(Fluids.WATER, 200))),
            new ResultOrCost(List.of(
                    new FluidStack(ModFluids.SOURCE_HYDROGEN.get(), 20),
                    new FluidStack(ModFluids.SOURCE_CHLORINE.get(), 50),
                    new FluidStack(ModFluids.SOURCE_SODIUM_HYDROXIDE_SOLUTION.get(), 100))),
            Condition.ALWAYS_TRUE
                    .and(new TemperatureCondition(new Range(273.15, 373.15)))
                    .and(new ElectrodeCountCondition(2))
    ));

    public static ReactionRecipe BAUXITE_CALCINATION = register(new ReactionRecipe(
            "bauxite_calcination",
            SyncType.RECIPE_SYNC,
            200,
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.BAUXITE.get(), 1))),
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.ALUMINIUM_OXIDE.get(), 1))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(1300.15, 2500)))
    ));
    public static ReactionRecipe MOLTEN_ELECTROLYTE_PREPARATION = register(new ReactionRecipe(
            "molten_electrolyte_preparation",
            SyncType.RECIPE_SYNC,
            100,
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.CRYOLITE.get(), 1),
                    new ItemStack(ModItems.ALUMINIUM_OXIDE.get(), 1))),
            new ResultOrCost(List.of(
                    new FluidStack(ModFluids.SOURCE_MOLTEN_ELECTROLYTE.get(), 288))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(1273.15, 1773.15)))
    ));// TODO: temperature > 1773.15 时缓慢分解
    public static ReactionRecipe DOLOMITE_CALCINATION = register(new ReactionRecipe(
            "dolomite_calcination",
            SyncType.RECIPE_SYNC,
            200,
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.DOLOMITE.get(), 1))),
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.IMPURE_MAGNESIUM_SALT.get(), 1))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(1200.15, 2500)))
    ));
    public static ReactionRecipe MAGNESIUM_SALT_ELECTROLYSIS = register(new ReactionRecipe(
            "magnesium_salt_electrolysis",
            SyncType.RECIPE_SYNC,
            200,
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.IMPURE_MAGNESIUM_SALT.get(), 1))),
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.RAW_MAGNESIUM.get(), 1))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(1000.15, 2000)))
    ));
    public static ReactionRecipe MAGNESIUM_REFINING = register(new ReactionRecipe(
            "magnesium_refining",
            SyncType.RECIPE_SYNC,
            100,
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.RAW_MAGNESIUM.get(), 1))),
            new ResultOrCost(List.of(
                    new ItemStack(ModItems.MAGNESIUM_INGOT.get(), 1))),
            Condition.ALWAYS_TRUE.and(
                    new TemperatureCondition(new Range(950.15, 2000)))
    ));


    public static ReactionRecipe register(ReactionRecipe recipe) {
        registeredRecipes.add(recipe);
        return recipe;
    }

    public static ReactionRecipe findById(String id) {
        return GameUtil.findById(registeredRecipes, id);
    }
}
