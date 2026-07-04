package com.mjzaymi.etherealvoid.blockentity;

import com.mjzaymi.etherealvoid.client.renderer.PoolEffectHandler;
import com.mjzaymi.etherealvoid.common.blockentity.UpdateBaseBlockEntity;
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

    // ==========================================
    // 🧱 字段与内部数据结构 (Fields & Data)
    // ==========================================

    private CuboidStructure structure;

    // Idle
    private final List<ItemStack> precipitates = new ArrayList<>();
    private final MultiFluidTank tank = new MultiFluidTank(0);

    // All
    private final List<ItemStack> precipitatesAll = new ArrayList<>();
    private final MultiFluidTank tankAll = new MultiFluidTank(0);
    private final List<ReactionRecipe> activeTasks = new ArrayList<>();

    // 物理参数
    private float temperature = 273.15f + 20f;
    private float pressure = 1;

    // ==========================================
    // 🏗️ 构造器与基础生命周期 (Constructor & LifeCycle)
    // ==========================================

    public ReactionPoolBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTION_POOL_BE.get(), pos, state);
    }

    @Override
    public void updateChangeState(Level level, boolean update) {
        super.updateChangeState(level, update);
        updateContentsAll();
    }

    // ==========================================
    // 💾 NBT 数据存储与读取 (NBT Serialization)
    // ==========================================

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        if (structure != null) pTag.put("structure", structure.serializeNBT());
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

    // ==========================================
    // 📐 多方块结构构建与销毁 (Structure Management)
    // ==========================================

    public void setStructure(CuboidStructure structure) {
        // 当结构被破坏时（从非 null 变成 null 的瞬间）
        if (structure == null && this.structure != null) {
            // 1. 通过 EffectHandler 分离处理粒子与声音
            PoolEffectHandler.spawnEvaporationParticles(level, this.structure, tank);

            if (this.temperature >= 273.15f + 100f) {
                PoolEffectHandler.spawnCoolingParticle(level, this.structure, this.temperature);
            }

            // 2. 🔥 【高效新增】: 遍历已缓存的结构成员，一键重置电极片
            // 仅在此瞬间执行一次，不涉及任何多余的拓扑搜索，性能极高！
            for (BlockPos pos : this.structure.members()) {
                BlockState state = level.getBlockState(pos);
                // 检查方块是否为电极片
                if (state.is(com.mjzaymi.etherealvoid.registration.ModBlocks.ELECTRODE_PLATE.get())) {
                    // 只有当它在工作（非 UNASSIGNED）时，才触发状态切换与粒子爆发
                    if (state.getValue(com.mjzaymi.etherealvoid.block.ElectrodePlate.MODE) != com.mjzaymi.etherealvoid.block.ElectrodePlate.ElectrodeMode.UNASSIGNED) {
                        com.mjzaymi.etherealvoid.block.ElectrodePlate.changeMode(level, pos, state, com.mjzaymi.etherealvoid.block.ElectrodePlate.ElectrodeMode.UNASSIGNED);
                    }
                }
            }

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

    // ==========================================
    // ⚙️ 核心 Tick 逻辑处理 (Core Game Loop)
    // ==========================================

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pLevel.isClientSide) return;

        var structure = getStructure();
        if (structure == null) return;

        // 验证多方块结构的完整性
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

        // 表现逻辑移交给 Handler 处理
        PoolEffectHandler.tickAmbientEffects(pLevel, structure, this.temperature);

        // 处理物理和化学反应
        updateTemperature(pLevel);
        processReactions(pLevel);
    }

    // ==========================================
    // 🌡️ 物理动力学: 温度调节 (Thermodynamics)
    // ==========================================

    public static final int UPDATE_TEMPERATURE_TICK = 20;
    public final int RANDOM_UPDATE_TEMPERATURE_REMAINDER = new Random().nextInt(UPDATE_TEMPERATURE_TICK);

    public void updateTemperature(Level pLevel) {
        if (pLevel.getGameTime() % UPDATE_TEMPERATURE_TICK != RANDOM_UPDATE_TEMPERATURE_REMAINDER) return;

        int heaterCount = structure.countHeatersBelow(pLevel);
        float targetTemperature = 293.15f + (heaterCount * 100.0f);

        var originalTemp = this.temperature;
        if (this.temperature < targetTemperature) {
            // 使用对数函数动态计算升温速率
            double logBonus = Math.log(heaterCount + 1.0);
            float heatingRate = (float) (10.0 * logBonus);

            // 确保哪怕只有 1 个加热器，最少也能升温 1°C
            if (heaterCount > 0 && heatingRate < 1.0f) {
                heatingRate = 1.0f;
            }

            this.temperature = Math.min(targetTemperature, this.temperature + heatingRate);

        } else if (this.temperature > targetTemperature) {
            float tempDifference = this.temperature - targetTemperature;

            // 基于温差的对数降温
            double logCooling = Math.log(tempDifference + 1.0);
            float coolingRate = (float) (2.5 * logCooling);

            // 保底机制
            if (coolingRate < 0.5f) {
                coolingRate = 0.5f;
            }

            this.temperature = Math.max(targetTemperature, this.temperature - coolingRate);
        }

        if (originalTemp != this.temperature)
            updateChangeState(true);
    }

    // ==========================================
    // 🧪 反应池机制: 配方与工艺流程 (Chemical Reactions)
    // ==========================================

    public void registerReaction(ReactionRecipe recipe) {
        recipe.cost(precipitates, tank);
        activeTasks.add(recipe.copyNew());
    }

    public static final int PROCESS_TICK = 20;
    public final int RANDOM_PROCESS_REMAINDER = new Random().nextInt(PROCESS_TICK);

    public void processReactions(Level pLevel) {
        if (pLevel.getGameTime() % PROCESS_TICK != RANDOM_PROCESS_REMAINDER) return;

        boolean changed = false;
        FOR:
        for (var recipe : ModReactionRecipes.registeredRecipes) {
            if (!recipe.costsEnough(precipitates, tank.getFluids())) continue;
            if (!recipe.matchCondition(this)) continue;
            boolean emptyTasks = activeTasks.isEmpty();
            boolean containsType = GameUtil.findById(activeTasks, recipe.id) != null;
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

    // ==========================================
    // 📊 内部缓存同步与信息流转 (Data Sync & Getters)
    // ==========================================

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

    @Override
    public AABB getRenderBoundingBox() {
        if (structure == null) return super.getRenderBoundingBox();
        var min = getStructure().min();
        var max = getStructure().max();
        return new AABB(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0
        );
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