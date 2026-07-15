package com.mjzaymi.etherealvoid.client;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.client.model.ConnectedGlassModel;
import com.mjzaymi.etherealvoid.client.model.SmallRocketModel;
import com.mjzaymi.etherealvoid.client.model.VirtualMinerModel;
import com.mjzaymi.etherealvoid.client.renderer.ReactionPoolRenderer;
import com.mjzaymi.etherealvoid.client.renderer.SmallRocketRenderer;
import com.mjzaymi.etherealvoid.client.renderer.VirtualMinerBlockEntityRenderer;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import com.mjzaymi.etherealvoid.registration.ModBlocks;
import com.mjzaymi.etherealvoid.registration.ModEntities;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        modid = EtherealVoid.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Mike Chen is Jay
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.REACTION_POOL_BE.get(),
                ReactionPoolRenderer::new
        );

        event.registerBlockEntityRenderer(ModBlockEntities.VIRTUAL_MINER.get(), VirtualMinerBlockEntityRenderer::new);

        event.registerEntityRenderer(ModEntities.SMALL_ROCKET.get(), SmallRocketRenderer::new);
    }

    @SubscribeEvent
    public static void modifyBakedModels(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation modelLocation = BlockModelShaper.stateToModelLocation(
                ModBlocks.ANTI_CORROSION_GLASS.get().defaultBlockState()
        );
        BakedModel originalModel = event.getModels().get(modelLocation);

        if (originalModel != null && !(originalModel instanceof ConnectedGlassModel)) {
            event.getModels().put(modelLocation, new ConnectedGlassModel(originalModel));
        }
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 注册你在 Blockbench 里生成的骨骼模型层（通常导出的类里自带了一个静态的 createBodyLayer() 方法）
        event.registerLayerDefinition(VirtualMinerBlockEntityRenderer.LAYER_LOCATION, VirtualMinerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 注册你在 SmallRocketModel 中定义的 Layer
        event.registerLayerDefinition(SmallRocketModel.LAYER_LOCATION, SmallRocketModel::createBodyLayer);
    }
}
