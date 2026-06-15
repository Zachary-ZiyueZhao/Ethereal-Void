package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public class LyingItemRenderer extends EntityRenderer<ItemEntity> {
    private final ItemRenderer itemRenderer;
    private final ItemEntityRenderer fallback;

    public LyingItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.fallback = new ItemEntityRenderer(context);
        this.shadowRadius = 0.15f;
        this.shadowStrength = 0.75f;
    }

    @Override
    public void render(ItemEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.level() == null) {
            fallback.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        var reactorOpt = CuboidStructure.findFromInterior(
                entity.level(),
                entity.blockPosition()
        );
        if (reactorOpt.isEmpty()) {
            fallback.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        // 只有靠近 reactor 底部才切换渲染引擎
        CuboidStructure reactor = reactorOpt.get();
        double interiorFloorY = reactor.min().getY() + 1.0D;
        if (entity.getY() > interiorFloorY + 0.25D) {
            fallback.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        }

        poseStack.pushPose();
        int stackIndex = getRenderStackIndex(entity);
        double yOffset = stackIndex * 0.025;
        long seed = entity.getId() * 734287L;
        double randomOffset = (seed & 15) * 0.0020;
        poseStack.translate(
                0.0,
                0.04 + yOffset + randomOffset,
                0.0
        );
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.getId() * 37) % 360));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        itemRenderer.renderStatic(
                entity.getItem(),
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static final float CELL_SIZE = 0.30f;
    private static final int MAX_STACK_LAYERS = 8;

    private int getRenderStackIndex(ItemEntity entity) {
        int cellX = (int)Math.floor(entity.getX() / CELL_SIZE);
        int cellZ = (int)Math.floor(entity.getZ() / CELL_SIZE);
        int index = 0;

        for (ItemEntity other : entity.level().getEntitiesOfClass(ItemEntity.class, entity.getBoundingBox().inflate(CELL_SIZE))) {
            if (other == entity) {
                continue;
            }
            int otherCellX = (int)Math.floor(other.getX() / CELL_SIZE);
            int otherCellZ = (int)Math.floor(other.getZ() / CELL_SIZE);

            if (otherCellX == cellX && otherCellZ == cellZ) {
                if (other.getId() < entity.getId()) {
                    index++;
                }
            }
        }

        return Math.min(index, MAX_STACK_LAYERS);
    }

    @Override
    public ResourceLocation getTextureLocation(ItemEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
