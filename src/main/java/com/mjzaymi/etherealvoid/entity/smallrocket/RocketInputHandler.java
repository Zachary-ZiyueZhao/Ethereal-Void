package com.mjzaymi.etherealvoid.entity.smallrocket;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RocketInputHandler {

    private static Field jumpingField = null;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        // 🌟 核心修正：只在每刻结束、且严格在【服务端】进行逻辑判定
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
            return;
        }

        // 当玩家正在骑乘我们的火箭时
        if (player.isPassenger() && player.getVehicle() instanceof SmallRocketEntity rocket) {
            // 只有在火箭还没点火（阶段 0）时，才检测空格键
            if (rocket.getLaunchStage() == 0) {
                try {
                    // 初始化反射字段（1.21 运行期完全使用官方 Mojmap，字段名固定为 "jumping"）
                    if (jumpingField == null) {
                        jumpingField = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("jumping");
                        jumpingField.setAccessible(true);
                    }

                    // 直接从服务端读取该玩家当前是否正按着跳跃键（空格）
                    boolean isJumping = jumpingField.getBoolean(player);

                    if (isJumping) {
                        // 🚀 服务端直接触发点火！服务端改变状态后会自动通过 DataSync 同步给客户端
                        rocket.startCountdown();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}