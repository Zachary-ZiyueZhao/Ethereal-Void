package com.mjzaymi.etherealvoid.common.util;

import com.mjzaymi.etherealvoid.multiblock.reactionpool.recipe.ReactionRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.mjzaymi.etherealvoid.common.util.ItemEntityModifier.DELTA_MOVEMENT_MODIFIER;

public class GameUtil {
    public static ReactionRecipe findById(List<ReactionRecipe> list, String id) {
        for (var recipe : list) if (recipe.id.equals(id)) return recipe;
        return null;
    }

    public static void addItemsToList(final List<ItemStack> list, List<?> ingredients) {
        for (Object o : ingredients) {
            if (!(o instanceof ItemStack itemStack)) continue;
            addItemToList(list, itemStack);
        }
    }

    public static void addItemToList(final List<ItemStack> list, ItemStack item) {
        if (item.isEmpty()) return;
        ItemStack incoming = item.copy();
        for (ItemStack itemStack : list) {
            if (ItemStack.isSameItemSameTags(itemStack, incoming)) {
                int maxStackSize = itemStack.getMaxStackSize();
                int currentCount = itemStack.getCount();
                if (currentCount < maxStackSize) {
                    int acceptAmount = Math.min(maxStackSize - currentCount, incoming.getCount());
                    itemStack.grow(acceptAmount);
                    incoming.shrink(acceptAmount);
                    if (incoming.isEmpty()) return;
                }
            }
        }

        while (!incoming.isEmpty()) {
            int maxStackSize = incoming.getMaxStackSize();
            ItemStack newStack = incoming.copy();
            if (incoming.getCount() > maxStackSize) {
                newStack.setCount(maxStackSize);
                incoming.shrink(maxStackSize);
            } else incoming.setCount(0);
            list.add(newStack);
        }
    }

    public static void subtractItemFromList(final List<ItemStack> items, ItemStack item) {
        if (item.isEmpty()) return;
        int toRemove = item.getCount();
        Iterator<ItemStack> iterator = items.iterator();
        while (iterator.hasNext()) {
            ItemStack itemStack = iterator.next();
            if (ItemStack.isSameItemSameTags(itemStack, item)) {
                int currentCount = itemStack.getCount();
                if (currentCount <= toRemove) {
                    toRemove -= currentCount;
                    iterator.remove();
                } else {
                    itemStack.shrink(toRemove);
                    toRemove = 0;
                }
                if (toRemove <= 0) break;
            }
        }
    }

    public static float getPreferredTotalPressure(float current) {
        if (current <= 10f) return 10f;
        return (float) Math.pow(10, Math.ceil(Math.log10(current)));
    }

    public static float getPreferredTotalTemperature(float current) {
        if (current <= 1000f) return 1000f;
        return (float) Math.pow(10, Math.ceil(Math.log10(current)));
    }

    public static void spawnItemRandomlyInArea(Level level, BlockPos min, BlockPos max, ItemStack ...list) {
        spawnItemRandomlyInArea(level, min, max, Arrays.asList(list), DELTA_MOVEMENT_MODIFIER);
    }

    public static void spawnItemRandomlyInArea(Level level, BlockPos min, BlockPos max, List<ItemStack> list) {
        spawnItemRandomlyInArea(level, min, max, list, DELTA_MOVEMENT_MODIFIER);
    }

    public static void spawnItemRandomlyInArea(Level level, BlockPos min, BlockPos max, List<ItemStack> list, ItemEntityModifier modifier) {
        for (ItemStack stack : list) {
            spawnItemRandomlyInArea(level, min, max, stack, modifier);
        }
    }

    public static void spawnItemRandomlyInArea(Level level, BlockPos min, BlockPos max, ItemStack stack) {
        spawnItemRandomlyInArea(level, min, max, stack, DELTA_MOVEMENT_MODIFIER);
    }


    public static Vec3 getRandomPosition(RandomSource random, BlockPos min, BlockPos max) {
        double padding = 0.5;
        double minX = min.getX() + padding;
        double maxX = (max.getX() + 1.0) - padding;
        double minY = min.getY() + padding;
        double maxY = (max.getY() + 1.0) - padding;
        double minZ = min.getZ() + padding;
        double maxZ = (max.getZ() + 1.0) - padding;

        double randomX = minX + random.nextDouble() * (maxX - minX);
        double randomY = minY + random.nextDouble() * (maxY - minY);
        double randomZ = minZ + random.nextDouble() * (maxZ - minZ);

        return new Vec3(randomX, randomY, randomZ);
    }


    public static void spawnItemRandomlyInArea(Level level, BlockPos min, BlockPos max, ItemStack stack, ItemEntityModifier modifier) {
        if (stack.isEmpty()) return;
        RandomSource random = level.getRandom();
        var vec3 = getRandomPosition(random, min, max);
        ItemEntity itemEntity = new ItemEntity(level, vec3.x, vec3.y, vec3.z, stack.copy());
        itemEntity = modifier.modify(itemEntity, random);

        level.addFreshEntity(itemEntity);
    }

    public static void mergeItemsInArea(Level level, AABB area) {
        // 1. 抓取区域内所有还活着（未被销毁）的物品实体
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area, ItemEntity::isAlive);

        // 如果物品少于2个，根本不需要合并
        if (items.size() < 2) return;

        // 2. 开始双重循环对比
        for (int i = 0; i < items.size(); i++) {
            ItemEntity entityA = items.get(i);
            if (!entityA.isAlive()) continue;

            ItemStack stackA = entityA.getItem();
            // 如果 A 已经是空的，或者已经达到了最大堆叠上限（比如64），跳过
            if (stackA.isEmpty() || stackA.getCount() >= stackA.getMaxStackSize()) continue;

            for (int j = i + 1; j < items.size(); j++) {
                ItemEntity entityB = items.get(j);
                // 确保 B 还活着，且不是 A 自己
                if (!entityB.isAlive()) continue;

                ItemStack stackB = entityB.getItem();
                if (stackB.isEmpty()) continue;

                // 3. 【核心判定】检查 A 和 B 是否是完全同一种物品（物品相同且 NBT/组件 相同）
                // 💡 避坑提示：
                // - 如果是 1.20.1 及以下：用 ItemStack.isSameItemSameTags(stackA, stackB)
                // - 如果是 1.20.5 及以上（组件化时代）：用 ItemStack.isSameItemSameComponents(stackA, stackB)
                if (ItemStack.isSameItemSameTags(stackA, stackB)) {

                    int maxStackSize = stackA.getMaxStackSize();
                    int spaceLeftInA = maxStackSize - stackA.getCount();

                    // 如果 A 还有空间容纳物品
                    if (spaceLeftInA > 0) {
                        // 计算实际能从 B 挪多少个到 A 里
                        int toMove = Math.min(spaceLeftInA, stackB.getCount());

                        // 4. 调整两边的 ItemStack 数量
                        stackA.grow(toMove);
                        stackB.shrink(toMove);

                        // 5. 将更新后的 ItemStack 写回实体（这一步会触发客户端同步，物品数量才会变）
                        entityA.setItem(stackA.copy());

                        if (stackB.isEmpty()) {
                            // 如果 B 被扣空了，直接将 B 实体从世界中彻底销毁
                            entityB.discard();
                        } else {
                            // 如果 B 还没被扣空（比如 A 满了，B 还剩一点），更新 B 的数量
                            entityB.setItem(stackB.copy());
                        }

                        // 如果 A 已经塞满了，不需要再继续找后续的 B 了，直接结束内层循环
                        if (stackA.getCount() >= maxStackSize) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
