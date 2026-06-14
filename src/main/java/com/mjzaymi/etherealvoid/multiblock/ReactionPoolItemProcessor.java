package com.mjzaymi.etherealvoid.multiblock;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReactionPoolItemProcessor {
    private static final String PROGRESS_TAG = "EtherealVoidReactionProgress";
    private static final List<ReactionPoolItemRecipe> RECIPES = List.of(
            new ReactionPoolItemRecipe(
                    "raw_iron_with_coal",
                    Ingredient.of(Items.RAW_IRON),
                    1,
                    Ingredient.of(Items.COAL),
                    1,
                    new ItemStack(Items.IRON_INGOT),
                    100
            )
    );

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide || event.level.getGameTime() % 20 != 0) {
            return;
        }

        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        Map<String, PoolContents> pools = new HashMap<>();
        for (Entity entity : serverLevel.getAllEntities()) {
            if (!(entity instanceof ItemEntity itemEntity) || !itemEntity.isAlive() || itemEntity.getItem().isEmpty()) {
                continue;
            }

            Optional<CuboidStructure> structure = CuboidStructure.findFromInterior(serverLevel, itemEntity.blockPosition());
            if (structure.isEmpty()) {
                itemEntity.setExtendedLifetime();
                itemEntity.setDefaultPickUpDelay();
                itemEntity.getPersistentData().remove(PROGRESS_TAG);
                continue;
            }

            itemEntity.setNeverPickUp();
            itemEntity.setUnlimitedLifetime();

            CuboidStructure pool = structure.get();
            pools.computeIfAbsent(key(pool), ignored -> new PoolContents(pool)).items.add(itemEntity);
        }

        for (PoolContents contents : pools.values()) {
            processReaction(serverLevel, contents);
        }
    }

    private static void processReaction(ServerLevel level, PoolContents contents) {
        for (ReactionPoolItemRecipe recipe : RECIPES) {
            ItemEntity first = findMatching(contents.items, recipe::matchesFirst);
            ItemEntity second = findMatching(contents.items, recipe::matchesSecond);

            if (first == null || second == null || first == second) {
                continue;
            }

            CompoundTag data = first.getPersistentData();
            int progress = data.getInt(PROGRESS_TAG) + 20;
            if (progress < recipe.cookTicks()) {
                data.putInt(PROGRESS_TAG, progress);
                return;
            }

            craft(level, contents.structure, first, recipe.firstCount(), second, recipe.secondCount(), recipe.result());
            return;
        }
    }

    private static ItemEntity findMatching(List<ItemEntity> items, StackPredicate predicate) {
        for (ItemEntity item : items) {
            if (predicate.test(item.getItem())) {
                return item;
            }
        }
        return null;
    }

    private static void craft(ServerLevel level, CuboidStructure structure, ItemEntity first, int firstCount, ItemEntity second, int secondCount, ItemStack result) {
        first.getItem().shrink(firstCount);
        second.getItem().shrink(secondCount);

        if (first.getItem().isEmpty()) {
            first.discard();
        }
        if (second.getItem().isEmpty()) {
            second.discard();
        }

        BlockPos min = structure.min();
        BlockPos max = structure.max();
        double x = (min.getX() + max.getX() + 1) / 2.0;
        double y = min.getY() + 1.15;
        double z = (min.getZ() + max.getZ() + 1) / 2.0;
        ItemEntity output = new ItemEntity(level, x, y, z, result.copy());
        output.setNeverPickUp();
        output.setUnlimitedLifetime();
        output.setDeltaMovement(0.0, 0.0, 0.0);
        level.addFreshEntity(output);
    }

    private static String key(CuboidStructure structure) {
        BlockPos min = structure.min();
        BlockPos max = structure.max();
        return min.getX() + "," + min.getY() + "," + min.getZ() + ":" + max.getX() + "," + max.getY() + "," + max.getZ();
    }

    private record PoolContents(CuboidStructure structure, List<ItemEntity> items) {
        private PoolContents(CuboidStructure structure) {
            this(structure, new ArrayList<>());
        }
    }

    private interface StackPredicate {
        boolean test(ItemStack stack);
    }
}
