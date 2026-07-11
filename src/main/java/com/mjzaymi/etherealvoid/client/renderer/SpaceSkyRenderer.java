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
    private static final ResourceLocation ATMO_TEXTURE = ResourceLocation.fromNamespaceAndPath("ethereal_void", "textures/sky/clouds.png");

    // 🌟【核心改变】：直接接管原版传入的 poseStack，不再自己 new 纯净矩阵
    public static void render(long gameTime, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // ==========================================
        // 🎛️ 终极震撼调参面板
        // ==========================================
        double planetRadius = 48000.0;
        double planetCenterY = -420.0 - planetRadius;
        float sizeMultiplier = 5.0F;

        double atmoHeightOffset = 200.0;
        float cloudTileFactor = 2.0F;
        float cloudSpeedX = 0.0003F;
        float cloudSpeedZ = 0.0001F;
        float maxSafeDistance = 80.0F;

        // 🌟 物理线性插值坐标（仅用于计算比例尺，确保尺寸绝对不抽搐）
        float partialTick = mc.getFrameTime();
        double stableY = mc.player.yo + (mc.player.getY() - mc.player.yo) * partialTick + mc.player.getEyeHeight();

        // 🌟 实时相机坐标（用于方块级平移同步，将原版摇晃完全吸收）
        double cameraX = mc.gameRenderer.getMainCamera().getPosition().x;
        double cameraZ = mc.gameRenderer.getMainCamera().getPosition().z;

        int split = 4;

        // ------------------------------------------
        // 🌍 初始化公共渲染环境
        // ------------------------------------------
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // ==========================================
        // 🌍 渲染地球地面层（原版方块同步版）
        // ==========================================
        double distanceToCenter = stableY - planetCenterY;
        if (distanceToCenter <= planetRadius) distanceToCenter = planetRadius + 1.0;

        double skySquare = distanceToCenter * distanceToCenter - planetRadius * planetRadius;
        float visualRatio = (float) (planetRadius / Math.sqrt(skySquare)) * sizeMultiplier;

        float finalRenderDistance = (float) (maxSafeDistance / Math.sqrt(2.0 * visualRatio * visualRatio + 1.0));
        if (finalRenderDistance < 4.0F) {
            finalRenderDistance = 4.0F;
        }
        float finalSize = finalRenderDistance * visualRatio;

        // 计算缩放并使用实时相机坐标进行相对平移
        float earthScale = finalSize / (float) planetRadius;
        float earthTransX = (float) (-cameraX * earthScale);
        float earthTransZ = (float) (-cameraZ * earthScale);

        // 🌟 直接在原版包含 Bobbing 的矩阵上操作
        poseStack.pushPose();
        // 像渲染一个普通方块一样，把它平移到相对相机的位置
        poseStack.translate(earthTransX, -finalRenderDistance, earthTransZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(gameTime * 0.005F));

        RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

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


        // ==========================================
        // ☁️ 渲染动态大气云层（原版方块同步版）
        // ==========================================
        double atmoRadius = planetRadius + atmoHeightOffset;
        double distanceToAtmo = stableY - planetCenterY;
        if (distanceToAtmo <= atmoRadius) distanceToAtmo = atmoRadius + 1.0;

        double atmoSquare = distanceToAtmo * distanceToAtmo - atmoRadius * atmoRadius;
        float atmoVisualRatio = (float) (atmoRadius / Math.sqrt(atmoSquare)) * sizeMultiplier;

        float finalAtmoRenderDistance = (float) (maxSafeDistance / Math.sqrt(2.0 * atmoVisualRatio * atmoVisualRatio + 1.0));
        if (finalAtmoRenderDistance < 3.9F) {
            finalAtmoRenderDistance = 3.9F;
        }
        float finalAtmoSize = finalAtmoRenderDistance * atmoVisualRatio;

        float atmoScale = finalAtmoSize / (float) atmoRadius;
        float atmoTransX = (float) (-cameraX * atmoScale);
        float atmoTransZ = (float) (-cameraZ * atmoScale);

        float uvScrollX = (gameTime * cloudSpeedX) % 1.0F;
        float uvScrollZ = (gameTime * cloudSpeedZ) % 1.0F;

        poseStack.pushPose();
        poseStack.translate(atmoTransX, -finalAtmoRenderDistance, atmoTransZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(gameTime * 0.005F));

        RenderSystem.setShaderTexture(0, ATMO_TEXTURE);
        Matrix4f atmoMatrix = poseStack.last().pose();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float atmoStep = finalAtmoSize * 2.0F / split;

        for (int x = 0; x < split; x++) {
            for (int z = 0; z < split; z++) {
                float x0 = -finalAtmoSize + x * atmoStep;
                float x1 = x0 + atmoStep;
                float z0 = -finalAtmoSize + z * atmoStep;
                float z1 = z0 + atmoStep;

                float u0 = (x * uvStep) * cloudTileFactor + uvScrollX;
                float u1 = ((x + 1) * uvStep) * cloudTileFactor + uvScrollX;
                float v0 = (z * uvStep) * cloudTileFactor + uvScrollZ;
                float v1 = ((z + 1) * uvStep) * cloudTileFactor + uvScrollZ;

                bufferbuilder.vertex(atmoMatrix, x0, 0.0F, z0).uv(u0, v0).endVertex();
                bufferbuilder.vertex(atmoMatrix, x0, 0.0F, z1).uv(u0, v1).endVertex();
                bufferbuilder.vertex(atmoMatrix, x1, 0.0F, z1).uv(u1, v1).endVertex();
                bufferbuilder.vertex(atmoMatrix, x1, 0.0F, z0).uv(u1, v0).endVertex();
            }
        }
        BufferUploader.drawWithShader(bufferbuilder.end());
        poseStack.popPose();

        // ------------------------------------------
        // 🏁 恢复原版环境
        // ------------------------------------------
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}