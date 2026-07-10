package com.mjzaymi.etherealvoid.dimensions.space;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSpaceFogHandler {

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        double playerY = player.getY();

        // 🌟 核心：当玩家身上带有盲目效果，并且高度处于临界区时，强制把黑色的盲目雾漂白成“云海白”
        if (player.hasEffect(MobEffects.BLINDNESS)) {
            if (playerY >= 280.0 || playerY <= -10.0) {
                // 设置 RGB 为 1.0, 1.0, 1.0（纯白色浓雾）
                event.setRed(1.0F);
                event.setGreen(1.0F);
                event.setBlue(1.0F);
            }
        }
    }
}