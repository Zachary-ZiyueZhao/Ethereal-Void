package com.mjzaymi.etherealvoid.registration;

import com.mjzaymi.etherealvoid.entity.SmallRocketEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "ethereal_void");

    // 注册我们的小火箭实体
    public static final RegistryObject<EntityType<SmallRocketEntity>> SMALL_ROCKET =
            ENTITY_TYPES.register("small_rocket", () ->
                    EntityType.Builder.<SmallRocketEntity>of(SmallRocketEntity::new, MobCategory.MISC)
                            .sized(1.5F, 4.0F) // 设定火箭的物理碰撞箱：宽 1.5 格，高 4.0 格
                            .build("small_rocket")
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}