package com.mjzaymi.etherealvoid.client;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.client.model.ConnectedGlassModel;
import com.mjzaymi.etherealvoid.client.renderer.LyingItemRenderer;
import com.mjzaymi.etherealvoid.registrations.ModBlocks;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EntityType;
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

        event.enqueueWork(() -> {

            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.ANTI_CORROSION_GLASS.get(),
                    RenderType.translucent()
            );

        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.ITEM, LyingItemRenderer::new);
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
}
