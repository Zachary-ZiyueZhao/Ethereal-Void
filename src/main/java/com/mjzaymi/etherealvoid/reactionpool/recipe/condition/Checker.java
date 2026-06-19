package com.mjzaymi.etherealvoid.reactionpool.recipe.condition;

import com.mjzaymi.etherealvoid.block.entity.ReactionPoolBlockEntity;

public interface Checker {
    boolean match(ReactionPoolBlockEntity blockEntity);
}