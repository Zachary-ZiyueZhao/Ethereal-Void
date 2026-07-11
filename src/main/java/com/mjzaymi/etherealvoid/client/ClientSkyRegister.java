package com.mjzaymi.etherealvoid.client;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.client.renderer.SpaceSkyRenderer;
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

    private static final double ATMOSPHERE_BOTTOM = -64.0D;
    private static final double ATMOSPHERE_TOP = 256.0D; // 大气完全融入背景的渐变终点

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension().equals(ORBIT_KEY)) {
            Player player = mc.player;
            if (player == null) return;

            double currentY = player.getY();


            event.setFogShape(com.mojang.blaze3d.shaders.FogShape.CYLINDER);

            double progress = (currentY - ATMOSPHERE_BOTTOM) / (ATMOSPHERE_TOP - ATMOSPHERE_BOTTOM);
            progress = Math.max(0.0D, Math.min(1.0D, progress));

            double smoothProgress = progress * progress * (3.0D - 2.0D * progress);

            float dynamicStart = (float) (4.0D + (400.0D * smoothProgress));
            float dynamicEnd = (float) (96.0D + (1500.0D * smoothProgress));

            event.setNearPlaneDistance(dynamicStart);
            event.setFarPlaneDistance(dynamicEnd);
            event.setCanceled(true);
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