package com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.condition;

import com.mjzaymi.etherealvoid.common.util.math.Range;

public class PressureCondition extends Condition {
    public PressureCondition(Range range) {
        super(blockEntity -> range.match(blockEntity.getPressure()));
    }
}
