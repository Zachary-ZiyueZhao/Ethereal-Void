package com.mjzaymi.etherealvoid.client;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModBusRegister {

    @SubscribeEvent
    public static void registerEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "low_earth_orbit"),
                // 改回 NORMAL，解封原版的动态雾气距离控制
                // 将第二个参数（是否有地面雾/hasGroundFog）保持为 false
                new DimensionSpecialEffects(Float.NaN, false, DimensionSpecialEffects.SkyType.NORMAL, false, false) {
                    @Override
                    public Vec3 getBrightnessDependentFogColor(Vec3 scale, float partialTick) {
                        return Vec3.ZERO;
                    }

                    @Override
                    public boolean isFoggyAt(int x, int z) {
                        return false;
                    }
                }
        );
    }
}