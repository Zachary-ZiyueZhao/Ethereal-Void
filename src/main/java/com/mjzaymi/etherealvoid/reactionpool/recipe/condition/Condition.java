package com.mjzaymi.etherealvoid.reactionpool.recipe.condition;

import com.mjzaymi.etherealvoid.block.entity.ReactionPoolBlockEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Condition implements Checker {
    public static final Condition ALWAYS_TRUE = new Condition();
    private final List<Checker> checkers = new ArrayList<>();
    public Condition(Checker...checker) {
        this(Arrays.asList(checker));
    }
    public Condition(List<Checker> checkers) {
        this.checkers.addAll(checkers);
    }

    @Override
    public boolean match(ReactionPoolBlockEntity blockEntity) {
        for (Checker checker : checkers) if (!checker.match(blockEntity)) return false;
        return true;
    }

    public Condition and(Checker checker) {
        List<Checker> newCheckers = new ArrayList<>(this.checkers);
        if (checker instanceof Condition condition) newCheckers.addAll(condition.checkers);
        else newCheckers.add(checker);
        return new Condition(newCheckers);
    }
}