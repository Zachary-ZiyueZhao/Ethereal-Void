package com.mjzaymi.etherealvoid.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class SmallRocketModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("ethereal_void", "small_rocket"), "main");
	private final ModelPart bone;
	private final ModelPart bb_main;

	public SmallRocketModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 57).addBox(-7.5F, -48.75F, -7.5F, 15.0F, 1.0F, 15.0F, new CubeDeformation(0.0F))
		.texOffs(116, 25).addBox(-5.0F, -52.5F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(116, 47).addBox(-4.5F, -53.25F, -4.5F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(0, 108).addBox(-4.0F, -54.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(96, 13).addBox(-5.5F, -51.75F, -5.5F, 11.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(124, 68).addBox(-2.0F, -57.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(96, 31).addBox(-1.5F, -57.75F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(108, 31).addBox(-1.0F, -58.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(28, 117).addBox(-0.5F, -69.25F, -0.5F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(96, 25).addBox(-2.5F, -56.25F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(92, 49).addBox(-3.0F, -55.5F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(64, 49).addBox(-3.5F, -54.75F, -3.5F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(96, 0).addBox(-6.0F, -51.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(64, 35).addBox(-6.5F, -50.25F, -6.5F, 13.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(60, 57).addBox(-7.0F, -49.5F, -7.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(32, 73).addBox(-12.0F, -23.0F, -5.0F, 4.0F, 23.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-8.0F, -48.0F, -8.0F, 16.0F, 41.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(116, 36).addBox(-5.0F, -7.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(0, 117).addBox(-10.0F, -25.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(116, 118).addBox(-10.0F, -7.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

		PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(92, 72).addBox(-10.0F, -6.0F, -6.0F, 4.0F, 23.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -18.0F, 0.0F, 0.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 73).addBox(-13.0F, -6.0F, -6.0F, 4.0F, 23.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.0F, -18.0F, 0.0F, 0.0F, 0.0F, 0.0F));

		PartDefinition cube_r4 = bb_main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(60, 72).addBox(-13.0F, -6.0F, -6.0F, 4.0F, 23.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.0F, -20.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r5 = bb_main.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(64, 0).addBox(-10.0F, -6.0F, -6.0F, 4.0F, 23.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.0F, 1.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r6 = bb_main.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(116, 107).addBox(-10.0F, -7.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r7 = bb_main.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(116, 57).addBox(-10.0F, -7.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(32, 106).addBox(-12.0F, -5.0F, -5.0F, 4.0F, 23.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r8 = bb_main.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(88, 107).addBox(-12.0F, -7.0F, -5.0F, 4.0F, 23.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

		PartDefinition cube_r9 = bb_main.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(60, 107).addBox(-12.0F, -7.0F, -5.0F, 4.0F, 23.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}