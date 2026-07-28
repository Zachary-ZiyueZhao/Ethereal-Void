package com.mjzaymi.etherealvoid.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

public class ThickOutlineRenderer {

    /**
     * 将 VoxelShape 的所有棱包裹为 3D 实体小长方体框架
     *
     * @param matrix    变换矩阵
     * @param consumer  顶点消费者 (使用 debugQuads 或 translucent)
     * @param shape     目标形状
     * @param thickness 包边线条的半厚度（例如 0.015f，则线粗为 0.03 单位）
     * @param r, g, b   颜色 RGB (0~1)
     * @param a         透明度 (0~1)
     */
    public static void renderThickEdges(Matrix4f matrix, VertexConsumer consumer, VoxelShape shape, float thickness, float r, float g, float b, float a) {
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float minX = (float) Math.min(x1, x2);
            float maxX = (float) Math.max(x1, x2);
            float minY = (float) Math.min(y1, y2);
            float maxY = (float) Math.max(y1, y2);
            float minZ = (float) Math.min(z1, z2);
            float maxZ = (float) Math.max(z1, z2);

            // 根据棱的延伸方向，在另外两个维度扩展 thickness，构建 3D 实心边框柱
            if (minX != maxX) {
                // X 轴方向的棱
                renderSolidBox(matrix, consumer, minX, minY - thickness, minZ - thickness, maxX, maxY + thickness, maxZ + thickness, r, g, b, a);
            } else if (minY != maxY) {
                // Y 轴方向的棱
                renderSolidBox(matrix, consumer, minX - thickness, minY, minZ - thickness, maxX + thickness, maxY, maxZ + thickness, r, g, b, a);
            } else if (minZ != maxZ) {
                // Z 轴方向的棱
                renderSolidBox(matrix, consumer, minX - thickness, minY - thickness, minZ, maxX + thickness, maxY + thickness, maxZ, r, g, b, a);
            }
        });
    }

    private static void renderSolidBox(Matrix4f matrix, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
        // Down (0, -1, 0)
        addQuad(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a, 0, -1, 0);
        // Up (0, 1, 0)
        addQuad(consumer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, r, g, b, a, 0, 1, 0);
        // North (0, 0, -1)
        addQuad(consumer, matrix, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, a, 0, 0, -1);
        // South (0, 0, 1)
        addQuad(consumer, matrix, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a, 0, 0, 1);
        // West (-1, 0, 0)
        addQuad(consumer, matrix, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a, -1, 0, 0);
        // East (1, 0, 0)
        addQuad(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a, 1, 0, 0);
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b, float a, float nx, float ny, float nz) {
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(nx, ny, nz).endVertex();
        consumer.vertex(matrix, x3, y3, z3).color(r, g, b, a).normal(nx, ny, nz).endVertex();
        consumer.vertex(matrix, x4, y4, z4).color(r, g, b, a).normal(nx, ny, nz).endVertex();
    }
}