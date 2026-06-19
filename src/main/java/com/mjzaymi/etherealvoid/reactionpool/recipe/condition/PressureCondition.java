package com.mjzaymi.etherealvoid.reactionpool.recipe.condition;

import com.mjzaymi.etherealvoid.util.math.Range;

public class PressureCondition extends Condition {
    public PressureCondition(Range range) {
        super(blockEntity -> range.match(blockEntity.getPressure()));
    }
}
