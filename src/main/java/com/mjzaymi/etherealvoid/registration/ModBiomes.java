package com.mjzaymi.etherealvoid.registration;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class ModBiomes {
    public static final ResourceKey<Biome> EARTH_ORBIT = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "earth_orbit")
    );

    public static void register() {
        EtherealVoid.LOGGER.info("Ethereal Void 生物群系 Key 初始化成功！");
    }
}