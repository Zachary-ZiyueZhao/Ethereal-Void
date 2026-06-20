package com.mjzaymi.etherealvoid.registration;

import com.mjzaymi.etherealvoid.reactionpool.recipe.ReactionRecipe;
import com.mjzaymi.etherealvoid.reactionpool.recipe.ResultOrCost;
import com.mjzaymi.etherealvoid.reactionpool.recipe.SyncType;
import com.mjzaymi.etherealvoid.reactionpool.recipe.condition.Condition;
import com.mjzaymi.etherealvoid.reactionpool.recipe.condition.TemperatureCondition;
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
    //TODO Change the example.
    public static ReactionRecipe EXAMPLE = register(new ReactionRecipe(
            "example", SyncType.RECIPE_SYNC, 80,
            new ResultOrCost(List.of(
                    new ItemStack(Items.BUCKET, 1))),
            new ResultOrCost(
                    List.of(new FluidStack(ModFluids.SOURCE_SOAP_WATER.get(), 300))),
            //An example of a complex condition
            be -> {
                float temperature = be.getTemperature();
                float pressure = be.getPressure();
                if (true) { //Your code here
                    return true; //return true if matches
                }
                return false;
            }
    ));


    public static ReactionRecipe register(ReactionRecipe recipe) {
        registeredRecipes.add(recipe);
        return recipe;
    }

    public static ReactionRecipe findById(String id) {
        return GameUtil.findById(registeredRecipes, id);
    }
}
