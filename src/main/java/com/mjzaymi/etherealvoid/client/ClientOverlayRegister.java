package com.mjzaymi.etherealvoid.client;

import com.mjzaymi.etherealvoid.EtherealVoid;
import com.mjzaymi.etherealvoid.dimensions.space.ClientSpaceOverlayHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientOverlayRegister {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        // 将我们的空间转场遮罩注册到屏幕渲染管线中
        // 这里的 VanillaGuiOverlay.VIGNETTE 代表将其渲染在原版画面（如南瓜头、黑边）附近
        event.registerAbove(VanillaGuiOverlay.VIGNETTE.id(), "space_transition_mask", ClientSpaceOverlayHandler.INSTANCE);
    }
}