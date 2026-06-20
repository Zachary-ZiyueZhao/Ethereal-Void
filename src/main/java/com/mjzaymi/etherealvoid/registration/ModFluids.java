package com.mjzaymi.etherealvoid.registration;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, EtherealVoid.MOD_ID);

    public static final RegistryObject<FlowingFluid> SOURCE_SOAP_WATER = FLUIDS.register("soap_water_fluid",
            () -> new ForgeFlowingFluid.Source(ModFluids.SOAP_WATER_FLUID_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_SOAP_WATER = FLUIDS.register("flowing_soap_water",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.SOAP_WATER_FLUID_PROPERTIES));
    public static final ForgeFlowingFluid.Properties SOAP_WATER_FLUID_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.SOAP_WATER_FLUID_TYPE, SOURCE_SOAP_WATER, FLOWING_SOAP_WATER)
            .slopeFindDistance(2).levelDecreasePerBlock(2);//.block(ModBlocks.SOAP_WATER_BLOCK);
            //.bucket(ModItems.SOAP_WATER_BUCKET);

    public static final RegistryObject<FlowingFluid> SOURCE_MOLTEN_ALUMINIUM = FLUIDS.register("molten_aluminium",
            () -> new ForgeFlowingFluid.Source(ModFluids.MOLTEN_ALUMINIUM_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_MOLTEN_ALUMINIUM = FLUIDS.register("flowing_molten_aluminium",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.MOLTEN_ALUMINIUM_PROPERTIES));
    public static final ForgeFlowingFluid.Properties MOLTEN_ALUMINIUM_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.MOLTEN_ALUMINIUM_TYPE, SOURCE_MOLTEN_ALUMINIUM, FLOWING_MOLTEN_ALUMINIUM)
            .slopeFindDistance(1).levelDecreasePerBlock(2);

    // 熔融冰晶石
    public static final RegistryObject<FlowingFluid> SOURCE_MOLTEN_CRYOLITE = FLUIDS.register("molten_cryolite",
            () -> new ForgeFlowingFluid.Source(ModFluids.MOLTEN_CRYOLITE_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_MOLTEN_CRYOLITE = FLUIDS.register("flowing_molten_cryolite",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.MOLTEN_CRYOLITE_PROPERTIES));
    public static final ForgeFlowingFluid.Properties MOLTEN_CRYOLITE_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.MOLTEN_CRYOLITE_TYPE, SOURCE_MOLTEN_CRYOLITE, FLOWING_MOLTEN_CRYOLITE)
            .slopeFindDistance(1).levelDecreasePerBlock(2);

    // 硫酸
    public static final RegistryObject<FlowingFluid> SOURCE_SULFURIC_ACID = FLUIDS.register("sulfuric_acid",
            () -> new ForgeFlowingFluid.Source(ModFluids.SULFURIC_ACID_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_SULFURIC_ACID = FLUIDS.register("flowing_sulfuric_acid",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.SULFURIC_ACID_PROPERTIES));
    public static final ForgeFlowingFluid.Properties SULFURIC_ACID_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.SULFURIC_ACID_TYPE, SOURCE_SULFURIC_ACID, FLOWING_SULFURIC_ACID)
            .slopeFindDistance(2).levelDecreasePerBlock(1);

    // 二氧化硫
    public static final RegistryObject<FlowingFluid> SOURCE_SULFUR_DIOXIDE = FLUIDS.register("sulfur_dioxide",
            () -> new ForgeFlowingFluid.Source(ModFluids.SULFUR_DIOXIDE_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_SULFUR_DIOXIDE = FLUIDS.register("flowing_sulfur_dioxide",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.SULFUR_DIOXIDE_PROPERTIES));
    public static final ForgeFlowingFluid.Properties SULFUR_DIOXIDE_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.SULFUR_DIOXIDE_TYPE, SOURCE_SULFUR_DIOXIDE, FLOWING_SULFUR_DIOXIDE)
            .slopeFindDistance(4).levelDecreasePerBlock(1);

    // 硫酸锌溶液
    public static final RegistryObject<FlowingFluid> SOURCE_ZINC_SULFATE_SOLUTION = FLUIDS.register("zinc_sulfate_solution",
            () -> new ForgeFlowingFluid.Source(ModFluids.ZINC_SULFATE_SOLUTION_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_ZINC_SULFATE_SOLUTION = FLUIDS.register("flowing_zinc_sulfate_solution",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.ZINC_SULFATE_SOLUTION_PROPERTIES));
    public static final ForgeFlowingFluid.Properties ZINC_SULFATE_SOLUTION_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.ZINC_SULFATE_SOLUTION_TYPE, SOURCE_ZINC_SULFATE_SOLUTION, FLOWING_ZINC_SULFATE_SOLUTION)
            .slopeFindDistance(2).levelDecreasePerBlock(1);

    // 硫酸铜溶液
    public static final RegistryObject<FlowingFluid> SOURCE_COPPER_SULFATE_SOLUTION = FLUIDS.register("copper_sulfate_solution",
            () -> new ForgeFlowingFluid.Source(ModFluids.COPPER_SULFATE_SOLUTION_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_COPPER_SULFATE_SOLUTION = FLUIDS.register("flowing_copper_sulfate_solution",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.COPPER_SULFATE_SOLUTION_PROPERTIES));
    public static final ForgeFlowingFluid.Properties COPPER_SULFATE_SOLUTION_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.COPPER_SULFATE_SOLUTION_TYPE, SOURCE_COPPER_SULFATE_SOLUTION, FLOWING_COPPER_SULFATE_SOLUTION)
            .slopeFindDistance(2).levelDecreasePerBlock(1);

    // 烧碱溶液
    public static final RegistryObject<FlowingFluid> SOURCE_SODIUM_HYDROXIDE_SOLUTION = FLUIDS.register("sodium_hydroxide_solution",
            () -> new ForgeFlowingFluid.Source(ModFluids.SODIUM_HYDROXIDE_SOLUTION_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_SODIUM_HYDROXIDE_SOLUTION = FLUIDS.register("flowing_sodium_hydroxide_solution",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.SODIUM_HYDROXIDE_SOLUTION_PROPERTIES));
    public static final ForgeFlowingFluid.Properties SODIUM_HYDROXIDE_SOLUTION_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.SODIUM_HYDROXIDE_SOLUTION_TYPE, SOURCE_SODIUM_HYDROXIDE_SOLUTION, FLOWING_SODIUM_HYDROXIDE_SOLUTION)
            .slopeFindDistance(2).levelDecreasePerBlock(1);

    // 氢气
    public static final RegistryObject<FlowingFluid> SOURCE_HYDROGEN = FLUIDS.register("hydrogen",
            () -> new ForgeFlowingFluid.Source(ModFluids.HYDROGEN_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_HYDROGEN = FLUIDS.register("flowing_hydrogen",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.HYDROGEN_PROPERTIES));
    public static final ForgeFlowingFluid.Properties HYDROGEN_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.HYDROGEN_TYPE, SOURCE_HYDROGEN, FLOWING_HYDROGEN)
            .slopeFindDistance(8).levelDecreasePerBlock(1);

    // 氯气
    public static final RegistryObject<FlowingFluid> SOURCE_CHLORINE = FLUIDS.register("chlorine",
            () -> new ForgeFlowingFluid.Source(ModFluids.CHLORINE_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_CHLORINE = FLUIDS.register("flowing_chlorine",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.CHLORINE_PROPERTIES));
    public static final ForgeFlowingFluid.Properties CHLORINE_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.CHLORINE_TYPE, SOURCE_CHLORINE, FLOWING_CHLORINE)
            .slopeFindDistance(8).levelDecreasePerBlock(1);

    // 液氢
    public static final RegistryObject<FlowingFluid> SOURCE_LIQUID_HYDROGEN = FLUIDS.register("liquid_hydrogen",
            () -> new ForgeFlowingFluid.Source(ModFluids.LIQUID_HYDROGEN_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_LIQUID_HYDROGEN = FLUIDS.register("flowing_liquid_hydrogen",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.LIQUID_HYDROGEN_PROPERTIES));
    public static final ForgeFlowingFluid.Properties LIQUID_HYDROGEN_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.LIQUID_HYDROGEN_TYPE, SOURCE_LIQUID_HYDROGEN, FLOWING_LIQUID_HYDROGEN)
            .slopeFindDistance(3).levelDecreasePerBlock(1);

    // 熔融冰晶石-氧化铝电解质
    public static final RegistryObject<FlowingFluid> SOURCE_MOLTEN_ELECTROLYTE = FLUIDS.register("molten_electrolyte",
            () -> new ForgeFlowingFluid.Source(ModFluids.MOLTEN_ELECTROLYTE_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_MOLTEN_ELECTROLYTE = FLUIDS.register("flowing_molten_electrolyte",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.MOLTEN_ELECTROLYTE_PROPERTIES));
    public static final ForgeFlowingFluid.Properties MOLTEN_ELECTROLYTE_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.MOLTEN_ELECTROLYTE_TYPE, SOURCE_MOLTEN_ELECTROLYTE, FLOWING_MOLTEN_ELECTROLYTE)
            .slopeFindDistance(1).levelDecreasePerBlock(2);

    // 熔融锌
    public static final RegistryObject<FlowingFluid> SOURCE_MOLTEN_ZINC = FLUIDS.register("molten_zinc",
            () -> new ForgeFlowingFluid.Source(ModFluids.MOLTEN_ZINC_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_MOLTEN_ZINC = FLUIDS.register("flowing_molten_zinc",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.MOLTEN_ZINC_PROPERTIES));
    public static final ForgeFlowingFluid.Properties MOLTEN_ZINC_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.MOLTEN_ZINC_TYPE, SOURCE_MOLTEN_ZINC, FLOWING_MOLTEN_ZINC)
            .slopeFindDistance(1).levelDecreasePerBlock(2);

    // 熔融镁
    public static final RegistryObject<FlowingFluid> SOURCE_MOLTEN_MAGNESIUM = FLUIDS.register("molten_magnesium",
            () -> new ForgeFlowingFluid.Source(ModFluids.MOLTEN_MAGNESIUM_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_MOLTEN_MAGNESIUM = FLUIDS.register("flowing_molten_magnesium",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.MOLTEN_MAGNESIUM_PROPERTIES));
    public static final ForgeFlowingFluid.Properties MOLTEN_MAGNESIUM_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.MOLTEN_MAGNESIUM_TYPE, SOURCE_MOLTEN_MAGNESIUM, FLOWING_MOLTEN_MAGNESIUM)
            .slopeFindDistance(1).levelDecreasePerBlock(2);

    // 熔融铜
    public static final RegistryObject<FlowingFluid> SOURCE_MOLTEN_COPPER = FLUIDS.register("molten_copper",
            () -> new ForgeFlowingFluid.Source(ModFluids.MOLTEN_COPPER_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_MOLTEN_COPPER = FLUIDS.register("flowing_molten_copper",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.MOLTEN_COPPER_PROPERTIES));
    public static final ForgeFlowingFluid.Properties MOLTEN_COPPER_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.MOLTEN_COPPER_TYPE, SOURCE_MOLTEN_COPPER, FLOWING_MOLTEN_COPPER)
            .slopeFindDistance(1).levelDecreasePerBlock(2);

    // 熔融航空铝合金
    public static final RegistryObject<FlowingFluid> SOURCE_MOLTEN_AEROSPACE_ALLOY = FLUIDS.register("molten_aerospace_alloy",
            () -> new ForgeFlowingFluid.Source(ModFluids.MOLTEN_AEROSPACE_ALLOY_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_MOLTEN_AEROSPACE_ALLOY = FLUIDS.register("flowing_molten_aerospace_alloy",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.MOLTEN_AEROSPACE_ALLOY_PROPERTIES));
    public static final ForgeFlowingFluid.Properties MOLTEN_AEROSPACE_ALLOY_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.MOLTEN_AEROSPACE_ALLOY_TYPE, SOURCE_MOLTEN_AEROSPACE_ALLOY, FLOWING_MOLTEN_AEROSPACE_ALLOY)
            .slopeFindDistance(1).levelDecreasePerBlock(2);


    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}