package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.block.entity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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

import java.util.Optional;

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

        CuboidStructure s = be.getStructure();
        if (s == null) return;
        if (be.getLevel() == null) return;

        BlockPos min = s.min();
        BlockPos max = s.max();
        min = new BlockPos(min.offset(1, 1, 1));
        max = new BlockPos(max.offset(-1, 0, -1));
        System.out.println("123");
        System.out.println(min);
        System.out.println(max);

        // 【警告】请把下面的 fill 逻辑移到 BlockEntity 的 server tick 中，不要放在渲染里！
        be.getTank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);

        FluidStack fluidStack = be.getTank().getFluid();
        if (fluidStack.isEmpty()) return;


        // 1. 计算填充比例
        float capacity = be.getTank().getCapacity();
        float amount = fluidStack.getAmount();
        float fillPercentage = Math.min(1.0f, amount / capacity);
        if (fillPercentage <= 0) return;

        // 2. 将绝对坐标转换为相对 BlockEntity 的局部坐标，并计算方块范围
        BlockPos bePos = be.getBlockPos();
        float epsilon = 0.005f; // 防止 Z-Fighting 的微小缩进

        int minX_i = min.getX() - bePos.getX();
        int minY_i = min.getY() - bePos.getY();
        int minZ_i = min.getZ() - bePos.getZ();
        int maxX_i = max.getX() - bePos.getX();
        int maxY_i = max.getY() - bePos.getY();
        int maxZ_i = max.getZ() - bePos.getZ();

        // 计算流体覆盖的物理边界
        float totalMinX = minX_i + epsilon;
        float totalMinY = minY_i + epsilon;
        float totalMinZ = minZ_i + epsilon;
        float totalMaxX = maxX_i + 1.0f - epsilon;
        float totalMaxY = maxY_i + 1.0f - epsilon;
        float totalMaxZ = maxZ_i + 1.0f - epsilon;

        // 计算最高处 Y 轴
        float vanillaWaterFullHeight = 0.88f;
        float totalRenderableHeight = (totalMaxY - totalMinY) * vanillaWaterFullHeight;
        float currentMaxY = totalMinY + totalRenderableHeight * fillPercentage;

        // 3. 获取纹理和颜色
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
        light = LevelRenderer.getLightColor(be.getLevel(), min);
        System.out.println(light);

        // 4. 获取顶点构建器 (使用 Translucent 透明层)
        VertexConsumer builder = buffer.getBuffer(RenderType.translucent());
        Matrix4f posMatrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // 5. 遍历渲染 (平铺纹理 + 修正了所有面的 CCW 逆时针渲染顺序防止剔除)

        // 5. 遍历渲染 (平铺纹理 + 彻底修正顶点环绕顺序，确保从“外部”看为正)

        // --- A. 顶面 (Top) 和 底面 (Bottom) ---
        for (int x = minX_i; x <= maxX_i; x++) {
            for (int z = minZ_i; z <= maxZ_i; z++) {
                float startX = (x == minX_i) ? totalMinX : (float) x;
                float endX = (x == maxX_i) ? totalMaxX : (float) (x + 1);
                float startZ = (z == minZ_i) ? totalMinZ : (float) z;
                float endZ = (z == maxZ_i) ? totalMaxZ : (float) (z + 1);

                // 顶面 (Top, 向 +Y)
                builder.vertex(posMatrix, startX, currentMaxY, endZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                builder.vertex(posMatrix, endX, currentMaxY, endZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                builder.vertex(posMatrix, endX, currentMaxY, startZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
                builder.vertex(posMatrix, startX, currentMaxY, startZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();

                // 底面 (Bottom, 向 -Y)
                builder.vertex(posMatrix, startX, totalMinY, startZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                builder.vertex(posMatrix, endX, totalMinY, startZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                builder.vertex(posMatrix, endX, totalMinY, endZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
                builder.vertex(posMatrix, startX, totalMinY, endZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, 0, -1, 0).endVertex();
            }
        }

        // --- B. 北面 (North, 向 -Z) 和 南面 (South, 向 +Z) ---
        for (int x = minX_i; x <= maxX_i; x++) {
            for (int y = minY_i; y <= maxY_i; y++) {
                float startX = (x == minX_i) ? totalMinX : (float) x;
                float endX = (x == maxX_i) ? totalMaxX : (float) (x + 1);

                float startY = (y == minY_i) ? totalMinY : (float) y;
                if (startY >= currentMaxY) continue;
                float endY = (y == maxY_i) ? currentMaxY : Math.min(currentMaxY, (float) (y + 1));

                // 北面 (North, -Z)
                builder.vertex(posMatrix, endX, startY, totalMinZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                builder.vertex(posMatrix, startX, startY, totalMinZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                builder.vertex(posMatrix, startX, endY, totalMinZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();
                builder.vertex(posMatrix, endX, endY, totalMinZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, 0, 0, -1).endVertex();

                // 南面 (South, +Z)
                builder.vertex(posMatrix, startX, startY, totalMaxZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                builder.vertex(posMatrix, endX, startY, totalMaxZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                builder.vertex(posMatrix, endX, endY, totalMaxZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
                builder.vertex(posMatrix, startX, endY, totalMaxZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
            }
        }

        // --- C. 西面 (West, 向 -X) 和 东面 (East, 向 +X) ---
        for (int z = minZ_i; z <= maxZ_i; z++) {
            for (int y = minY_i; y <= maxY_i; y++) {
                float startZ = (z == minZ_i) ? totalMinZ : (float) z;
                float endZ = (z == maxZ_i) ? totalMaxZ : (float) (z + 1);

                float startY = (y == minY_i) ? totalMinY : (float) y;
                if (startY >= currentMaxY) continue;
                float endY = (y == maxY_i) ? currentMaxY : Math.min(currentMaxY, (float) (y + 1));

                // 西面 (West, -X)
                builder.vertex(posMatrix, totalMinX, startY, startZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                builder.vertex(posMatrix, totalMinX, startY, endZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                builder.vertex(posMatrix, totalMinX, endY, endZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();
                builder.vertex(posMatrix, totalMinX, endY, startZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, -1, 0, 0).endVertex();

                // 东面 (East, +X)
                builder.vertex(posMatrix, totalMaxX, startY, endZ).color(red, green, blue, alpha).uv(u0, v1).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                builder.vertex(posMatrix, totalMaxX, startY, startZ).color(red, green, blue, alpha).uv(u1, v1).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                builder.vertex(posMatrix, totalMaxX, endY, startZ).color(red, green, blue, alpha).uv(u1, v0).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
                builder.vertex(posMatrix, totalMaxX, endY, endZ).color(red, green, blue, alpha).uv(u0, v0).uv2(light).normal(normalMatrix, 1, 0, 0).endVertex();
            }
        }
    }
}