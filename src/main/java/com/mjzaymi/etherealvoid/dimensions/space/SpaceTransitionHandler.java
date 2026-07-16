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
                EtherealVoid.LOGGER.error("Dimension missing: " + ORBIT_KEY.location());
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
        // 🌟【修改点 1】：精准捕获速度。如果是骑乘状态，必须以载具（火箭）的向上速度为准，否则以玩家自身为准。
        Vec3 originalVelocity = player.isPassenger() && player.getVehicle() != null
                ? player.getVehicle().getDeltaMovement()
                : player.getDeltaMovement();

        if (player.isPassenger()) {
            Entity vehicle = player.getVehicle();

            // 在传送前让玩家下车，避免原版维度传送系统因“骑乘中跨界”产生死锁或同步崩溃
            player.stopRiding();

            if (vehicle != null) {
                // 🌟【修改点 2】：核心修复！
                // 调用 changeDimension 会克隆并返回目标维度中的【全新火箭实例 (teleVehicle)】。
                // 我们必须用变量接住它。
                Entity teleVehicle = vehicle.changeDimension(targetLevel, new SpaceTeleporter(x, y, z, originalVelocity.x, originalVelocity.y, originalVelocity.z));

                // 传送玩家自己，返回目标维度中的【新玩家实例 (telePlayer)】
                ServerPlayer telePlayer = (ServerPlayer) player.changeDimension(targetLevel, new SpaceTeleporter(x, y, z, originalVelocity.x, originalVelocity.y, originalVelocity.z));

                if (telePlayer != null && teleVehicle != null) {
                    // 🌟【修改点 3】：为新维度的火箭和玩家同时赋予传送前的惯性速度
                    teleVehicle.setDeltaMovement(originalVelocity);
                    restorePlayerVelocity(telePlayer, originalVelocity);

                    // 🌟【修改点 4】：核心修复！
                    // 让新维度的玩家，重新骑乘新维度的火箭，实现无缝对接，玩家不会下车！
                    telePlayer.startRiding(teleVehicle, true);
                }
            }
        } else {
            // 非骑乘状态下的普通传送
            ServerPlayer telePlayer = (ServerPlayer) player.changeDimension(targetLevel, new SpaceTeleporter(x, y, z, originalVelocity.x, originalVelocity.y, originalVelocity.z));
            if (telePlayer != null) {
                restorePlayerVelocity(telePlayer, originalVelocity);
            }
        }
    }

    /**
     * 辅助方法：强制恢复并同步玩家的动量
     */
    private static void restorePlayerVelocity(ServerPlayer player, Vec3 velocity) {
        player.setDeltaMovement(velocity);
        player.hasImpulse = true;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }
}