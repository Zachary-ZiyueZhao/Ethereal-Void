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
        renderBillboardStars(poseStack);

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
                float z1 = z0 + atmoStep;
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

    // 🌟 终极完美版：固定在宇宙、完美广告牌面向玩家、数量繁多、方差极大、色彩淡雅
    private static void renderBillboardStars(PoseStack poseStack) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // 🛠️ 核心防灭灯：强行重置 Shader 颜色，防止被其他渲染器的残留 Alpha 滤成透明隐形
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        // 提取原版视图旋转与位移矩阵（不抹平它，完整保留）
        Matrix4f modelViewMatrix = poseStack.last().pose();

        // 👑 创建一个纯净的单位矩阵（Identity），用于最后塞入顶点，防止显卡对变换后的坐标进行二次旋转
        Matrix4f identityMatrix = new Matrix4f().identity();

        // 使用固定种子，确保星星位置和透明度固定，不出现每帧随机闪烁
        Random random = new Random(432110842L);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // 🌟 要求 1：星星再多一点（从 600 猛增至 1500 颗，营造极其浩瀚的深空感）
        for (int i = 0; i < 3000; ++i) {
            // 计算随机的 3D 球体投射坐标（这代表星星在宇宙世界中的绝对方向）
            double x = random.nextFloat() * 2.0F - 1.0F;
            double y = random.nextFloat() * 2.0F - 1.0F;
            double z = random.nextFloat() * 2.0F - 1.0F;
            double d = x * x + y * y + z * z;

            if (d < 1.0 && d > 0.01) {
                d = 1.0 / Math.sqrt(d);
                // 保持你原本能完美显现的 95.0 远景半径
                double worldX = x * d * 95.0;
                double worldY = y * d * 95.0;
                double worldZ = z * d * 95.0;

                // 🌟 要求 2：大小方差再大一点（利用平方随机 nextFloat * nextFloat）
                // 这样会让绝大多数星星变成细小的微尘（0.02），但极少数会成为特别瞩目的大亮星（最高 0.38）
                float size = 0.02F + random.nextFloat() * random.nextFloat() * 0.36F;

                // 🌟 要求 3：颜色再淡一点（大幅压低 Alpha 至 15~95，降低基础亮度，使其深邃优雅不喧宾夺主）
                int alpha = 50 + random.nextInt(100);
                int brightness = 160 + random.nextInt(75);

                // 👑 核心魔法：将世界坐标下的星体中心点，通过原版矩阵变换到【屏幕视角空间（View Space）】
                // 这一步做完后，viewPos.x 和 viewPos.y 就是该星星在玩家屏幕上的直观投影中心，viewPos.z 是深度
                org.joml.Vector4f viewPos = new org.joml.Vector4f((float)worldX, (float)worldY, (float)worldZ, 1.0F);
                modelViewMatrix.transform(viewPos);

                // 👑 既然中心点已经在屏幕空间了，直接在 X 和 Y 轴上加上 size 偏移，就是完美的正对屏幕广告牌！
                // 此处传入 identityMatrix，告诉 Shader 坐标已经转好了，直接画就行！
                bufferBuilder.vertex(identityMatrix, viewPos.x - size, viewPos.y - size, viewPos.z).color(brightness, brightness, brightness, alpha).endVertex();
                bufferBuilder.vertex(identityMatrix, viewPos.x + size, viewPos.y - size, viewPos.z).color(brightness, brightness, brightness, alpha).endVertex();
                bufferBuilder.vertex(identityMatrix, viewPos.x + size, viewPos.y + size, viewPos.z).color(brightness, brightness, brightness, alpha).endVertex();
                bufferBuilder.vertex(identityMatrix, viewPos.x - size, viewPos.y + size, viewPos.z).color(brightness, brightness, brightness, alpha).endVertex();
            }
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}