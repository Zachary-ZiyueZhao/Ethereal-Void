package com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.condition;

import com.mjzaymi.etherealvoid.blockentity.ReactionPoolBlockEntity;

public interface Checker {
    boolean match(ReactionPoolBlockEntity blockEntity);
}