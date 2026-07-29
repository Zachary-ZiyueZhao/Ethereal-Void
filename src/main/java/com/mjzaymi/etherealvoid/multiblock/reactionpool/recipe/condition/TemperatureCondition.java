package com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.condition;

import com.mjzaymi.etherealvoid.common.util.math.Range;

public class TemperatureCondition extends Condition {
    public TemperatureCondition(Range range) {
        super(blockEntity -> range.match(blockEntity.getTemperature()));
    }
}
