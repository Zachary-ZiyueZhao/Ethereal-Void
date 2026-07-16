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

    public static final RegistryObject<EntityType<SmallRocketEntity>> SMALL_ROCKET = ENTITY_TYPES.register("small_rocket",
            () -> EntityType.Builder.<SmallRocketEntity>of(SmallRocketEntity::new, MobCategory.MISC)
                    .sized(1.2F, 3.5F) // 火箭的碰撞箱大小
                    .clientTrackingRange(64) // 【重要】增大追踪范围，防止火箭飞太高后由于客户端超出视野范围直接被卸载看不到
                    .updateInterval(1)      // 【重要】设置为每 tick (1帧) 同步一次位置，让升天动画丝般顺滑
                    .build("small_rocket")
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}