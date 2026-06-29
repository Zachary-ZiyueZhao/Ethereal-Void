package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.common.blockentity.UpdateBaseBlockEntity;
import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.reactionpool.recipe.ReactionRecipe;
import com.mjzaymi.etherealvoid.reactionpool.recipe.SyncType;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import com.mjzaymi.etherealvoid.registration.ModReactionRecipes;
import com.mjzaymi.etherealvoid.common.util.GameUtil;
import com.mjzaymi.etherealvoid.common.util.fluid.MultiFluidTank;
import com.mjzaymi.etherealvoid.common.util.fluid.FluidSorter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraft.resources.ResourceLocation;

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
        // 当原先有结构，而传入新结构为 null 时，说明多方块外壳被砸碎破坏了
        if (structure == null && this.structure != null) {
            spawnEvaporationParticles();
            this.temperature = 293.15f;
            this.pressure = 1.0f;
        }
        if (structure == null) {
            tank.setCapacity(0);
            if (this.structure != null) {
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


    public CuboidStructure getStructure() {
        return structure;
    }

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

        // 吸纳多方块内部的流体和方块源
        for (BlockPos p : structure.interiors()) {
            var s = pLevel.getBlockState(p);
            var fluid = s.getFluidState();
            if (!fluid.isEmpty() && fluid.isSource()) {
                tank.fill(new FluidStack(fluid.getType(), 1000), IFluidHandler.FluidAction.EXECUTE);
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

        // 物品吞噬合并
        var min = structure.min();
        var max = structure.max();
        AABB area = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D);
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

        // 伤害危害层
        BlockPos intMin = structure.interiorMin();
        BlockPos intMax = structure.interiorMax();
        AABB interiorArea = new AABB(intMin.getX(), intMin.getY(), intMin.getZ(), intMax.getX() + 1.0D, intMax.getY() + 1.0D, intMax.getZ() + 1.0D);
        List<net.minecraft.world.entity.player.Player> players = pLevel.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, interiorArea);
        if (!players.isEmpty()) {
            boolean hasFluids = !tank.getFluids().isEmpty() && tank.getFluids().stream().anyMatch(fs -> fs.getAmount() > 0);
            if (hasFluids) {
                for (var player : players) {
                    if (player.isSpectator()) continue;
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 40, 0, true, false, false));
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN, 40, 2, true, false, false));
                    if (pLevel.getGameTime() % 10 == 0) {
                        player.hurt(pLevel.damageSources().magic(), 4.0F);
                    }
                }
            }
        }


        //一下是并列的进程，需要在方法第一行优化tick

        //Update temperature
        updateTemperature(pLevel);

        //Process Reactions
        processReactions(pLevel);
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

    public static final int UPDATE_TEMPERATURE_TICK = 20; //间隔tick
    public final int RANDOM_UPDATE_TEMPERATURE_REMAINDER = new Random().nextInt(UPDATE_TEMPERATURE_TICK); //每个方块随机选tick
    public void updateTemperature(Level pLevel) {
        if (pLevel.getGameTime() % UPDATE_TEMPERATURE_TICK != RANDOM_UPDATE_TEMPERATURE_REMAINDER) return;

        var heaterCount = structure.countHeatersBelow(pLevel);
        var targetTemperature = 293.15f + (heaterCount * 100.0f);

        var originalTemp = this.temperature;
        if (this.temperature < targetTemperature) {
            this.temperature = Math.min(targetTemperature, this.temperature + 10);
        } else if (this.temperature > targetTemperature) {
            this.temperature = Math.max(targetTemperature, this.temperature - 4);
        }
        if (originalTemp!=this.temperature)
            updateChangeState(true);
    }

    public void registerReaction(ReactionRecipe recipe) {
        recipe.cost(precipitates, tank);
        activeTasks.add(recipe.copyNew());
    }

    public static final int PROCESS_TICK = 20; //间隔tick
    public final int RANDOM_PROCESS_REMAINDER = new Random().nextInt(PROCESS_TICK); //每个方块随机选tick
    public void processReactions(Level pLevel) {
        if (pLevel.getGameTime() % PROCESS_TICK != RANDOM_PROCESS_REMAINDER) return;

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
                            continue;
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

    private void spawnEvaporationParticles() {
        if (level == null || level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (structure == null) return;

        List<FluidStack> fluids = tank.getFluids();
        if (fluids == null || fluids.isEmpty() || fluids.stream().allMatch(fs -> fs.getAmount() <= 0)) return;

        BlockPos min = structure.interiorMin();
        BlockPos max = structure.interiorMax();
        float capacity = tank.getCapacity();
        if (capacity <= 0) return;

        float totalHeight = max.getY() - min.getY() + 1.0f;
        float currentAirDensity = 0.0012f;
        float totalLiquidHeight = 0;
        float totalGasHeight = 0;

        for (FluidStack fluidStack : fluids) {
            float amount = fluidStack.getAmount();
            if (amount <= 0) continue;
            float fillPercentage = Math.min(1.0f, amount / capacity);
            float height = fillPercentage * totalHeight;

            net.minecraft.world.level.material.Fluid fluid = fluidStack.getFluid();
            float density = 1.0f; // 默认密度

            ResourceLocation rl = net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(fluid);
            if (rl != null) {
                String path = rl.getPath();
                if (path.endsWith("_flowing")) {
                    path = path.substring(0, path.length() - 8);
                }
                if (FluidSorter.DENSITY_MAP.containsKey(path)) {
                    density = FluidSorter.DENSITY_MAP.get(path);
                }
            }

            if (density < currentAirDensity) {
                totalGasHeight += height;
            } else {
                totalLiquidHeight += height;
            }
        }

        RandomSource random = level.random;
        int totalFluidAmount = fluids.stream().mapToInt(FluidStack::getAmount).sum();
        int baseCount = Math.max(200, Math.min(1000, totalFluidAmount / 50));

        // --- A. 液体爆炸渲染：集中在水槽底部的液体层区间 ---
        if (totalLiquidHeight > 0) {
            double liquidMinY = min.getY();
            double liquidMaxY = min.getY() + totalLiquidHeight;
            int liquidCount = Math.round(baseCount * (totalLiquidHeight / totalHeight));

            for (int i = 0; i < Math.max(20, liquidCount); i++) {
                double x = min.getX() + random.nextDouble() * (max.getX() - min.getX() + 1);
                double z = min.getZ() + random.nextDouble() * (max.getZ() - min.getZ() + 1);
                double y = liquidMinY + random.nextDouble() * (liquidMaxY - liquidMinY);

                if (random.nextFloat() < 0.7f) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0.08, 0.03, 0.08, 0.01);
                } else {
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0.06, 0.03, 0.06, 0.02);
                }
            }
        }

        // --- B. 气体膨胀渲染：集中在水槽顶部的倒挂气体层区间 ---
        if (totalGasHeight > 0) {
            double gasMaxY = max.getY() + 1.0;
            double gasMinY = gasMaxY - totalGasHeight;
            int gasCount = Math.round(baseCount * (totalGasHeight / totalHeight));

            for (int i = 0; i < Math.max(20, gasCount); i++) {
                double x = min.getX() + random.nextDouble() * (max.getX() - min.getX() + 1);
                double z = min.getZ() + random.nextDouble() * (max.getZ() - min.getZ() + 1);
                double y = gasMinY + random.nextDouble() * (gasMaxY - gasMinY);

                if (random.nextFloat() < 0.7f) {
                    serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 1, 0.1, 0.05, 0.1, 0.005);
                } else {
                    serverLevel.sendParticles(ParticleTypes.ASH, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
                }
            }
        }
    }

    public MultiFluidTank getTank() {
        return this.tank;
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