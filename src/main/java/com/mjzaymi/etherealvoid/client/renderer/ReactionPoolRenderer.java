package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.block.entity.ReactionPoolBlockEntity;
import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.util.fluid.FluidSorter;
import com.mjzaymi.etherealvoid.util.render.RenderFace;
import com.mjzaymi.etherealvoid.util.render.RenderUtil;
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
        BlockPos min = s.min().offset(1, 1, 1);
        BlockPos max = s.max().offset(-1, 0, -1);

        light = LevelRenderer.getLightColor(be.getLevel(), min);

        float capacity = be.getTank().getCapacity();

        List<FluidStack> fluids =  be.getTank().getFluids();
        fluids.sort(FluidSorter.DENSITY_SORTER);
        float totalHeight = max.getY() - min.getY() + 0.88f;
        float currentHeight = 0;
        for (FluidStack fluidStack : fluids) {
            float amount = fluidStack.getAmount();
            float fillPercentage = Math.min(1.0f, amount / capacity);
            if (fillPercentage <= 0) continue;
            float height = fillPercentage * totalHeight;
            //System.out.println("amount:" + amount);
            //System.out.println("capacity:" + capacity);
            //System.out.println("fillPercentage:" + fillPercentage);
            //System.out.println("height:"+height);

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