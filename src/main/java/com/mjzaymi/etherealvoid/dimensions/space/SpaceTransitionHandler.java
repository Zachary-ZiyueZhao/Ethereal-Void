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
        Vec3 originalVelocity = player.isPassenger() && player.getVehicle() != null
                ? player.getVehicle().getDeltaMovement()
                : player.getDeltaMovement();

        SpaceTeleporter teleporter = new SpaceTeleporter(x, y, z, originalVelocity.x, originalVelocity.y, originalVelocity.z);

        if (player.isPassenger()) {
            Entity vehicle = player.getVehicle();

            // 1. 玩家先下车
            player.stopRiding();

            if (vehicle != null) {
                // 2. 传送火箭（此时会走我们重写的 placeEntity，绝对精准）
                Entity teleVehicle = vehicle.changeDimension(targetLevel, teleporter);
                // 3. 传送玩家
                ServerPlayer telePlayer = (ServerPlayer) player.changeDimension(targetLevel, teleporter);

                if (telePlayer != null && teleVehicle != null) {
                    // 删除强行的 teleVehicle.moveTo 和 telePlayer.connection.teleport
                    // 因为原版 changeDimension 结合我们的 teleporter 已经把他们放在了精确的位置

                    // 4. 恢复向上惯性速度
                    teleVehicle.setDeltaMovement(originalVelocity);
                    restorePlayerVelocity(telePlayer, originalVelocity);

                    // 5. 在太空重新挂载骑乘
                    telePlayer.startRiding(teleVehicle, true);
                }
            }
        } else {
            ServerPlayer telePlayer = (ServerPlayer) player.changeDimension(targetLevel, teleporter);
            if (telePlayer != null) {
                restorePlayerVelocity(telePlayer, originalVelocity);
            }
        }
    }

    private static void restorePlayerVelocity(ServerPlayer player, Vec3 velocity) {
        player.setDeltaMovement(velocity);
        player.hasImpulse = true;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }
}