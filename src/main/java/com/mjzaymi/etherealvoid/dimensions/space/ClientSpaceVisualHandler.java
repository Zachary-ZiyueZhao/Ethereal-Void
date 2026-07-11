package com.mjzaymi.etherealvoid.dimensions.space;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientSpaceVisualHandler {

    public static final ResourceKey<Level> EARTH_KEY = Level.OVERWORLD;
    public static final ResourceKey<Level> ORBIT_KEY = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "low_earth_orbit"));

    public static final double EARTH_START_Y = 280.0;
    public static final double EARTH_MAX_Y = 320.0;
    public static final double ORBIT_START_Y = -20.0;
    public static final double ORBIT_MAX_Y = -60.0;

    /**
     * 获取空间遮罩过渡系数 (0.0 -> 无遮罩，1.0 -> 纯色满上)
     */
    public static float getFogFactor(Player player) {
        if (player == null) return 0.0F;
        double y = player.getY();
        ResourceKey<Level> dim = player.level().dimension();

        if (dim.equals(EARTH_KEY) && y >= EARTH_START_Y) {
            return (float) Math.min(1.0, Math.max(0.0, (y - EARTH_START_Y) / (EARTH_MAX_Y - EARTH_START_Y)));
        } else if (dim.equals(ORBIT_KEY) && y <= ORBIT_START_Y) {
            return (float) Math.min(1.0, Math.max(0.0, (ORBIT_START_Y - y) / (ORBIT_START_Y - ORBIT_MAX_Y)));
        }
        return 0.0F;
    }
}