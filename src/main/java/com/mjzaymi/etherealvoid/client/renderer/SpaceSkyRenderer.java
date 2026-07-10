package com.mjzaymi.etherealvoid.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class SpaceSkyRenderer {

    private static final ResourceLocation EARTH_TEXTURE = ResourceLocation.fromNamespaceAndPath("ethereal_void", "textures/sky/earth_big.png");

    public static void render(long gameTime, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 🌟 1. 重新设定符合“低轨道”的硬核物理参数
        double planetRadius = 20000.0;
        double planetCenterY = -340.0 - planetRadius;

        // 获取当前相机的真实世界 Y 坐标
        double cameraY = mc.gameRenderer.getMainCamera().getPosition().y;

        // 计算相机到虚拟地心的真实距离
        double distanceToCenter = cameraY - planetCenterY;

        // 安全锁：防止玩家跌进地心导致数学报错
        if (distanceToCenter <= planetRadius) {
            distanceToCenter = planetRadius + 1.0;
        }

        // 平面固定挂在相机下方 100 格
        float renderDistance = 100.0F;

        // 🌟 2. 【核心突破】球体切线视区算法
        // 利用勾股定理计算出真正贴近地表时的缩放比。
        // 当玩家在 Y=64 时，距离地表极近，这个公式会让 size 飙升到 300~500，让地球几乎“贴脸”！
        double skySquare = distanceToCenter * distanceToCenter - planetRadius * planetRadius;
        float size = (float) (renderDistance * (planetRadius / Math.sqrt(skySquare)));

        // 🌟 3. 渲染状态设置
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest(); // 强行关闭深度测试，确保它是巨大的天空背景

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.pushPose();

        // 🌟 4. 位置变换
        // 直接向下平移 100 格，来到视线正下方
        poseStack.translate(0.0F, -renderDistance, 0.0F);

        // 星球自转
        poseStack.mulPose(Axis.YP.rotationDegrees(gameTime * 0.05F));

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // 🌟 5. 顶点映射（由于 size 现在能突破 100 的限制，它会铺满你屏幕下方的整片世界！）
        bufferbuilder.vertex(matrix, -size, 0.0F, -size).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix, -size, 0.0F, size).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix, size, 0.0F, size).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix, size, 0.0F, -size).uv(1.0F, 0.0F).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());

        poseStack.popPose();

        // 恢复渲染状态
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}