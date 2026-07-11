package com.mjzaymi.etherealvoid.dimensions.space;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ethereal_void", value = Dist.CLIENT)
public class SpaceClientEvents {

    private static final ResourceKey<Level> SPACE_DIMENSION = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "low_earth_orbit"));

    // 用于缓存玩家原版的视角摇晃设置，离开太空时恢复
    private static boolean originalBobView = true;
    private static boolean hasStoredPreference = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (mc.level.dimension().equals(SPACE_DIMENSION)) {
            // 进入太空无晃动
            if (!hasStoredPreference) {
                originalBobView = mc.options.bobView().get();
                hasStoredPreference = true;
            }
            if (mc.options.bobView().get()) {
                mc.options.bobView().set(false);
            }
        } else {
            // 离开太空，恢复玩家原本设置
            if (hasStoredPreference) {
                mc.options.bobView().set(originalBobView);
                hasStoredPreference = false;
            }
        }
    }
}