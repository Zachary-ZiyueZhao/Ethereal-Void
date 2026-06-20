package com.mjzaymi.etherealvoid.common.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;

public interface ItemEntityModifier {
    ItemEntityModifier DELTA_MOVEMENT_MODIFIER = (itemEntity, random) -> {
        // 【可选】设置物品的初始速度
        // 默认情况下，new ItemEntity 会自带一点随机散射的速度。
        // 如果你希望物品静止生成（比如平移或做特定动画），可以强行清空速度：
        // itemEntity.setDeltaMovement(0, 0, 0);
        // 或者给它一个微微向上的喷射速度（原版打碎方块的效果）：
        itemEntity.setDeltaMovement(
                (random.nextFloat() - 0.5) * 0.1,
                0.2,
                (random.nextFloat() - 0.5) * 0.1
        );
        return itemEntity;
    };
    ItemEntity modify(ItemEntity itemEntity, RandomSource random);
}