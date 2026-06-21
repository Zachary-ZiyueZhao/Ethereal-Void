package com.mjzaymi.etherealvoid.common.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RenderUtil {
    public static int getRenderedAmount(int count) {
        if (count > 48) return 5;
        if (count > 32) return 4;
        if (count > 15) return 3;
        if (count > 1) return 2;
        return 1;
    }
    private static float sliceV(float v0, float v1, float sliceHeight) {
        sliceHeight = Math.max(0.0f, Math.min(1.0f, sliceHeight));
        return v0 + (v1 - v0) * sliceHeight;
    }
    public static void render(VertexConsumer builder, PoseStack poseStack, RenderFace renderFace,
                              BlockPos bePos, BlockPos min, BlockPos max,
                              float epsilon, float height, float fromBottom,
                              int tintColor, int light,
                              float u0, float u1, float v0, float v1) {
        Matrix4f posMatrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();
        int minX_i = min.getX() - bePos.getX();
        int minY_i = min.getY() - bePos.getY();
        int minZ_i = min.getZ() - bePos.getZ();
        int maxX_i = max.getX() - bePos.getX();
        int maxY_i = max.getY() - bePos.getY();
        int maxZ_i = max.getZ() - bePos.getZ();

        float totalMinX = minX_i + epsilon;
        float totalMinZ = minZ_i + epsilon;
        float totalMinY = minY_i;
        totalMinY += fromBottom;
        float totalMaxX = maxX_i + 1.0f - epsilon;
        float totalMaxZ = maxZ_i + 1.0f - epsilon;
        float currentMaxY = totalMinY + height;

        int alpha = (tintColor >> 24) & 0xFF;
        int red = (tintColor >> 16) & 0xFF;
        int green = (tintColor >> 8) & 0xFF;
        int blue = tintColor & 0xFF;

        // ================= 北面 & 南面 =================
        for (int x = minX_i; x <= maxX_i; x++) {
            for (int y = minY_i; y <= maxY_i; y++) {
                float startX = (x == minX_i) ? totalMinX : (float) x;
                float endX = (x == maxX_i) ? totalMaxX : (float) (x + 1);

                if (y <= totalMinY - 1.0f) continue;
                float startY = (y <= totalMinY) ? totalMinY : (float) y;
                if (startY >= currentMaxY) continue;
                float endY = (y == maxY_i) ? currentMaxY : Math.min(currentMaxY, (float) (y + 1));

                // 计算局部坐标 (0.0 ~ 1.0 之间)
                float fracStartX = startX - x;
                float fracEndX = endX - x;
                float fracStartY = startY - y;
                float fracEndY = endY - y;

                // Y轴映射到V轴 (Y越小越接近v1, Y越大越接近v0)
                float vStart = v1 - fracStartY * (v1 - v0);
                float vEnd = v1 - fracEndY * (v1 - v0);

                // 北面 (North, -Z) - X坐标翻转映射
                if (renderFace.NORTH) {
                    float uNorthStart = u1 - fracStartX * (u1 - u0);
                    float uNorthEnd = u1 - fracEndX * (u1 - u0);
                    builder.vertex(posMatrix, endX, startY, totalMinZ).color(red, green, blue, alpha).uv(uNorthEnd, vStart).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                    builder.vertex(posMatrix, startX, startY, totalMinZ).color(red, green, blue, alpha).uv(uNorthStart, vStart).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                    builder.vertex(posMatrix, startX, endY, totalMinZ).color(red, green, blue, alpha).uv(uNorthStart, vEnd).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                    builder.vertex(posMatrix, endX, endY, totalMinZ).color(red, green, blue, alpha).uv(uNorthEnd, vEnd).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                }

                // 南面 (South, +Z) - X坐标正向映射
                if (renderFace.SOUTH) {
                    float uSouthStart = u0 + fracStartX * (u1 - u0);
                    float uSouthEnd = u0 + fracEndX * (u1 - u0);
                    builder.vertex(posMatrix, startX, startY, totalMaxZ).color(red, green, blue, alpha).uv(uSouthStart, vStart).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                    builder.vertex(posMatrix, endX, startY, totalMaxZ).color(red, green, blue, alpha).uv(uSouthEnd, vStart).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                    builder.vertex(posMatrix, endX, endY, totalMaxZ).color(red, green, blue, alpha).uv(uSouthEnd, vEnd).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                    builder.vertex(posMatrix, startX, endY, totalMaxZ).color(red, green, blue, alpha).uv(uSouthStart, vEnd).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                }
            }
        }

        // ================= 西面 & 东面 =================
        for (int z = minZ_i; z <= maxZ_i; z++) {
            for (int y = minY_i; y <= maxY_i; y++) {
                float startZ = (z == minZ_i) ? totalMinZ : (float) z;
                float endZ = (z == maxZ_i) ? totalMaxZ : (float) (z + 1);

                if (y <= totalMinY - 1.0f) continue;
                float startY = (y <= totalMinY) ? totalMinY : (float) y;
                if (startY >= currentMaxY) continue;
                float endY = (y == maxY_i) ? currentMaxY : Math.min(currentMaxY, (float) (y + 1));

                // 计算局部坐标
                float fracStartZ = startZ - z;
                float fracEndZ = endZ - z;
                float fracStartY = startY - y;
                float fracEndY = endY - y;

                float vStart = v1 - fracStartY * (v1 - v0);
                float vEnd = v1 - fracEndY * (v1 - v0);

                // 西面 (West, -X) - Z坐标正向映射
                if (renderFace.WEST) {
                    float uWestStart = u0 + fracStartZ * (u1 - u0);
                    float uWestEnd = u0 + fracEndZ * (u1 - u0);
                    builder.vertex(posMatrix, totalMinX, startY, startZ).color(red, green, blue, alpha).uv(uWestStart, vStart).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMinX, startY, endZ).color(red, green, blue, alpha).uv(uWestEnd, vStart).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMinX, endY, endZ).color(red, green, blue, alpha).uv(uWestEnd, vEnd).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMinX, endY, startZ).color(red, green, blue, alpha).uv(uWestStart, vEnd).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                }

                // 东面 (East, +X) - Z坐标翻转映射
                if (renderFace.EAST) {
                    float uEastStart = u1 - fracStartZ * (u1 - u0);
                    float uEastEnd = u1 - fracEndZ * (u1 - u0);
                    builder.vertex(posMatrix, totalMaxX, startY, endZ).color(red, green, blue, alpha).uv(uEastEnd, vStart).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMaxX, startY, startZ).color(red, green, blue, alpha).uv(uEastStart, vStart).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMaxX, endY, startZ).color(red, green, blue, alpha).uv(uEastStart, vEnd).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMaxX, endY, endZ).color(red, green, blue, alpha).uv(uEastEnd, vEnd).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                }
            }
        }

        // ================= 顶面 & 底面 =================
        for (int x = minX_i; x <= maxX_i; x++) {
            for (int z = minZ_i; z <= maxZ_i; z++) {
                float startX = (x == minX_i) ? totalMinX : (float) x;
                float endX = (x == maxX_i) ? totalMaxX : (float) (x + 1);
                float startZ = (z == minZ_i) ? totalMinZ : (float) z;
                float endZ = (z == maxZ_i) ? totalMaxZ : (float) (z + 1);

                float fracStartX = startX - x;
                float fracEndX = endX - x;
                float fracStartZ = startZ - z;
                float fracEndZ = endZ - z;

                // X 轴统一正向映射为 U
                float uTopStart = u0 + fracStartX * (u1 - u0);
                float uTopEnd = u0 + fracEndX * (u1 - u0);

                // 顶面 (Top, 向 +Y) - Z坐标正向映射为V
                if (renderFace.TOP) {
                    float vTopStart = v0 + fracStartZ * (v1 - v0);
                    float vTopEnd = v0 + fracEndZ * (v1 - v0);
                    builder.vertex(posMatrix, startX, currentMaxY, endZ).color(red, green, blue, alpha).uv(uTopStart, vTopEnd).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                    builder.vertex(posMatrix, endX, currentMaxY, endZ).color(red, green, blue, alpha).uv(uTopEnd, vTopEnd).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                    builder.vertex(posMatrix, endX, currentMaxY, startZ).color(red, green, blue, alpha).uv(uTopEnd, vTopStart).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                    builder.vertex(posMatrix, startX, currentMaxY, startZ).color(red, green, blue, alpha).uv(uTopStart, vTopStart).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                }

                // 底面 (Bottom, 向 -Y) - 底面的V通常是反的
                if (renderFace.BOTTOM) {
                    float vBotStart = v1 - fracStartZ * (v1 - v0);
                    float vBotEnd = v1 - fracEndZ * (v1 - v0);
                    builder.vertex(posMatrix, startX, totalMinY, startZ).color(red, green, blue, alpha).uv(uTopStart, vBotStart).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                    builder.vertex(posMatrix, endX, totalMinY, startZ).color(red, green, blue, alpha).uv(uTopEnd, vBotStart).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                    builder.vertex(posMatrix, endX, totalMinY, endZ).color(red, green, blue, alpha).uv(uTopEnd, vBotEnd).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                    builder.vertex(posMatrix, startX, totalMinY, endZ).color(red, green, blue, alpha).uv(uTopStart, vBotEnd).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                }
            }
        }
    }
}
