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

        double planetRadius = 48000.0;
        double planetCenterY = -340.0 - planetRadius;

        // 获取当前相机的真实世界 Y 坐标
        double cameraY = mc.gameRenderer.getMainCamera().getPosition().y;

        // 计算相机到虚拟地心的真实距离
        double distanceToCenter = cameraY - planetCenterY;

        // 安全锁
        if (distanceToCenter <= planetRadius) {
            distanceToCenter = planetRadius + 1.0;
        }

        // 虚拟平面原本挂在相机下方 100 格
        float renderDistance = 100.0F;

        // 算出原本需要的 size
        double skySquare = distanceToCenter * distanceToCenter - planetRadius * planetRadius;
        float size = (float) (renderDistance * (planetRadius / Math.sqrt(skySquare)));

        // 设想将平面的最远端顶点（四个角）的绝对距离强行锁死在 30 格的绝对安全区内
        float maxSafeDistance = 30.0F;

        // 根据勾股定理，计算出大平面原坐标下的最远距离，从而得出收缩系数 k
        float k = (float) (maxSafeDistance / Math.sqrt(2.0 * size * size + renderDistance * renderDistance));

        // 算出收缩后的真实安全渲染距离和安全尺寸
        float finalRenderDistance = renderDistance * k;
        float finalSize = size * k;

        // 渲染状态设置
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.pushPose();

        // 位置变换
        poseStack.translate(0.0F, -finalRenderDistance, 0.0F);

        // 星球自转
        poseStack.mulPose(Axis.YP.rotationDegrees(gameTime * 0.005F));

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        int split = 4;
        // 所有的顶点生成全部基于 finalSize
        float step = finalSize * 2.0F / split;
        float uvStep = 1.0F / split;

        for (int x = 0; x < split; x++) {
            for (int z = 0; z < split; z++) {

                float x0 = -finalSize + x * step;
                float x1 = x0 + step;

                float z0 = -finalSize + z * step;
                float z1 = z0 + step;

                float u0 = x * uvStep;
                float u1 = u0 + uvStep;

                float v0 = z * uvStep;
                float v1 = v0 + uvStep;

                bufferbuilder.vertex(matrix, x0, 0.0F, z0).uv(u0, v0).endVertex();
                bufferbuilder.vertex(matrix, x0, 0.0F, z1).uv(u0, v1).endVertex();
                bufferbuilder.vertex(matrix, x1, 0.0F, z1).uv(u1, v1).endVertex();
                bufferbuilder.vertex(matrix, x1, 0.0F, z0).uv(u1, v0).endVertex();
            }
        }

        BufferUploader.drawWithShader(bufferbuilder.end());

        poseStack.popPose();

        // 恢复渲染状态
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}