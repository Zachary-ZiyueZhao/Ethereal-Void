package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.client.model.VirtualMinerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.blockentity.VirtualMinerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class VirtualMinerBlockEntityRenderer implements BlockEntityRenderer<VirtualMinerBlockEntity> {
    // 注册模型层的标识符
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "virtual_miner"), "main");

    private final VirtualMinerModel<Entity> model;
    private final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "textures/entity/virtual_miner.png");

    public VirtualMinerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // 利用 Context 烘焙出你 Blockbench 模型内部的 ModelPart
        this.model = new VirtualMinerModel<>(context.bakeLayer(LAYER_LOCATION));
    }

    @Override
    public void render(VirtualMinerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // 1. 将中心移到方块的水平中心 (0.5, 0.5)
        // 2. 核心修改：调整 Y 轴的位移。这里先设为 1.5D（向上提 1.5 格），如果进游戏发现高了或低了，微调这个数值即可
        poseStack.translate(0.5D, 1.5D, 0.5D);

        // 3. 翻转 Y 和 Z 轴，使 EntityModel 保持正立
        poseStack.scale(1.0F, -1.0F, -1.0F);

        // 计算动画时间刻
        float ageInTicks = (float) blockEntity.getLevel().getGameTime() + partialTick;

        // 触发动画逻辑
        this.model.setupAnim(null, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        // 获取材质并渲染
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}