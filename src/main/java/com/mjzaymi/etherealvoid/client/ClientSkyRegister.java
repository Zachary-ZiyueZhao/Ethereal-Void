package com.mjzaymi.etherealvoid.client;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.client.renderer.SpaceSkyRenderer;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSkyRegister {

    private static final ResourceKey<Level> ORBIT_KEY = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "low_earth_orbit"));

    private static final double ATMOSPHERE_START_Y = -64.0D;  // 大气层底部（刚进宇宙的极低空）
    private static final double ATMOSPHERE_END_Y = 64.0D;    // 大气层完全消散、化为纯净深空的高度

    @SubscribeEvent

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension().equals(ORBIT_KEY)) {

            Player player = mc.player;
            if (player == null) return;

            double currentY = player.getY();

            if (currentY <= ATMOSPHERE_START_Y) {
                event.setNearPlaneDistance(0.0F);
                event.setFarPlaneDistance(32.0F);
                event.setCanceled(true);
            }

            else if (currentY >= ATMOSPHERE_END_Y) {
                event.setNearPlaneDistance(99999.0F);
                event.setFarPlaneDistance(100000.0F);
                event.setCanceled(true);
            }
            else {
                double progress = (currentY - ATMOSPHERE_START_Y) / (ATMOSPHERE_END_Y - ATMOSPHERE_START_Y);

                double fogThinningFactor = progress * progress;

                float dynamicFogStart = (float) (0.0D + (128.0D * fogThinningFactor));
                float dynamicFogEnd = (float) (32.0D + (1000.0D * fogThinningFactor));

                event.setNearPlaneDistance(dynamicFogStart);
                event.setFarPlaneDistance(dynamicFogEnd);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension().equals(ORBIT_KEY)) {
            var player = mc.player;
            if (player == null) return;

            double currentY = player.getY();

            // 1. 低空：保持你群系或你想呈现的初始雾气颜色（例如淡淡的宇宙灰色/深蓝色）
            // 假设低空初始雾色为 R=0.05, G=0.08, B=0.12 (可以根据喜好自定)
            float startR = 186.0F/255.0F;
            float startG = 216.0F/255.0F;
            float startB = 227.0F/255.0F;

            // 2. 高空：完全透明/融入宇宙背景的终点颜色（全黑）
            float endR = 0.0F;
            float endG = 0.0F;
            float endB = 0.0F;

            if (currentY <= ATMOSPHERE_START_Y) {
                event.setRed(startR);
                event.setGreen(startG);
                event.setBlue(startB);
            }
            else if (currentY >= ATMOSPHERE_END_Y) {
                // 🌌 达到深空：强制变成纯黑，等同于雾气本体 100% 变全透明隐形
                event.setRed(endR);
                event.setGreen(endG);
                event.setBlue(endB);
            }
            else {
                // 3. 渐变区：这里完美同步你的 progress * progress 平方消散曲线
                double progress = (currentY - ATMOSPHERE_START_Y) / (ATMOSPHERE_END_Y - ATMOSPHERE_START_Y);
                float fogThinningFactor = (float) (progress * progress);

                // 根据你 RenderFog 的消散曲线，等比例让颜色衰减至纯黑
                // 随着 fogThinningFactor 逼近 1.0，颜色会极其平滑地淡化掉
                float currentR = startR + (endR - startR) * fogThinningFactor;
                float currentG = startG + (endG - startG) * fogThinningFactor;
                float currentB = startB + (endB - startB) * fogThinningFactor;

                event.setRed(currentR);
                event.setGreen(currentG);
                event.setBlue(currentB);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.dimension().equals(ORBIT_KEY)) {
                SpaceSkyRenderer.render(
                        mc.level.getGameTime(),
                        event.getPoseStack()
                );
            }
        }
    }
}