package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.block.entity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ReactionPoolRenderer implements BlockEntityRenderer<ReactionPoolBlockEntity> {

    public ReactionPoolRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ReactionPoolBlockEntity be,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource buffer,
                       int light,
                       int overlay) {

        System.out.println("LLLL");

        if (be.getStructure() == null) return;
        System.out.println("123");
        System.out.println(be.getStructure().min());
        System.out.println(be.getStructure().max());

        // 【警告】请把下面的 fill 逻辑移到 BlockEntity 的 server tick 中，不要放在渲染里！
        be.getTank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);

        FluidStack fluidStack = be.getTank().getFluid();
        if (fluidStack.isEmpty()) return;

        CuboidStructure s = be.getStructure();
        BlockPos min = s.min();
        BlockPos max = s.max();

        // 1. 计算当前流体占总容量 senior 比例，从而计算流体渲染的高度
        float capacity = be.getTank().getCapacity();
        float amount = fluidStack.getAmount();
        float fillPercentage = Math.min(1.0f, amount / capacity);
        if (fillPercentage <= 0) return;

        // 2. 将绝对世界坐标转换为相对于当前 BlockEntity 的局部坐标
        BlockPos bePos = be.getBlockPos();

        // 稍微往内缩一点（例如 0.005f），防止流体渲染面与方块内壁重叠导致闪烁 (Z-Fighting)
        float epsilon = 0.005f;

        float minX = min.getX() - bePos.getX() + epsilon;
        float minY = min.getY() - bePos.getY() + epsilon;
        float minZ = min.getZ() - bePos.getZ() + epsilon;

        // max 坐标由于是方块坐标，实际几何边界需要 +1.0f
        float maxX = max.getX() - bePos.getX() + 1.0f - epsilon;
        float maxY = max.getY() - bePos.getY() + 1.0f - epsilon;
        float maxZ = max.getZ() - bePos.getZ() + 1.0f - epsilon;

        // 根据蓄水比例动态调整最高处的 Y 轴坐标
        float currentMaxY = minY + (maxY - minY) * fillPercentage;

        // 3. 获取流体的纹理和颜色 (Forge API)
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ext.getStillTexture(fluidStack));

        int tintColor = ext.getTintColor(fluidStack);
        int alpha = (tintColor >> 24) & 0xFF;
        int red = (tintColor >> 16) & 0xFF;
        int green = (tintColor >> 8) & 0xFF;
        int blue = tintColor & 0xFF;

        // 4. 获取顶点构建器（流体通常使用 Translucent 半透明渲染类型）
        VertexConsumer builder = buffer.getBuffer(RenderType.translucent());

        // 5. 渲染流体盒子的 6 个面
        Matrix4f posMatrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // 顶面 (Top)
        renderFace(builder, posMatrix, normalMatrix, minX, currentMaxY, minZ, maxX, currentMaxY, maxZ, u0, v0, u1, v1, red, green, blue, alpha, light, 0, 1, 0);
        // 底面 (Bottom)
        renderFace(builder, posMatrix, normalMatrix, minX, minY, maxZ, maxX, minY, minZ, u0, v0, u1, v1, red, green, blue, alpha, light, 0, -1, 0);
        // 北面 (North)
        renderFace(builder, posMatrix, normalMatrix, maxX, currentMaxY, minZ, minX, minY, minZ, u0, v0, u1, v1, red, green, blue, alpha, light, 0, 0, -1);
        // 南面 (South)
        renderFace(builder, posMatrix, normalMatrix, minX, currentMaxY, maxZ, maxX, minY, maxZ, u0, v0, u1, v1, red, green, blue, alpha, light, 0, 0, 1);
        // 西面 (West)
        renderFace(builder, posMatrix, normalMatrix, minX, currentMaxY, minZ, minX, minY, maxZ, u0, v0, u1, v1, red, green, blue, alpha, light, -1, 0, 0);
        // 东面 (East)
        renderFace(builder, posMatrix, normalMatrix, maxX, currentMaxY, maxZ, maxX, minY, minZ, u0, v0, u1, v1, red, green, blue, alpha, light, 1, 0, 0);
    }

    private void renderFace(VertexConsumer builder, Matrix4f posMat, Matrix3f normMat,
                            float x1, float y1, float z1, float x2, float y2, float z2,
                            float u0, float v0, float u1, float v1,
                            int r, int g, int b, int a, int light,
                            float nx, float ny, float nz) {

        // 简易地根据面向对 4 个顶点进行正交展开
        if (ny != 0) { // 顶面或底面 (XZ平面)
            addVertex(builder, posMat, normMat, x1, y1, z1, u0, v0, r, g, b, a, light, nx, ny, nz);
            addVertex(builder, posMat, normMat, x1, y1, z2, u0, v1, r, g, b, a, light, nx, ny, nz);
            addVertex(builder, posMat, normMat, x2, y2, z2, u1, v1, r, g, b, a, light, nx, ny, nz);
            addVertex(builder, posMat, normMat, x2, y2, z1, u1, v0, r, g, b, a, light, nx, ny, nz);
        } else if (nx != 0) { // 东面或西面 (YZ平面)
            addVertex(builder, posMat, normMat, x1, y1, z1, u0, v0, r, g, b, a, light, nx, ny, nz);
            addVertex(builder, posMat, normMat, x1, y2, z1, u0, v1, r, g, b, a, light, nx, ny, nz);
            addVertex(builder, posMat, normMat, x1, y2, z2, u1, v1, r, g, b, a, light, nx, ny, nz);
            addVertex(builder, posMat, normMat, x1, y1, z2, u1, v0, r, g, b, a, light, nx, ny, nz);
        } else { // 北面或南面 (XY平面)
            addVertex(builder, posMat, normMat, x1, y1, z1, u0, v0, r, g, b, a, light, nx, ny, nz);
            addVertex(builder, posMat, normMat, x2, y1, z1, u0, v1, r, g, b, a, light, nx, ny, nz);
            addVertex(builder, posMat, normMat, x2, y2, z1, u1, v1, r, g, b, a, light, nx, ny, nz);
            addVertex(builder, posMat, normMat, x1, y2, z1, u1, v0, r, g, b, a, light, nx, ny, nz);
        }
    }

    private void addVertex(VertexConsumer builder, Matrix4f posMat, Matrix3f normMat,
                           float x, float y, float z, float u, float v,
                           int r, int g, int b, int a, int light,
                           float nx, float ny, float nz) {
        builder.vertex(posMat, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .uv2(light)
                .normal(normMat, nx, ny, nz)
                .endVertex();
    }
}