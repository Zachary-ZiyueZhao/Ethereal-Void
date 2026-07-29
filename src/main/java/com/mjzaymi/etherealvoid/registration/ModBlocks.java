package com.mjzaymi.etherealvoid.registration;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.block.*;
import com.mjzaymi.etherealvoid.block.electricity.FluidPump;
import com.mjzaymi.etherealvoid.block.electricity.HydraulicGenerator;
import com.mjzaymi.etherealvoid.block.electricity.ThreeWireCable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, EtherealVoid.MOD_ID);

    public static final RegistryObject<Block> ANTI_CORROSION_GLASS = BLOCKS.register("anti_corrosion_glass", AntiCorrosionGlass::new);
    public static final RegistryObject<Block> ELECTRODE_PLATE = BLOCKS.register("electrode_plate", ElectrodePlate::new);
    public static final RegistryObject<Block> STEEL_CASING = BLOCKS.register("steel_casing", SteelCasing::new);
    public static final RegistryObject<Block> RESISTIVE_HEATER = BLOCKS.register("resistive_heater", ResistiveHeater::new);
    public static final RegistryObject<Block> FLUID_PIPE = BLOCKS.register("fluid_pipe", FluidPipe::new);
    public static final RegistryObject<Block> HYDRAULIC_GENERATOR = BLOCKS.register("hydraulic_generator", HydraulicGenerator::new);
    public static final RegistryObject<Block> FLUID_PUMP = BLOCKS.register("fluid_pump", FluidPump::new);
    public static final RegistryObject<Block> THREE_WIRE_CABLE = BLOCKS.register("three_wire_cable", ThreeWireCable::new);


    public static final RegistryObject<Block> MAGNETIC_SIEVE = BLOCKS.register("magnetic_sieve", MagneticSieve::new);

    public static final RegistryObject<Block> GEM_POLISHING_STATION = BLOCKS.register("gem_polishing_station",
            () -> new GemPolishingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> POOL_MONITOR = BLOCKS.register("pool_monitor", PoolMonitor::new);
    public static final RegistryObject<Block> REACTION_POOL_FLUID_IO = BLOCKS.register("reaction_pool_fluid_io", ReactionPoolFluidIO::new);

    // 仆从方块：设置为空气般不可见，且不可导电/透光（noOcclusion）
    public static final RegistryObject<Block> VIRTUAL_MINER_PART = BLOCKS.register("virtual_miner_part",
            () -> new VirtualMinerPartBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER).noOcclusion().noLootTable()));

    public static final RegistryObject<Block> VIRTUAL_MINER = BLOCKS.register("virtual_miner", VirtualMinerBlock::new);


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
