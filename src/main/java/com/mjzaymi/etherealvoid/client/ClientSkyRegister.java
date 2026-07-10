package com.mjzaymi.etherealvoid.client;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.client.renderer.SpaceSkyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSkyRegister {

    private static final ResourceKey<Level> ORBIT_KEY = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "low_earth_orbit"));

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension().equals(ORBIT_KEY)) {
            event.setNearPlaneDistance(99999.0F);
            event.setFarPlaneDistance(100000.0F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        // 改用 AFTER_SKY 阶段
        // 这样底下的星球就属于“天空背景”，玩家放的方块、空间站可以完美遮挡住星球！
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