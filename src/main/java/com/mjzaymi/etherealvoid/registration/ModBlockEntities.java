package com.mjzaymi.etherealvoid.registration;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.blockentity.*;
import com.mjzaymi.etherealvoid.blockentity.electricity.FluidPumpBlockEntity;
import com.mjzaymi.etherealvoid.blockentity.electricity.HydraulicGeneratorBlockEntity;
import com.mjzaymi.etherealvoid.blockentity.electricity.ThreeWireCableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, EtherealVoid.MOD_ID);

    public static final RegistryObject<BlockEntityType<GemPolishingStationBlockEntity>> GEM_POLISHING_BE =
            BLOCK_ENTITIES.register("gem_polishing_be", () ->
                    BlockEntityType.Builder.of(GemPolishingStationBlockEntity::new,
                            ModBlocks.GEM_POLISHING_STATION.get()).build(null));

    public static final RegistryObject<BlockEntityType<PoolMonitorBlockEntity>> POOL_MONITOR_BE =
            BLOCK_ENTITIES.register("pool_monitor_be", () ->
                    BlockEntityType.Builder.of(PoolMonitorBlockEntity::new,
                            ModBlocks.POOL_MONITOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<ReactionPoolBlockEntity>> REACTION_POOL_BE =
            BLOCK_ENTITIES.register("reaction_pool_be", () ->
                    BlockEntityType.Builder.of(ReactionPoolBlockEntity::new,
                            ModBlocks.STEEL_CASING.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE_BE =
            BLOCK_ENTITIES.register("fluid_pipe_be", () ->
                    BlockEntityType.Builder.of(FluidPipeBlockEntity::new,
                            ModBlocks.FLUID_PIPE.get()).build(null));

    public static final RegistryObject<BlockEntityType<HydraulicGeneratorBlockEntity>> HYDRAULIC_GENERATOR_BE =
            BLOCK_ENTITIES.register("hydraulic_generator_be", () ->
                    BlockEntityType.Builder.of(HydraulicGeneratorBlockEntity::new,
                            ModBlocks.HYDRAULIC_GENERATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidPumpBlockEntity>> FLUID_PUMP_BE =
            BLOCK_ENTITIES.register("fluid_pump_be", () ->
                    BlockEntityType.Builder.of(FluidPumpBlockEntity::new,
                            ModBlocks.FLUID_PUMP.get()).build(null));

    public static final RegistryObject<BlockEntityType<ThreeWireCableBlockEntity>> THREE_WIRE_CABLE_BE =
            BLOCK_ENTITIES.register("three_wire_cable_be", () ->
                    BlockEntityType.Builder.of(ThreeWireCableBlockEntity::new,
                            ModBlocks.THREE_WIRE_CABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ReactionPoolFluidIOBlockEntity>> REACTION_POOL_FLUID_IO_BE =
            BLOCK_ENTITIES.register("reaction_pool_fluid_io_be", () ->
                    BlockEntityType.Builder.of(ReactionPoolFluidIOBlockEntity::new,
                            ModBlocks.REACTION_POOL_FLUID_IO.get()).build(null));

    public static final RegistryObject<BlockEntityType<VirtualMinerBlockEntity>> VIRTUAL_MINER =
            BLOCK_ENTITIES.register("virtual_miner", () ->
                    BlockEntityType.Builder.of(VirtualMinerBlockEntity::new, ModBlocks.VIRTUAL_MINER.get()).build(null));



    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}