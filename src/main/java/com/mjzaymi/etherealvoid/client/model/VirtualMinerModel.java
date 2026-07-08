package com.mjzaymi.etherealvoid.client.model;
// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class VirtualMinerModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("ethereal_void", "virtual_miner"), "main");
	private final ModelPart group;
	private final ModelPart group2;
	private final ModelPart deco;
	private final ModelPart tube;
	private final ModelPart j1;
	private final ModelPart j2;
	private final ModelPart j3;
	private final ModelPart board;
	private final ModelPart boards;
	private final ModelPart square;
	private final ModelPart wires;
	private final ModelPart wires2;
	private final ModelPart wires4;
	private final ModelPart wires3;
	private final ModelPart screen;
	private final ModelPart io;
	private final ModelPart e;
	private final ModelPart fluid;
	private final ModelPart ore;
	private final ModelPart tool;
	private final ModelPart rotation;
	private final ModelPart rotation2;
	private final ModelPart connections;
	private final ModelPart a;
	private final ModelPart b;
	private final ModelPart bb_main;

	public VirtualMinerModel(ModelPart root) {
		this.group = root.getChild("group");
		this.group2 = root.getChild("group2");
		this.deco = root.getChild("deco");
		this.tube = this.deco.getChild("tube");
		this.j1 = this.tube.getChild("1");
		this.j2 = this.tube.getChild("2");
		this.j3 = this.tube.getChild("3");
		this.board = this.deco.getChild("board");
		this.boards = this.deco.getChild("boards");
		this.square = this.deco.getChild("square");
		this.wires = this.deco.getChild("wires");
		this.wires2 = this.deco.getChild("wires2");
		this.wires4 = this.deco.getChild("wires4");
		this.wires3 = this.deco.getChild("wires3");
		this.screen = root.getChild("screen");
		this.io = root.getChild("io");
		this.e = this.io.getChild("e");
		this.fluid = this.io.getChild("fluid");
		this.ore = this.io.getChild("ore");
		this.tool = this.io.getChild("tool");
		this.rotation = root.getChild("rotation");
		this.rotation2 = root.getChild("rotation2");
		this.connections = root.getChild("connections");
		this.a = this.connections.getChild("a");
		this.b = this.connections.getChild("b");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition group = partdefinition.addOrReplaceChild("group", CubeListBuilder.create().texOffs(36, 191).addBox(-23.0F, -1.0F, 15.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(182, 199).addBox(-23.0F, -1.0F, -23.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(126, 181).addBox(-22.5F, -25.0F, -22.5F, 7.0F, 24.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(154, 181).addBox(-22.5F, -25.0F, 15.5F, 7.0F, 24.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(0, 103).addBox(-23.5F, -32.0F, -23.5F, 9.0F, 7.0F, 47.0F, new CubeDeformation(0.0F))
		.texOffs(136, 38).addBox(-21.0F, -23.0F, -15.5F, 4.0F, 3.0F, 31.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition group2 = partdefinition.addOrReplaceChild("group2", CubeListBuilder.create().texOffs(176, 162).addBox(15.0F, -1.0F, 15.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(194, 171).addBox(15.0F, -1.0F, -23.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(70, 181).addBox(15.5F, -25.0F, -22.5F, 7.0F, 24.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(98, 181).addBox(15.5F, -25.0F, 15.5F, 7.0F, 24.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(112, 103).addBox(14.5F, -32.0F, -23.5F, 9.0F, 7.0F, 47.0F, new CubeDeformation(0.0F))
		.texOffs(0, 157).addBox(17.0F, -23.0F, -15.5F, 4.0F, 3.0F, 31.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition deco = partdefinition.addOrReplaceChild("deco", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition tube = deco.addOrReplaceChild("tube", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition j1 = tube.addOrReplaceChild("1", CubeListBuilder.create().texOffs(118, 172).addBox(-14.5F, -31.0F, -16.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(214, 210).addBox(-12.5F, -31.0F, -13.5F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(214, 207).addBox(-14.5F, -31.0F, -13.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(156, 216).addBox(-12.5F, -31.0F, -10.5F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(214, 215).addBox(-14.5F, -31.0F, -10.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(54, 213).addBox(-12.5F, -31.0F, -16.5F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition j2 = tube.addOrReplaceChild("2", CubeListBuilder.create().texOffs(198, 96).addBox(-10.5F, -31.0F, 11.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(70, 174).addBox(-14.5F, -31.0F, 11.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 217).addBox(-10.5F, -31.0F, 9.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(80, 174).addBox(-14.5F, -31.0F, 9.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 217).addBox(-10.5F, -31.0F, 13.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(90, 174).addBox(-14.5F, -31.0F, 13.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 217).addBox(-10.5F, -31.0F, 7.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(214, 203).addBox(-14.5F, -31.0F, 7.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 217).addBox(-10.5F, -31.0F, 15.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(214, 205).addBox(-14.5F, -31.0F, 15.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition j3 = tube.addOrReplaceChild("3", CubeListBuilder.create().texOffs(0, 209).addBox(12.5F, -31.0F, -6.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(208, 162).addBox(9.5F, -30.0F, -6.5F, 4.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(18, 209).addBox(9.5F, -29.0F, -6.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(62, 259).addBox(10.5F, -30.5F, -7.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition board = deco.addOrReplaceChild("board", CubeListBuilder.create().texOffs(208, 79).addBox(-10.0F, -29.0F, -15.5F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(100, 174).addBox(-4.0F, -28.5F, -14.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(198, 100).addBox(-4.0F, -28.5F, -11.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 218).addBox(-11.0F, -28.5F, -14.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 217).addBox(-11.0F, -28.5F, -11.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 210).addBox(-6.0F, -28.5F, -9.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(62, 210).addBox(-9.0F, -28.5F, -9.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 218).addBox(-6.0F, -28.5F, -16.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 218).addBox(-9.0F, -28.5F, -16.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 0.0F, 1.0F));

		PartDefinition boards = deco.addOrReplaceChild("boards", CubeListBuilder.create().texOffs(106, 172).addBox(4.0F, -28.5F, -15.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(168, 212).addBox(8.0F, -28.5F, -11.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(156, 212).addBox(4.0F, -28.5F, -11.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(208, 86).addBox(8.0F, -28.5F, -15.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(176, 171).addBox(-3.0F, -28.5F, -5.5F, 5.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(214, 199).addBox(-6.0F, -28.5F, 0.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(162, 216).addBox(-10.0F, -28.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(206, 58).addBox(-7.0F, -28.5F, 6.5F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition square = deco.addOrReplaceChild("square", CubeListBuilder.create().texOffs(202, 91).addBox(5.0F, -28.5F, -4.5F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(206, 38).addBox(-4.0F, -28.5F, -4.5F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(70, 172).addBox(-3.0F, -28.5F, -4.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(88, 172).addBox(-3.0F, -28.5F, 5.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(206, 50).addBox(-2.0F, -28.5F, -2.5F, 6.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0F, 7.0F));

		PartDefinition wires = deco.addOrReplaceChild("wires", CubeListBuilder.create().texOffs(182, 208).addBox(15.5F, -18.0F, -12.0F, 1.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(78, 212).addBox(14.5F, -18.0F, -12.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(78, 212).addBox(14.5F, -11.0F, -12.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -1.5F));

		PartDefinition wires2 = deco.addOrReplaceChild("wires2", CubeListBuilder.create().texOffs(126, 212).addBox(15.5F, -18.0F, -12.0F, 1.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(34, 213).addBox(14.5F, -11.0F, -12.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(44, 213).addBox(14.5F, -18.0F, -12.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 21.5F));

		PartDefinition wires4 = deco.addOrReplaceChild("wires4", CubeListBuilder.create().texOffs(62, 200).addBox(15.5F, -18.0F, -11.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 217).addBox(14.5F, -11.0F, -11.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(6, 217).addBox(14.5F, -18.0F, -11.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 17.5F));

		PartDefinition wires3 = deco.addOrReplaceChild("wires3", CubeListBuilder.create().texOffs(198, 208).addBox(15.5F, -18.0F, -12.0F, 1.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(110, 212).addBox(14.5F, -18.0F, -12.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(110, 212).addBox(14.5F, -11.0F, -12.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition screen = partdefinition.addOrReplaceChild("screen", CubeListBuilder.create().texOffs(126, 157).addBox(-11.0F, -22.5F, -21.0F, 22.0F, 15.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(182, 265).addBox(11.0F, -7.5F, -20.0F, -22.0F, -15.0F, -1.0F, new CubeDeformation(-0.3F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition io = partdefinition.addOrReplaceChild("io", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition e = io.addOrReplaceChild("e", CubeListBuilder.create().texOffs(208, 68).addBox(-3.0F, -27.0F, 19.0F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(136, 212).addBox(5.0F, -26.0F, 19.0F, 1.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(146, 212).addBox(-6.0F, -26.0F, 19.0F, 1.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(170, 216).addBox(-5.0F, -26.0F, 22.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(176, 216).addBox(3.0F, -26.0F, 22.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition fluid = io.addOrReplaceChild("fluid", CubeListBuilder.create().texOffs(36, 200).addBox(-4.0F, -12.0F, 19.0F, 8.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(136, 91).addBox(-15.5F, -9.5F, 20.0F, 31.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ore = io.addOrReplaceChild("ore", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = ore.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(182, 181).addBox(-4.0F, -12.0F, -5.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition tool = io.addOrReplaceChild("tool", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r2 = tool.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 191).addBox(-4.0F, -12.0F, -5.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition rotation = partdefinition.addOrReplaceChild("rotation", CubeListBuilder.create().texOffs(136, 72).addBox(-9.0F, -3.0F, -9.0F, 18.0F, 1.0F, 18.0F, new CubeDeformation(1.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition rotation2 = partdefinition.addOrReplaceChild("rotation2", CubeListBuilder.create().texOffs(70, 157).addBox(-7.0F, -1.0F, -7.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition connections = partdefinition.addOrReplaceChild("connections", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition a = connections.addOrReplaceChild("a", CubeListBuilder.create().texOffs(136, 96).addBox(-14.5F, -31.0F, -22.5F, 29.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = a.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(70, 176).addBox(-3.0F, -5.0F, -1.0F, 29.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.5F, -30.0F, -22.5F, -1.5708F, 0.0F, 0.0F));

		PartDefinition b = connections.addOrReplaceChild("b", CubeListBuilder.create().texOffs(132, 176).addBox(-14.5F, -31.0F, 20.5F, 29.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r4 = b.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(176, 157).addBox(-3.0F, -5.0F, -1.0F, 29.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.5F, -30.0F, 15.5F, -1.5708F, 0.0F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-14.5F, -28.0F, -19.5F, 29.0F, 14.0F, 39.0F, new CubeDeformation(0.0F))
		.texOffs(0, 53).addBox(-14.5F, -13.0F, -19.5F, 29.0F, 11.0F, 39.0F, new CubeDeformation(0.0F))
		.texOffs(136, 0).addBox(-13.5F, -14.0F, -18.5F, 27.0F, 1.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cover_r1 = bb_main.addOrReplaceChild("cover_r1", CubeListBuilder.create().texOffs(0, 221).addBox(-3.0F, -46.0F, 0.0F, 29.0F, 47.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.5F, -32.0F, -22.5F, -1.5708F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		group.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		group2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		deco.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		screen.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		io.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		rotation.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		rotation2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		connections.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}