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
    private static final double PLANET_CENTER_OFFSET = 6739.05D;   // 地心偏移量 R
    private static final double GRAVITY_K_CONSTANT = 2673384.0D;   // 引力常数 K
    private static final double SPACE_WASD_CONTROL_EFFICIENCY = 0.01D;
    private static final double VACUUM_HEIGHT = 3840.0D;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        var player = event.player;
        if (player.level().dimension().equals(SPACE_DIMENSION)) {

            if (!player.getAbilities().flying) {
                if (!player.onGround() && !player.isInWater() && !player.onClimbable()) {

                    double currentY = player.getY();

                    // 1. 计算当前高度对应的动态重力加速度
                    double r = currentY + PLANET_CENTER_OFFSET;
                    if (r < 10.0D) r = 10.0D;

                    double dynamicGravity = GRAVITY_K_CONSTANT / (r * r);

                    if (dynamicGravity > 0.06D) dynamicGravity = 0.06D;
                    if (dynamicGravity < 0.001D) dynamicGravity = 0.001D;

                    // 2. 计算空气阻力
                    double dragMultiplier;
                    if (currentY <= -64.0D) {
                        dragMultiplier = 0.98D;
                    } else if (currentY >= VACUUM_HEIGHT) {
                        dragMultiplier = 1.0D;
                    } else {
                        double heightProgress = (currentY + 64.0D) / (VACUUM_HEIGHT + 64.0D);
                        double dragReduction = 1.0D - heightProgress;
                        dragMultiplier = 1.0D - (0.02D * dragReduction * dragReduction);
                    }

                    Vec3 currentVelocity = player.getDeltaMovement();

                    double correctedX = currentVelocity.x * dragMultiplier;
                    double correctedZ = currentVelocity.z * dragMultiplier;
                    double correctedY;

                    // 🌟 3. 宇宙维度专属：物理加速下坠逻辑（严格限定仅对鞘翅生效）
                    if (player.isFallFlying()) {

                        // 计算当前高度下，宇宙基础的下落趋势常数
                        double baseSpaceDownwardTrend = VANILLA_GRAVITY - dynamicGravity;

                        if (currentVelocity.y > 0.0D) {
                            // 🚀 场景 A：从主世界开鞘翅冲上来，带着向上惯性
                            // 允许向上冲刺，但没有火箭动力会迅速耗尽
                            correctedY = currentVelocity.y - baseSpaceDownwardTrend;

                            // 废除火箭水平推力
                            if (currentVelocity.horizontalDistanceSqr() > 1.5D) {
                                correctedX = currentVelocity.x * 0.5D;
                                correctedZ = currentVelocity.z * 0.5D;
                            }
                        } else {
                            // 📉 场景 B：开始下坠，开启真正的【滑翔专享·加速自由落体】
                            correctedX = 0.0D;
                            correctedZ = 0.0D;

                            // 在上一帧 Y 轴速度基础上源源不断叠加下坠速度
                            correctedY = currentVelocity.y - (baseSpaceDownwardTrend * 1.5D);

                            // 绝杀原版鞘翅视角抬高产生的假升力
                            if (correctedY > -0.04D) {
                                correctedY = Math.min(currentVelocity.y, 0.0D) - 0.04D;
                            }

                            // 终端下落速度截断
                            if (correctedY < -3.5D) {
                                correctedY = -3.5D;
                            }
                        }
                    } else {
                        // 🌟 4. 完美的太空微重力漂浮公式（非滑翔状态）
                        // 恢复成你最初调好的物理公式，保证普通的跳跃和漂浮体感绝不改变
                        correctedY = currentVelocity.y + VANILLA_GRAVITY - dynamicGravity;
                    }

                    // 削弱空中 WASD 键（非宇宙滑翔状态下生效）
                    if (!player.isFallFlying() && (player.zza != 0 || player.xxa != 0)) {
                        correctedX = (correctedX * (1.0 - SPACE_WASD_CONTROL_EFFICIENCY));
                        correctedZ = (correctedZ * (1.0 - SPACE_WASD_CONTROL_EFFICIENCY));
                    }

                    // 5. 合成最终速度并应用
                    Vec3 newSpaceVector = new Vec3(correctedX, correctedY, correctedZ);
                    player.setDeltaMovement(newSpaceVector);
                    player.hasImpulse = true;
                }
            }
        }
    }
}