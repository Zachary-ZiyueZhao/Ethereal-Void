package com.mjzaymi.etherealvoid.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RenderUtil {
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
        for (int x = minX_i; x <= maxX_i; x++) {
            for (int y = minY_i; y <= maxY_i; y++) {
                float startX = (x == minX_i) ? totalMinX : (float) x;
                float endX = (x == maxX_i) ? totalMaxX : (float) (x + 1);

                float startY = (y <= totalMinY) ? totalMinY : (float) y;
                if (startY >= currentMaxY) continue;
                float endY = (y == maxY_i) ? currentMaxY : Math.min(currentMaxY, (float) (y + 1));

                // 北面 (North, -Z)
                if (renderFace.NORTH) {
                    builder.vertex(posMatrix, endX, startY, totalMinZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                    builder.vertex(posMatrix, startX, startY, totalMinZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                    builder.vertex(posMatrix, startX, endY, totalMinZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                    builder.vertex(posMatrix, endX, endY, totalMinZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                }

                // 南面 (South, +Z)
                if (renderFace.SOUTH) {
                    builder.vertex(posMatrix, startX, startY, totalMaxZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                    builder.vertex(posMatrix, endX, startY, totalMaxZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                    builder.vertex(posMatrix, endX, endY, totalMaxZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                    builder.vertex(posMatrix, startX, endY, totalMaxZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                }
            }
        }

        for (int z = minZ_i; z <= maxZ_i; z++) {
            for (int y = minY_i; y <= maxY_i; y++) {
                float startZ = (z == minZ_i) ? totalMinZ : (float) z;
                float endZ = (z == maxZ_i) ? totalMaxZ : (float) (z + 1);

                float startY = (y <= totalMinY) ? totalMinY : (float) y;
                if (startY >= currentMaxY) continue;
                float endY = (y == maxY_i) ? currentMaxY : Math.min(currentMaxY, (float) (y + 1));

                // 西面 (West, -X)
                if (renderFace.WEST) {
                    builder.vertex(posMatrix, totalMinX, startY, startZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMinX, startY, endZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMinX, endY, endZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMinX, endY, startZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                }

                // 东面 (East, +X)
                if (renderFace.EAST) {
                    builder.vertex(posMatrix, totalMaxX, startY, endZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMaxX, startY, startZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMaxX, endY, startZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                    builder.vertex(posMatrix, totalMaxX, endY, endZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                }
            }
        }

        for (int x = minX_i; x <= maxX_i; x++) {
            for (int z = minZ_i; z <= maxZ_i; z++) {
                float startX = (x == minX_i) ? totalMinX : (float) x;
                float endX = (x == maxX_i) ? totalMaxX : (float) (x + 1);
                float startZ = (z == minZ_i) ? totalMinZ : (float) z;
                float endZ = (z == maxZ_i) ? totalMaxZ : (float) (z + 1);

                // 顶面 (Top, 向 +Y)
                if (renderFace.TOP) {
                    builder.vertex(posMatrix, startX, currentMaxY, endZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                    builder.vertex(posMatrix, endX, currentMaxY, endZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                    builder.vertex(posMatrix, endX, currentMaxY, startZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                    builder.vertex(posMatrix, startX, currentMaxY, startZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                }

                // 底面 (Bottom, 向 -Y)
                if (renderFace.BOTTOM) {
                    builder.vertex(posMatrix, startX, totalMinY, startZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                    builder.vertex(posMatrix, endX, totalMinY, startZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                    builder.vertex(posMatrix, endX, totalMinY, endZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                    builder.vertex(posMatrix, startX, totalMinY, endZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                }
            }
        }
    }
}
