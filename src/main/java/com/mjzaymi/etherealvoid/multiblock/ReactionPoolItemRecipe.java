package com.mjzaymi.etherealvoid.multiblock;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record ReactionPoolItemRecipe(
        String id,
        Ingredient first,
        int firstCount,
        Ingredient second,
        int secondCount,
        ItemStack result,
        int cookTicks
) {
    public boolean matchesFirst(ItemStack stack) {
        return first.test(stack) && stack.getCount() >= firstCount;
    }

    public boolean matchesSecond(ItemStack stack) {
        return second.test(stack) && stack.getCount() >= secondCount;
    }
}
