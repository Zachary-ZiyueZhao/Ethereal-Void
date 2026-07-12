package com.mjzaymi.etherealvoid.dimensions.space;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpaceTransitionHandler {

    private static final ResourceKey<Level> EARTH_KEY = Level.OVERWORLD;
    private static final ResourceKey<Level> ORBIT_KEY = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "low_earth_orbit"));

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

            if (orbitLevel == null) {
                EtherealVoid.LOGGER.error(
                        "Dimension missing: " + ORBIT_KEY.location()
                );
                // 如果服务器还没初始化它，强行让服务器加载它
                for (ServerLevel level : player.server.getAllLevels()) {
                    if (level.dimension().equals(ORBIT_KEY)) {
                        orbitLevel = level;
                        break;
                    }
                }
            }

            if (orbitLevel != null) {
                COOLDOWN_MAP.put(uuid, currentTime + 60);
                teleportToDimension(player, orbitLevel, player.getX(), ORBIT_MIN_Y + 1, player.getZ());
            }
        }
        // 太空 -> 地球
        else if (currentDim.equals(ORBIT_KEY) && playerY <= ORBIT_MIN_Y) {
            ServerLevel earthLevel = player.server.getLevel(EARTH_KEY);
            if (earthLevel != null) {
                COOLDOWN_MAP.put(uuid, currentTime + 60);
                teleportToDimension(player, earthLevel, player.getX(), EARTH_MAX_Y - 1, player.getZ());
            }
        }
    }

    private static void teleportToDimension(ServerPlayer player, ServerLevel targetLevel, double x, double y, double z) {
        // 1. 🌟 在传送前精准记录玩家当前的动量
        Vec3 originalVelocity = player.getDeltaMovement();

        if (player.isPassenger()) {
            Entity vehicle = player.getVehicle();
            player.stopRiding();
            if (vehicle != null) {
                vehicle.changeDimension(targetLevel, new SpaceTeleporter(x, y, z, originalVelocity.x, originalVelocity.y, originalVelocity.z));
            }
            // 记录跨界后的新玩家实例
            ServerPlayer telePlayer = (ServerPlayer) player.changeDimension(targetLevel, new SpaceTeleporter(x, y, z, originalVelocity.x, originalVelocity.y, originalVelocity.z));
            if (telePlayer != null) {
                // 2. 🌟 恢复玩家跨界后的动量
                restorePlayerVelocity(telePlayer, originalVelocity);
                if (vehicle != null) {
                    telePlayer.startRiding(vehicle, true);
                }
            }
        } else {
            ServerPlayer telePlayer = (ServerPlayer) player.changeDimension(targetLevel, new SpaceTeleporter(x, y, z, originalVelocity.x, originalVelocity.y, originalVelocity.z));
            if (telePlayer != null) {
                // 2. 🌟 恢复玩家跨界后的动量
                restorePlayerVelocity(telePlayer, originalVelocity);
            }
        }
    }

    /**
     * 辅助方法：强制恢复并同步玩家的动量
     */
    private static void restorePlayerVelocity(ServerPlayer player, Vec3 velocity) {
        // 强制写入服务端玩家实例的速度
        player.setDeltaMovement(velocity);

        // 关键：触发脉冲标记，防止原版执行不必要的重置
        player.hasImpulse = true;

        // 🌟 核心：向客户端发送运动量同步数据包，强制刷新客户端的画面，防止两端速度不一致发生瞬移拉扯
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }
}