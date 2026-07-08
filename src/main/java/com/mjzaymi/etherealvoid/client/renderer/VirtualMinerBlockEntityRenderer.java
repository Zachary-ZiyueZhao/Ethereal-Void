package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.block.VirtualMinerBlock;
import com.mjzaymi.etherealvoid.client.model.VirtualMinerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.blockentity.VirtualMinerBlockEntity;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class VirtualMinerBlockEntityRenderer implements BlockEntityRenderer<VirtualMinerBlockEntity> {
    // 注册模型层的标识符
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "virtual_miner"), "main");

    private final VirtualMinerModel<Entity> model;
    private final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "textures/entity/virtual_miner.png");

    public VirtualMinerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // 利用 Context 烘焙出 Blockbench 模型内部的 ModelPart
        this.model = new VirtualMinerModel<>(context.bakeLayer(LAYER_LOCATION));
    }

    @Override
    public void render(VirtualMinerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 获取当前方块的 BlockState
        BlockState blockState = blockEntity.getBlockState();
        Direction facing = blockState.hasProperty(VirtualMinerBlock.FACING) ? blockState.getValue(VirtualMinerBlock.FACING) : Direction.NORTH;

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        poseStack.scale(1.0F, -1.0F, -1.0F);

        float ageInTicks = (float) blockEntity.getLevel().getGameTime() + partialTick;
        this.model.setupAnim(null, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}