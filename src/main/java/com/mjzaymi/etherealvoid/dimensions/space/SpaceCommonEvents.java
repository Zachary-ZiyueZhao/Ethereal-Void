package com.mjzaymi.etherealvoid.dimensions.space;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ethereal_void")
public class SpaceCommonEvents {

    private static final ResourceKey<Level> SPACE_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "low_earth_orbit")
    );


    private static final double VANILLA_GRAVITY = 0.08D; // 原版重力常数

    // y=-64 -> g=0.06 ； y=16384 -> g=0.005
    private static final double PLANET_CENTER_OFFSET = 6739.05D;   // 地心偏移量 R
    private static final double GRAVITY_K_CONSTANT = 2673384.0D;   // 引力常数 K

    // 玩家自主施加的 WASD 衰减系数
    private static final double SPACE_WASD_CONTROL_EFFICIENCY = 0.01D;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        var player = event.player;
        if (player.level().dimension().equals(SPACE_DIMENSION)) {

            if (!player.getAbilities().flying) {
                if (!player.onGround() && !player.isInWater() && !player.onClimbable()) {

                    double currentY = player.getY();

                    // 重力加速度
                    double r = currentY + PLANET_CENTER_OFFSET;
                    if (r < 10.0D) r = 10.0D;

                    double dynamicGravity = GRAVITY_K_CONSTANT / (r * r);

                    // 限制重力上下限
                    if (dynamicGravity > 0.06D) dynamicGravity = 0.06D;
                    if (dynamicGravity < 0.001D) dynamicGravity = 0.001D;

                    // 计算空气阻力
                    double dragMultiplier;
                    if (currentY <= -64.0D) {
                        dragMultiplier = 0.98D; // 这个值似乎有点问题，但无所谓了
                    } else if (currentY >= 8192.0D) {
                        dragMultiplier = 1.0D;  // 真空
                    } else {
                        // 在 -64 到 8192 之间，阻力随高度差的平方成正比减少
                        double heightProgress = (currentY + 64.0D) / (8192.0D + 64.0D);
                        double dragReduction = 1.0D - heightProgress;
                        dragMultiplier = 1.0D - (0.02D * dragReduction * dragReduction);
                    }

                    Vec3 currentVelocity = player.getDeltaMovement();

                    double correctedY = currentVelocity.y + VANILLA_GRAVITY - dynamicGravity;

                    double correctedX = currentVelocity.x * dragMultiplier;
                    double correctedZ = currentVelocity.z * dragMultiplier;

                    // 削弱空中 WASD 键
                    if (player.zza != 0 || player.xxa != 0) {
                        correctedX = (correctedX * (1.0 - SPACE_WASD_CONTROL_EFFICIENCY));
                        correctedZ = (correctedZ * (1.0 - SPACE_WASD_CONTROL_EFFICIENCY));
                    }

                    // 合成速度
                    Vec3 newSpaceVector = new Vec3(correctedX, correctedY, correctedZ);
                    player.setDeltaMovement(newSpaceVector);
                    player.hasImpulse = true;
                }
            }
        }
    }
}