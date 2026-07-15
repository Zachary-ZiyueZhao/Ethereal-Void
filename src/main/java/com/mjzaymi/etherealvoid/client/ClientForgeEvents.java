package com.mjzaymi.etherealvoid.client;

import com.mjzaymi.etherealvoid.entity.SmallRocketEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 注意这里的 bus 改为了 Bus.FORGE
@Mod.EventBusSubscriber(modid = "ethereal_void", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (event.getLevel().isClientSide()) {
            if (event.getEntityBeingMounted() instanceof SmallRocketEntity && event.getEntityMounting() instanceof Player player) {
                Minecraft mc = Minecraft.getInstance();

                if (player.equals(mc.player)) {
                    if (event.isMounting()) {
                        // 玩家骑上火箭：自动切到第三人称背面视角
                        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                    } else if (event.isDismounting()) {
                        // 玩家离开火箭：自动切回第一人称
                        mc.options.setCameraType(CameraType.FIRST_PERSON);
                    }
                }
            }
        }
    }
}