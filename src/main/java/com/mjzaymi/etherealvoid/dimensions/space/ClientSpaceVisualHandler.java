package com.mjzaymi.etherealvoid.dimensions.space;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSpaceVisualHandler {

    private static final ResourceKey<Level> EARTH_KEY = Level.OVERWORLD;
    private static final ResourceKey<Level> ORBIT_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "low_earth_orbit"));

    // 🌟 提取一个公用的计算 Factor 方法，确保颜色和距离使用完全一致的平滑曲线
    private static float getFogFactor(Player player) {
        double y = player.getY();
        ResourceKey<Level> dim = player.level().dimension();

        // 只有在主世界高空才起雾
        if (dim.equals(EARTH_KEY) && y >= 280.0 && y <= 320.0) {
            return (float) ((y - 280.0) / (320.0 - 280.0));
        }
        // 只有在轨道低空才起雾（彻底解决主世界地下起雾的问题！）
        else if (dim.equals(ORBIT_KEY) && y <= -20.0 && y >= -60.0) {
            return (float) ((-20.0 - y) / (-20.0 - (-60.0)));
        }
        return 0.0F;
    }

    // 2. 平滑修改雾的渲染距离
    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;

        float fogFactor = getFogFactor(player);

        if (fogFactor > 0.0F) {
            event.setFogShape(FogShape.CYLINDER);

            float vanillaFarPlane = event.getFarPlaneDistance();
            // 使用 3 次方曲线，让前期的雾更稀薄，后期的雾更浓，过渡极其平滑
            float smoothFactor = (float) Math.pow(fogFactor, 3);

            float targetEnd = vanillaFarPlane - (vanillaFarPlane - 2.0F) * smoothFactor;
            float targetStart = vanillaFarPlane * (1.0F - smoothFactor) * 0.2F - 10.0F * smoothFactor;

            event.setNearPlaneDistance(targetStart);
            event.setFarPlaneDistance(targetEnd);
        }
    }

    // 3. 🌟 核心修复：让雾的颜色随着高度平滑混合（插值 Lerp），解决一瞬间闪现的问题
    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        float fogFactor = getFogFactor(player);

        if (fogFactor > 0.0F) {
            ResourceKey<Level> dim = player.level().dimension();

            // 获取原版当前天气、时间计算出的原本雾色
            float currentR = event.getRed();
            float currentG = event.getGreen();
            float currentB = event.getBlue();

            float targetR, targetG, targetB;

            if (dim.equals(EARTH_KEY)) {
                // 地球高空目标色：纯白大气层云海
                targetR = 0.95F; targetG = 0.95F; targetB = 1.0F;
            } else {
                // 太空低空目标色：漆黑深邃的宇宙灰蓝色
                targetR = 0.02F; targetG = 0.02F; targetB = 0.05F;
            }

            // 🌟 线性插值公式 (Lerp)：随高度将当前颜色逐渐过渡到目标颜色
            // 雾越浓，平滑因子越大，颜色变化越完美
            float smoothColorFactor = (float) Math.pow(fogFactor, 2);
            event.setRed(currentR + (targetR - currentR) * smoothColorFactor);
            event.setGreen(currentG + (targetG - currentG) * smoothColorFactor);
            event.setBlue(currentB + (targetB - currentB) * smoothColorFactor);
        }
    }
}