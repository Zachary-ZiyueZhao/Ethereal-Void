package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.client.model.SmallRocketModel;
import com.mjzaymi.etherealvoid.entity.SmallRocketEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SmallRocketRenderer extends EntityRenderer<SmallRocketEntity> {
    // 你的小火箭贴图路径：src/main/resources/assets/ethereal_void/textures/entity/small_rocket.png
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ethereal_void", "textures/block/small_rocket.png");
    private final SmallRocketModel<SmallRocketEntity> model;

    public SmallRocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 烘焙并绑定你之前写的模型 Layer Definition
        this.model = new SmallRocketModel<>(context.bakeLayer(SmallRocketModel.LAYER_LOCATION));
    }

    @Override
    public void render(SmallRocketEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 1. 根据火箭的偏航角（Yaw）旋转模型，让模型的朝向和实体本身保持一致
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getViewYRot(partialTicks)));

        // 2. 将模型翻转摆正（MC模型的Y轴在代码里默认是朝下的，需要绕X轴转180度摆正）
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        // 3. 略微向下平移，避免火箭浮空或嵌入地面（-1.5F 只是估值，你可以根据实际效果微调）
        poseStack.translate(0.0D, -1.5D, 0.0D);

        // 4. 渲染模型
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SmallRocketEntity entity) {
        return TEXTURE;
    }
}