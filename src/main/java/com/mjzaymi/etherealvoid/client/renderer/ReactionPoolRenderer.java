package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.block.entity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.util.GameUtil;
import com.mjzaymi.etherealvoid.util.fluid.FluidSorter;
import com.mjzaymi.etherealvoid.util.render.RenderFace;
import com.mjzaymi.etherealvoid.util.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ReactionPoolRenderer implements BlockEntityRenderer<ReactionPoolBlockEntity> {

    public ReactionPoolRenderer(BlockEntityRendererProvider.Context ignoredCtx) {
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

        BlockPos bePos = be.getBlockPos();
        BlockPos min = s.interiorMin();
        BlockPos max = s.interiorMax();

        light = LevelRenderer.getLightColor(be.getLevel(), min);


        //Render Items
        var list = be.getPrecipitatesAll();
        long seed = be.getBlockPos().asLong();
        RandomSource random = RandomSource.create(seed);
        for (var stack : list) {
            if (stack.isEmpty()) continue;
            poseStack.pushPose();
            int renderLayers = RenderUtil.getRenderedAmount(stack.getCount());

            var vec3 = GameUtil.getRandomPosition(random,
                    be.getStructure().interiorFloorMin(), be.getStructure().interiorFloorMax());
            for (int i = 0; i < renderLayers; i++) {
                poseStack.pushPose();
                double yOffset = i * 0.025;
                double randomOffset = (random.nextInt(16)) * 0.0020;
                poseStack.translate(vec3.x-bePos.getX(),
                        yOffset + randomOffset + 1,
                        vec3.z-bePos.getZ());

                poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360f));
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
                // (可选) 原版的 GROUND 模式有点大，稍微缩放一下会显得更精致
                poseStack.scale(0.75f, 0.75f, 0.75f);
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        stack,
                        ItemDisplayContext.GROUND,
                        light,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        buffer,
                        be.getLevel(),
                        (int) seed // 传入 seed 确保物品变体一致
                );
                poseStack.popPose();
            }
            poseStack.popPose();
        }
        if (buffer instanceof MultiBufferSource.BufferSource immediateBuffer) immediateBuffer.endBatch();


        //Render Fluids
        float capacity = be.getTankAll().getCapacity();
        List<FluidStack> fluids =  be.getTankAll().getFluids();
        fluids.sort(FluidSorter.DENSITY_SORTER);
        float totalHeight = max.getY() - min.getY() + 0.88f;
        float currentHeight = 0;
        for (FluidStack fluidStack : fluids) {
            float amount = fluidStack.getAmount();
            float fillPercentage = Math.min(1.0f, amount / capacity);
            if (fillPercentage <= 0) continue;
            float height = fillPercentage * totalHeight;

            Fluid fluid = fluidStack.getFluid();
            IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid);
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(ext.getStillTexture(fluidStack));
            int tintColor = ext.getTintColor(fluidStack);
            VertexConsumer builder = buffer.getBuffer(RenderType.translucent());
            float u0 = sprite.getU0();
            float u1 = sprite.getU1();
            float v0 = sprite.getV0();
            float v1 = sprite.getV1();
            RenderUtil.render(builder, poseStack, RenderFace.ALL,
                    bePos, min, max,
                    0.005f, height, currentHeight,
                    tintColor, light,
                    u0, u1, v0, v1);
            currentHeight += height;
        }
    }
}