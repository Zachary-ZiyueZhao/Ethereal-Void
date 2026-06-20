package com.mjzaymi.etherealvoid.block.entity;

import com.mjzaymi.etherealvoid.common.block.entity.UpdateBaseBlockEntity;
import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.reactionpool.recipe.ReactionRecipe;
import com.mjzaymi.etherealvoid.reactionpool.recipe.SyncType;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import com.mjzaymi.etherealvoid.registration.ModReactionRecipes;
import com.mjzaymi.etherealvoid.common.util.GameUtil;
import com.mjzaymi.etherealvoid.common.util.fluid.MultiFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

public class ReactionPoolBlockEntity extends UpdateBaseBlockEntity {

    private CuboidStructure structure;

    //Idle
    private final List<ItemStack> precipitates = new ArrayList<>();
    private final MultiFluidTank tank = new MultiFluidTank(0);
    //All
    private final List<ItemStack> precipitatesAll = new ArrayList<>();
    private final MultiFluidTank tankAll = new MultiFluidTank(0);
    private final List<ReactionRecipe> activeTasks = new ArrayList<>();
    private float temperature = 273.15f+20f;
    private float pressure = 1;

    public ReactionPoolBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTION_POOL_BE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        if (structure!=null) pTag.put("structure", structure.serializeNBT());
        pTag.put("tank", tank.writeToNBT(new CompoundTag()));
        var list = new ListTag();
        synchronized (precipitates) {
            for (ItemStack itemStack : precipitates) list.add(itemStack.save(new CompoundTag()));
        }
        pTag.put("precipitates", list);
        list = new ListTag();
        synchronized (activeTasks) {
            for (ReactionRecipe task : activeTasks) list.add(task.save(new CompoundTag()));
        }
        pTag.put("activeTasks", list);
        pTag.putFloat("temperature", temperature);
        pTag.putFloat("pressure", pressure);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        structure = CuboidStructure.deserializeNBT(pTag.getCompound("structure"));
        tank.readFromNBT(pTag.getCompound("tank"));
        synchronized (precipitates) {
            precipitates.clear();
            for (Tag t : pTag.getList("precipitates", Tag.TAG_COMPOUND))
                precipitates.add(ItemStack.of((CompoundTag) t));
        }
        synchronized (activeTasks) {
            activeTasks.clear();
            for (Tag t : pTag.getList("activeTasks", Tag.TAG_COMPOUND))
                activeTasks.add(ReactionRecipe.of((CompoundTag) t));
        }
        temperature = pTag.getFloat("temperature");
        pressure = pTag.getFloat("pressure");
        updateContentsAll();
    }

    @Override
    public void updateChangeState(Level level, boolean update) {
        super.updateChangeState(level, update);
        updateContentsAll();
    }

    public void setStructure(CuboidStructure structure) {
        if (structure==null) {
            tank.setCapacity(0);
            if (this.structure!=null) {
                GameUtil.spawnItemRandomlyInArea(level,
                        this.structure.interiorFloorMin(), this.structure.interiorFloorMax(),
                        precipitates);
            }
            precipitates.clear();
            activeTasks.clear();
        } else {
            tank.setCapacity(structure.interiors().size() * 1000);
        }
        this.structure = structure;
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (structure==null) return super.getRenderBoundingBox();
        var min = getStructure().min();
        var max = getStructure().max();
        return new AABB(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0
        );
    }


    public CuboidStructure getStructure() {
        return structure;
    }

    public static final int PROCESS_TICK = 20;

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pLevel.isClientSide) return;

        var structure = getStructure();
        if (structure == null) return;

        var realStructureOpt = CuboidStructure.findFromCorner(pLevel, getBlockPos());
        if (realStructureOpt.isEmpty()) {
            setStructure(null);
            setChanged(pLevel, pPos, pState);
            updateChangeState(true);
            return;
        } else if (!structure.isEqual(realStructureOpt.get())){
            setStructure(realStructureOpt.get());
            setChanged(pLevel, pPos, pState);
            updateChangeState(true);
            return;
        }

        for (BlockPos p : structure.interiors()) {
            var s = pLevel.getBlockState(p);
            var fluid = s.getFluidState();
            if (!fluid.isEmpty() && fluid.isSource()) {
                tank.fill(
                        new FluidStack(fluid.getType(), 1000),
                        IFluidHandler.FluidAction.EXECUTE
                );
                pLevel.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                setChanged(pLevel, pPos, pState);
                pLevel.updateNeighborsAt(p, Blocks.AIR.defaultBlockState().getBlock());
                updateChangeState(true);
                continue;
            }

            if (!s.isAir()) {
                Block.dropResources(s, pLevel, p);
                pLevel.removeBlock(p, false);
            }
        }


        var min = structure.min();
        var max = structure.max();
        AABB area = new AABB(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D
        );
        GameUtil.mergeItemsInArea(pLevel, area);
        var entities = pLevel.getEntitiesOfClass(ItemEntity.class, area);
        if (!entities.isEmpty()) {
            synchronized (precipitates) {
                for (var entity : entities) {
                    if (!entity.isAlive() || entity.getItem().isEmpty()) continue;
                    GameUtil.addItemToList(precipitates, entity.getItem());
                    entity.discard();
                }
            }
            updateChangeState(true);
        }

        if (pLevel.getGameTime() % PROCESS_TICK != 0) return;
        processReaction();
    }

    public void registerReaction(ReactionRecipe recipe) {
        recipe.cost(precipitates, tank);
        activeTasks.add(recipe.copyNew());
    }

    public void processReaction() {
        boolean changed = false;
        //Check for recipes and register them.
        FOR:
        for (var recipe : ModReactionRecipes.registeredRecipes) {
            if (!recipe.costsEnough(precipitates, tank.getFluids())) continue;
            if (!recipe.matchCondition(this)) continue;
            boolean emptyTasks = activeTasks.isEmpty();
            boolean containsType = GameUtil.findById(activeTasks, recipe.id)!=null;
            switch (recipe.syncType) {
                case SyncType.SYNC -> {
                    if (!emptyTasks) continue;
                    registerReaction(recipe);
                    break FOR;
                }
                case SyncType.RECIPE_SYNC -> {
                    if (containsType) continue;
                    registerReaction(recipe);
                }
                case SyncType.RECIPE_ASYNC -> {
                    if (!(emptyTasks || containsType)) continue;
                    while (recipe.costsEnough(precipitates, tank.getFluids())) registerReaction(recipe);
                    break FOR;
                }
                case SyncType.ASYNC -> {
                    while (recipe.costsEnough(precipitates, tank.getFluids())) registerReaction(recipe);
                }
            }
        }
        synchronized (activeTasks) {
            synchronized (precipitates) {
                synchronized (tank) {
                    Iterator<ReactionRecipe> taskIterator = activeTasks.listIterator();
                    while (taskIterator.hasNext()) {
                        ReactionRecipe task = taskIterator.next();
                        if (task.tick(PROCESS_TICK)) {
                            task.result(precipitates, tank);
                            taskIterator.remove();
                            changed = true;
                        }
                        if (!task.matchCondition(this)) {
                            task.returnCost(precipitates, tank);
                            taskIterator.remove();
                            changed = true;
                        }
                    }
                }
            }
        }
        if (changed) updateChangeState(true);
    }

    public void updateContentsAll() {
        var pCopy = new ArrayList<>(precipitates);
        for (ReactionRecipe task : activeTasks) GameUtil.addItemsToList(pCopy, task.cost.ingredients);
        synchronized (precipitatesAll) {
            precipitatesAll.clear();
            precipitatesAll.addAll(pCopy);
        }
        synchronized (tankAll) {
            tankAll.copyFrom(tank);
            for (var task : activeTasks)
                for (Object o : task.cost.ingredients) {
                    if (!(o instanceof FluidStack fluidStack)) continue;
                    tankAll.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                }
        }
    }

    public List<ItemStack> getPrecipitatesAll() {
        return precipitatesAll;
    }
    public MultiFluidTank getTankAll() {
        return tankAll;
    }
    public float getTemperature() {
        return temperature;
    }
    public float getPressure() {
        return pressure;
    }

    public void addItem(ItemStack item) {
        GameUtil.addItemToList(precipitates, item);
    }
}