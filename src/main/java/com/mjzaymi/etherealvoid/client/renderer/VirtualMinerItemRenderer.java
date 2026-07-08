package com.mjzaymi.etherealvoid.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.client.model.VirtualMinerModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class VirtualMinerItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final VirtualMinerItemRenderer INSTANCE = new VirtualMinerItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels()
    );

    private final VirtualMinerModel<Entity> model;
    private final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "textures/entity/virtual_miner.png");

    public VirtualMinerItemRenderer(BlockEntityRenderDispatcher rd, EntityModelSet modelSet) {
        super(rd, modelSet);
        this.model = new VirtualMinerModel<>(modelSet.bakeLayer(VirtualMinerBlockEntityRenderer.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        if (context == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.3D, 0.5D);
            poseStack.scale(0.25F, -0.25F, -0.25F);

        } else if (context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {

            // 如果是右手拿，往左横移；左手拿，往右横移。从而居中对齐到玩家脑袋正上方
            float sideOffset = (context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) ? 0.2F : 0F;
            poseStack.translate(sideOffset, 0.6D, 0.2D);

            // 修正旋转
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            // 缩放比例
            poseStack.scale(0.7F, -0.7F, -0.7F);

        } else if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            poseStack.translate(0.5D, 0.4D, 0.5D);
            poseStack.scale(0.35F, -0.35F, -0.35F);

        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.scale(0.3F, -0.3F, -0.3F);
        }

        this.model.setupAnim(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}