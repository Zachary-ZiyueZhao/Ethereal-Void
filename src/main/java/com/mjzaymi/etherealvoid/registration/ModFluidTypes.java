package com.mjzaymi.etherealvoid.registration;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.fluid.BaseFluidType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ModFluidTypes {
    public static final ResourceLocation WATER_STILL_RL = ResourceLocation.parse("block/water_still");
    public static final ResourceLocation WATER_FLOWING_RL = ResourceLocation.parse("block/water_flow");
    public static final ResourceLocation SOAP_OVERLAY_RL = ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "misc/in_soap_water");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, EtherealVoid.MOD_ID);

    public static final RegistryObject<FluidType> SOAP_WATER_FLUID_TYPE = register("soap_water_fluid",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().lightLevel(2).density(15).viscosity(5).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> MOLTEN_ALUMINIUM_TYPE = register("molten_aluminium",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().lightLevel(2).density(15).viscosity(5).sound(SoundAction.get("drink"),
                            SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> MOLTEN_CRYOLITE_TYPE = register("molten_cryolite",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().lightLevel(2).density(20).viscosity(6).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> SULFURIC_ACID_TYPE = register("sulfuric_acid",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().density(18).viscosity(4).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> SULFUR_DIOXIDE_TYPE = register("sulfur_dioxide",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().density(2).viscosity(1).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> ZINC_SULFATE_SOLUTION_TYPE = register("zinc_sulfate_solution",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().density(12).viscosity(3).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> COPPER_SULFATE_SOLUTION_TYPE = register("copper_sulfate_solution",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().density(12).viscosity(3).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> SODIUM_HYDROXIDE_SOLUTION_TYPE = register("sodium_hydroxide_solution",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().density(11).viscosity(3).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> HYDROGEN_TYPE = register("hydrogen",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().density(1).viscosity(1).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> CHLORINE_TYPE = register("chlorine",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().density(3).viscosity(1).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> LIQUID_HYDROGEN_TYPE = register("liquid_hydrogen",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().lightLevel(1).density(2).viscosity(1).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> MOLTEN_ELECTROLYTE_TYPE = register("molten_electrolyte",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().lightLevel(3).density(18).viscosity(6).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> MOLTEN_ZINC_TYPE = register("molten_zinc",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().lightLevel(2).density(16).viscosity(5).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> MOLTEN_MAGNESIUM_TYPE = register("molten_magnesium",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().lightLevel(3).density(10).viscosity(4).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> MOLTEN_COPPER_TYPE = register("molten_copper",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().lightLevel(4).density(18).viscosity(6).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));

    public static final RegistryObject<FluidType> MOLTEN_AEROSPACE_ALLOY_TYPE = register("molten_aerospace_alloy",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, SOAP_OVERLAY_RL,
                    0xA1E038D0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f),
                    FluidType.Properties.create().lightLevel(3).density(17).viscosity(6).sound(
                            SoundAction.get("drink"), SoundEvents.HONEY_DRINK)));


    private static <I extends FluidType> RegistryObject<FluidType> register(final String name, final Supplier<? extends I> sup) {
        return FLUID_TYPES.register(name, sup);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}