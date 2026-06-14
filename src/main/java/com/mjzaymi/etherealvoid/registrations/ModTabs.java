package com.mjzaymi.etherealvoid.registrations;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.utils.ReflectUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;
import java.util.List;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EtherealVoid.MOD_ID);


    public static final RegistryObject<CreativeModeTab> CUSTOM_TAB = CREATIVE_MODE_TABS.register("custom_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.MAGNETIC_SIEVE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                //TODO ORDER
                //BlockItems
                /*output.accept(ModItems.ANTI_CORROSION_GLASS_ITEM.get());
                output.accept(ModItems.ELECTRODE_PLATE_ITEM.get());
                output.accept(ModItems.STEEL_CASING_ITEM.get());
                output.accept(ModItems.RESISTIVE_HEATER_ITEM.get());
                output.accept(ModItems.MAGNETIC_SIEVE_ITEM.get());
                //Items
                output.accept(ModItems.CRUSHED_IRON_ORE.get());*/
                List<Field> fields = ReflectUtil.getStaticFinalFields(ModItems.class);
                for (Field f : fields) {
                    f.setAccessible(true);
                    try {
                        if (f.get(null) instanceof RegistryObject<?> item && item.get() instanceof Item itemGet) {
                            output.accept(itemGet);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
