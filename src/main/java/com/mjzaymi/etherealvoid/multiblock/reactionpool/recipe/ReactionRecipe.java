package com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe;

import com.mjzaymi.etherealvoid.blockentity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.condition.Checker;
import com.mjzaymi.etherealvoid.registration.ModReactionRecipes;
import com.mjzaymi.etherealvoid.common.util.GameUtil;
import com.mjzaymi.etherealvoid.common.util.fluid.MultiFluidTank;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

public class ReactionRecipe {
    public final String id;
    public final int syncType;
    public final int cookTicks;
    public final ResultOrCost cost;
    public final ResultOrCost result;
    public final Checker checker;
    public int ticksRemained;
    public ReactionRecipe(String id, int syncType, int cookTicks, ResultOrCost cost, ResultOrCost result, Checker checker) {
        this.id = id;
        this.syncType = syncType;
        this.cookTicks = cookTicks;
        this.cost = cost;
        this.result = result;
        this.checker = checker;
        this.ticksRemained = cookTicks;
    }

    public static ReactionRecipe of(CompoundTag tag) {
        var result = ModReactionRecipes.findById(tag.getString("id"));
        if (result!=null) result.ticksRemained = tag.getInt("ticksRemained");
        return result;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("id", id);
        tag.putInt("ticksRemained", ticksRemained);
        return tag;
    }

    public boolean tick(int tick) {
        this.ticksRemained -= tick;
        return this.ticksRemained <= 0;
    }

    public boolean matchCondition(ReactionPoolBlockEntity blockEntity) {
        return checker.match(blockEntity);
    }

    public boolean costsEnough(List<ItemStack> items, List<FluidStack> fluids) {
        var costList = (List<?>) cost.ingredients;
        for (Object o : costList) {
            if (o instanceof ItemStack itemStack) {
                boolean found = false;
                for (ItemStack item : items)
                    if (Ingredient.of(itemStack.getItem()).test(item) &&
                            item.getCount() >= itemStack.getCount()) {
                        found = true;
                        break;
                    }
                if (!found) return false;
            }
            if (o instanceof FluidStack fluidStack) {
                boolean found = false;
                for (FluidStack fluid : fluids)
                    if (fluid.isFluidEqual(fluidStack) &&
                            fluid.getAmount() >= fluidStack.getAmount()) {
                        found = true;
                        break;
                    }
                if (!found) return false;
            }
        }
        return true;
    }

    public void cost(final List<ItemStack> items, final MultiFluidTank tank) {
        for (Object o : cost.ingredients) {
            if (o instanceof ItemStack item) GameUtil.subtractItemFromList(items, item);
            if (o instanceof FluidStack fluidStack) tank.drain(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void output(final List<ItemStack> items, final MultiFluidTank tank, ResultOrCost resultOrCost) {
        for (Object o : resultOrCost.ingredients) {
            if (o instanceof ItemStack item) GameUtil.addItemToList(items, item);
            if (o instanceof FluidStack fluidStack) tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public void returnCost(final List<ItemStack> items, final MultiFluidTank tank) {
        output(items, tank, cost);
    }

    //TODO ODDS
    public void result(final List<ItemStack> items, final MultiFluidTank tank) {
        output(items, tank, result);
    }

    public ReactionRecipe copyNew() {
        return new ReactionRecipe(id, syncType, cookTicks, cost, result, checker);
    }
}
