package com.mjzaymi.etherealvoid.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

import java.util.Random;

public class SpaceSkyRenderer {

    private static final ResourceLocation EARTH_TEXTURE = ResourceLocation.fromNamespaceAndPath("ethereal_void", "textures/sky/earth_big.png");
    private static final ResourceLocation ATMO_TEXTURE = ResourceLocation.fromNamespaceAndPath("ethereal_void", "textures/sky/clouds.png");

    public static void render(long gameTime, float partialTick, PoseStack poseStack, ClientLevel level) {
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

        // ==========================================
        // 🌌 功能 1：渲染背景静止繁星（放在最底层渲染）
        // ==========================================
        renderStaticStars(poseStack);

        // 🚫 手动渲染太阳、月亮部分逻辑已彻底删除 🚫

        // ==========================================
        // 🌍 保留原功能：渲染地球地面层
        // ==========================================
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        double distanceToCenter = stableY - planetCenterY;
        if (distanceToCenter <= planetRadius) distanceToCenter = planetRadius + 1.0;

        double skySquare = distanceToCenter * distanceToCenter - planetRadius * planetRadius;
        float visualRatio = (float) (planetRadius / Math.sqrt(skySquare)) * sizeMultiplier;

        float finalRenderDistance = (float) (maxSafeDistance / Math.sqrt(2.0 * visualRatio * visualRatio + 1.0));
        if (finalRenderDistance < 4.0F) {
            finalRenderDistance = 4.0F;
        }
        float finalSize = finalRenderDistance * visualRatio;

        float earthScale = finalSize / (float) planetRadius;
        float earthTransX = (float) (-cameraX * earthScale);
        float earthTransZ = (float) (-cameraZ * earthScale);

        poseStack.pushPose();
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
        // ☁️ 保留原功能：渲染动态大气云层
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
                float z1 = z0 + atmoStep; // 笔误修复，此行为原版的 z0 + atmoStep
                float z1Actual = z0 + atmoStep;

                float u0 = (x * uvStep) * cloudTileFactor + uvScrollX;
                float u1 = ((x + 1) * uvStep) * cloudTileFactor + uvScrollX;
                float v0 = (z * uvStep) * cloudTileFactor + uvScrollZ;
                float v1 = ((z + 1) * uvStep) * cloudTileFactor + uvScrollZ;

                bufferbuilder.vertex(atmoMatrix, x0, 0.0F, z0).uv(u0, v0).endVertex();
                bufferbuilder.vertex(atmoMatrix, x0, 0.0F, z1Actual).uv(u0, v1).endVertex();
                bufferbuilder.vertex(atmoMatrix, x1, 0.0F, z1Actual).uv(u1, v1).endVertex();
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

    // 🌟 终极强悍星空：固定世界坐标、真正广告牌、大尺寸方差、极淡深空感
    private static void renderStaticStars(PoseStack poseStack) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // 🛠️ 解决“完全没有”的核心防御：强制清除前置渲染的 Shader 颜色混淆，将基础亮度恢复为 100%
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        // 👑 实时动态提取相机在世界空间中的 Right 向量与 Up 向量
        // 这样可以确保各个顶点的延展方向完全与屏幕平行，生成真正固定在太空背景下的 Billboard
        Minecraft mc = Minecraft.getInstance();
        var camera = mc.gameRenderer.getMainCamera();
        Quaternionf cameraRotation = camera.rotation();
        Vector3f lookRight = new Vector3f(1.0F, 0.0F, 0.0F).rotate(cameraRotation);
        Vector3f lookUp = new Vector3f(0.0F, 1.0F, 0.0F).rotate(cameraRotation);

        // 使用固定种子，确保星星位置与透明度永远保持静止，不闪烁
        Random random = new Random(774910842L);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // 🌟 数量级满足：生成 1200 颗星星，密密麻麻的深空背景
        for (int i = 0; i < 1200; ++i) {
            double x = random.nextFloat() * 2.0F - 1.0F;
            double y = random.nextFloat() * 2.0F - 1.0F;
            double z = random.nextFloat() * 2.0F - 1.0F;
            double d = x * x + y * y + z * z;

            if (d < 1.0 && d > 0.01) {
                d = 1.0 / Math.sqrt(d);

                // 🌟 核心修改：渲染半径确定为 25.0F！
                // 绝对小于你的 32.0F 最低雾气裁剪面，100% 能够避开视锥体裁剪，保证亮起！
                float radius = 25.0F;
                float centerX = (float) (x * d * radius);
                float centerY = (float) (y * d * radius);
                float centerZ = (float) (z * d * radius);

                // 🌟 大小方差显著加大：利用平方随机让极小的宇宙微尘（0.04）占绝对多数，极少数亮星（可达0.3）
                float size = 0.04F + random.nextFloat() * random.nextFloat() * 0.26F;

                // 🌟 颜色再淡一点：Alpha 严格锁定在柔和的 [25 ~ 125] 区间，使其深邃优雅，充当完美背景
                int alpha = 25 + random.nextInt(100);
                int brightness = 190 + random.nextInt(55); // 灰白伪随机色调

                // 依托相机的世界空间方向向量，向外平移出 4 个面朝玩家的顶点
                float ax = centerX - (lookRight.x() + lookUp.x()) * size;
                float ay = centerY - (lookRight.y() + lookUp.y()) * size;
                float az = centerZ - (lookRight.z() + lookUp.z()) * size;

                float bx = centerX + (lookRight.x() - lookUp.x()) * size;
                float by = centerY + (lookRight.y() - lookUp.y()) * size;
                float bz = centerZ + (lookRight.z() - lookUp.z()) * size;

                float cx = centerX + (lookRight.x() + lookUp.x()) * size;
                float cy = centerY + (lookRight.y() + lookUp.y()) * size;
                float cz = centerZ + (lookRight.z() + lookUp.z()) * size;

                float dx = centerX - (lookRight.x() - lookUp.x()) * size;
                float dy = centerY - (lookRight.y() - lookUp.y()) * size;
                float dz = centerZ - (lookRight.z() - lookUp.z()) * size;

                bufferBuilder.vertex(matrix, ax, ay, az).color(brightness, brightness, brightness, alpha).endVertex();
                bufferBuilder.vertex(matrix, bx, by, bz).color(brightness, brightness, brightness, alpha).endVertex();
                bufferBuilder.vertex(matrix, cx, cy, cz).color(brightness, brightness, brightness, alpha).endVertex();
                bufferBuilder.vertex(matrix, dx, dy, dz).color(brightness, brightness, brightness, alpha).endVertex();
            }
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}