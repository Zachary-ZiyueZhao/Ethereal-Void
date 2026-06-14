package com.mjzaymi.etherealvoid.registrations;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.block.*;
import net.minecraft.world.level.block.Block;
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


    public static final RegistryObject<Block> MAGNETIC_SIEVE = BLOCKS.register("magnetic_sieve", MagneticSieve::new);


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
