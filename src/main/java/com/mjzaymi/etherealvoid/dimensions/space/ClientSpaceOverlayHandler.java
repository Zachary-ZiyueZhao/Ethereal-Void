package com.mjzaymi.etherealvoid.dimensions.space;

import com.mjzaymi.etherealvoid.dimensions.space.ClientSpaceVisualHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ClientSpaceOverlayHandler implements IGuiOverlay {

    public static final ClientSpaceOverlayHandler INSTANCE = new ClientSpaceOverlayHandler();

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        // 获取转场系数 (0.0 ~ 1.0)
        float fogFactor = ClientSpaceVisualHandler.getFogFactor(player);
        if (fogFactor <= 0.0F) return;

        float r, g, b;

        // 🌟 核心修改：统一色彩基准
        // 如果玩家在主世界，我们实时抓取当前主世界的天空色（带昼夜交替）
        if (player.level().dimension().equals(ClientSpaceVisualHandler.EARTH_KEY)) {
            Vec3 skyColor = level.getSkyColor(player.getEyePosition(partialTick), partialTick);
            r = (float) skyColor.x;
            g = (float) skyColor.y;
            b = (float) skyColor.z;

            // 主世界上升：叠加高空云海的反光白度
            float whiteOffset = fogFactor * 0.25F;
            r = Math.min(1.0F, r + whiteOffset);
            g = Math.min(1.0F, g + whiteOffset);
            b = Math.min(1.0F, b + whiteOffset);
        } else {
            // 🌟 太空下降：直接强行映射为主世界“最亮云层”的统一颜色
            // 这样不管太空中有多黑，只要开始下降，屏幕就会亮起和主世界顶层一模一样的耀眼白茫茫色彩
            // 如果你希望太空下降也带有一点动态，可以把这里改成固定的白天色，或者直接写死成纯白/浅蓝
            float whiteOffset = fogFactor * 0.25F;
            r = Math.min(1.0F, 0.4F + whiteOffset); // 对应主世界白天的蔚蓝基底 + 变白
            g = Math.min(1.0F, 0.6F + whiteOffset);
            b = Math.min(1.0F, 1.0F); // 保证B通道饱满
        }

        // 控制遮罩的透明度 (Alpha)
        float alpha = (float) Math.pow(fogFactor, 2);

        // 执行渲染
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // 混合成整数 ARGB
        int colorIdx = ((int) (alpha * 255) << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);

        // 绘制全屏遮罩
        guiGraphics.fill(0, 0, screenWidth, screenHeight, colorIdx);

        // 恢复状态
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}