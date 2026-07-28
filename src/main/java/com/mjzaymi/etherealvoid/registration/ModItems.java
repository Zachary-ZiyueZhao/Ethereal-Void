package com.mjzaymi.etherealvoid.registration;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.blockentity.VirtualMinerItem;
import com.mjzaymi.etherealvoid.common.item.BaseItem;
import com.mjzaymi.etherealvoid.item.FluidPipeFilterItem;
import com.mjzaymi.etherealvoid.item.Multimeter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.mjzaymi.etherealvoid.item.FluidStorageTankItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EtherealVoid.MOD_ID);

    //Block Items
    public static final RegistryObject<BlockItem> ANTI_CORROSION_GLASS_ITEM = registerBlockItem("anti_corrosion_glass", ModBlocks.ANTI_CORROSION_GLASS);
    public static final RegistryObject<BlockItem> ELECTRODE_PLATE_ITEM = registerBlockItem("electrode_plate", ModBlocks.ELECTRODE_PLATE);
    public static final RegistryObject<BlockItem> STEEL_CASING_ITEM = registerBlockItem("steel_casing", ModBlocks.STEEL_CASING);
    public static final RegistryObject<BlockItem> RESISTIVE_HEATER_ITEM = registerBlockItem("resistive_heater", ModBlocks.RESISTIVE_HEATER);
    public static final RegistryObject<BlockItem> FLUID_PIPE_ITEM = registerBlockItem("fluid_pipe", ModBlocks.FLUID_PIPE);
    public static final RegistryObject<BlockItem> HYDRAULIC_GENERATOR_ITEM = registerBlockItem("hydraulic_generator", ModBlocks.HYDRAULIC_GENERATOR);
    public static final RegistryObject<BlockItem> FLUID_PUMP_ITEM = registerBlockItem("fluid_pump", ModBlocks.FLUID_PUMP);
    public static final RegistryObject<BlockItem> REACTION_POOL_FLUID_IO_ITEM = registerBlockItem("reaction_pool_fluid_io", ModBlocks.REACTION_POOL_FLUID_IO);
    public static final RegistryObject<Item> VIRTUAL_MINER = ITEMS.register("virtual_miner", () -> new VirtualMinerItem(ModBlocks.VIRTUAL_MINER.get(), new Item.Properties()));


    public static final RegistryObject<BlockItem> MAGNETIC_SIEVE_ITEM = registerBlockItem("magnetic_sieve", ModBlocks.MAGNETIC_SIEVE);
    public static final RegistryObject<BlockItem> GEM_POLISHING_STATION_ITEM = registerBlockItem("gem_polishing_station", ModBlocks.GEM_POLISHING_STATION);
    public static final RegistryObject<BlockItem> POOL_MONITOR_ITEM = registerBlockItem("pool_monitor", ModBlocks.POOL_MONITOR);
    //Items
    public static final RegistryObject<Item> CRUSHED_IRON_ORE = ITEMS.register("crushed_iron_ore", BaseItem::new);
    public static final RegistryObject<Item> COAL_POWDER = ITEMS.register("coal_powder", BaseItem::new);
    public static final RegistryObject<Item> RAW_STEEL_POWDER = ITEMS.register("raw_steel_powder", BaseItem::new);
    public static final RegistryObject<Item> STEEL_INGOT = ITEMS.register("steel_ingot", BaseItem::new);
    public static final RegistryObject<Item> BAUXITE = ITEMS.register("bauxite", BaseItem::new);
    public static final RegistryObject<Item> ALUMINIUM_OXIDE = ITEMS.register("aluminium_oxide", BaseItem::new);
    public static final RegistryObject<Item> CRYOLITE = ITEMS.register("cryolite", BaseItem::new);
    public static final RegistryObject<Item> ALUMINIUM_INGOT = ITEMS.register("aluminium_ingot", BaseItem::new);
    public static final RegistryObject<Item> CAUSTIC_SODA = ITEMS.register("caustic_soda", BaseItem::new);
    public static final RegistryObject<Item> IMPURE_MAGNESIUM_SALT = ITEMS.register("impure_magnesium_salt", BaseItem::new);
    public static final RegistryObject<Item> MATTE = ITEMS.register("matte", BaseItem::new);
    public static final RegistryObject<Item> PURIFIED_COPPER_ORE = ITEMS.register("purified_copper_ore", BaseItem::new);
    public static final RegistryObject<Item> ZINC_CONCENTRATE = ITEMS.register("zinc_concentrate", BaseItem::new);
    public static final RegistryObject<Item> ZINC_OXIDE = ITEMS.register("zinc_oxide", BaseItem::new);
    public static final RegistryObject<Item> ZINC_INGOT = ITEMS.register("zinc_ingot", BaseItem::new);
    public static final RegistryObject<Item> DOLOMITE = ITEMS.register("dolomite", BaseItem::new);
    // public static final RegistryObject<Item> CALCINED_DOLOMITE = ITEMS.register("calcined_dolomite", BaseItem::new);
    public static final RegistryObject<Item> RAW_MAGNESIUM = ITEMS.register("raw_magnesium", BaseItem::new);
    public static final RegistryObject<Item> MAGNESIUM_INGOT = ITEMS.register("magnesium_ingot", BaseItem::new);
    public static final RegistryObject<Item> MULTIMETER = ITEMS.register("multimeter", Multimeter::new);
    public static final RegistryObject<Item> FLUID_STORAGE_TANK = ITEMS.register("fluid_storage_tank", () -> new FluidStorageTankItem(new Item.Properties()));
    public static final RegistryObject<Item> FLUID_PIPE_FILTER = ITEMS.register("fluid_pipe_filter", () -> new FluidPipeFilterItem(new Item.Properties()));




    private static <T extends Block> RegistryObject<BlockItem> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
