package com.mjzaymi.etherealvoid.reactionpool.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultOrCost {
    public final float dropOdds;
    public final Map<Integer, Float> odds;
    public final List<?> ingredients;

    public static Map<Integer, Float> generateBlankMap(List<?> list) {
        Map<Integer, Float> map = new HashMap<>();
        for (int i=0;i<list.size();i++) {
            map.put(i, 1.0f);
        }
        return map;
    }

    public ResultOrCost(List<?> ingredients) {
        this(1.0f, ingredients);
    }

    public ResultOrCost(float dropOdds, List<?> ingredients) {
        this(dropOdds, generateBlankMap(ingredients), ingredients);
    }

    public ResultOrCost(float dropOdds, Map<Integer, Float> odds, List<?> ingredients) {
        this.dropOdds = dropOdds;
        this.odds = odds;
        this.ingredients = ingredients;
    }
}