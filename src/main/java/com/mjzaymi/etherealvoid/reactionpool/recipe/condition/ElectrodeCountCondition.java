package com.mjzaymi.etherealvoid.reactionpool.recipe.condition;

import com.mjzaymi.etherealvoid.blockentity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;

public class ElectrodeCountCondition implements Checker {
    private final int requiredCount;

    /**
     * @param requiredCount 电极片集群数量，理论上都是 2
     */
    public ElectrodeCountCondition(int requiredCount) {
        this.requiredCount = requiredCount;
    }

    @Override
    public boolean match(ReactionPoolBlockEntity blockEntity) {
        CuboidStructure structure = blockEntity.getStructure();
        if (structure == null) {
            return false;
        }

        int actualCount = structure.countIndependentElectromines(blockEntity.getLevel());

        return actualCount == this.requiredCount;
    }
}