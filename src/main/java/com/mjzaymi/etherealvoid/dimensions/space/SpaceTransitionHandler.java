package com.mjzaymi.etherealvoid.dimensions.space;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpaceTransitionHandler {

    private static final ResourceKey<Level> EARTH_KEY = Level.OVERWORLD;
    private static final ResourceKey<Level> ORBIT_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(EtherealVoid.MOD_ID, "low_earth_orbit"));

    private static final double EARTH_MAX_Y = 320.0;
    private static final double ORBIT_MIN_Y = -60.0;

    private static final Map<UUID, Long> COOLDOWN_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.END) return;

        ServerPlayer player = (ServerPlayer) event.player;
        ServerLevel currentLevel = player.serverLevel();
        ResourceKey<Level> currentDim = currentLevel.dimension();
        UUID uuid = player.getUUID();
        long currentTime = currentLevel.getGameTime();

        if (COOLDOWN_MAP.containsKey(uuid) && currentTime < COOLDOWN_MAP.get(uuid)) {
            return;
        }

        double playerY = player.getY();

        // 地球 -> 太空
        if (currentDim.equals(EARTH_KEY) && playerY >= EARTH_MAX_Y) {
            ServerLevel orbitLevel = player.server.getLevel(ORBIT_KEY);
            if (orbitLevel != null) {
                COOLDOWN_MAP.put(uuid, currentTime + 60);
                // 传送到轨道 -25.0 处，此时客户端会计算出该高度依然带有中等程度的浓雾，随后向上飞时慢慢消散
                teleportToDimension(player, orbitLevel, player.getX(), -59.0, player.getZ());
            }
        }
        // 太空 -> 地球
        else if (currentDim.equals(ORBIT_KEY) && playerY <= ORBIT_MIN_Y) {
            ServerLevel earthLevel = player.server.getLevel(EARTH_KEY);
            if (earthLevel != null) {
                COOLDOWN_MAP.put(uuid, currentTime + 60);
                // 传送到地球 285.0 处，同样带有中等浓雾，向下掉落时平滑消散
                teleportToDimension(player, earthLevel, player.getX(), 319.0, player.getZ());
            }
        }
    }

    private static void teleportToDimension(ServerPlayer player, ServerLevel targetLevel, double x, double y, double z) {
        double motionX = player.getDeltaMovement().x;
        double motionY = player.getDeltaMovement().y;
        double motionZ = player.getDeltaMovement().z;

        if (player.isPassenger()) {
            Entity vehicle = player.getVehicle();
            player.stopRiding();
            if (vehicle != null) {
                vehicle.changeDimension(targetLevel, new SpaceTeleporter(x, y, z, motionX, motionY, motionZ));
            }
            player.changeDimension(targetLevel, new SpaceTeleporter(x, y, z, motionX, motionY, motionZ));
            player.startRiding(vehicle, true);
        } else {
            player.changeDimension(targetLevel, new SpaceTeleporter(x, y, z, motionX, motionY, motionZ));
        }
    }
}