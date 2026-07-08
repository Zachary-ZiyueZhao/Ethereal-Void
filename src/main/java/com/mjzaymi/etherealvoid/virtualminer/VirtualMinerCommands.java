package com.mjzaymi.etherealvoid.virtualminer;

import com.mojang.brigadier.CommandDispatcher;
import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EtherealVoid.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VirtualMinerCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // 注册指令结构：/ev_vein [check / scan]
        dispatcher.register(Commands.literal("ev_vein")
                .requires(source -> source.hasPermission(2)) // 需要OP管理员权限
                .then(Commands.literal("check").executes(context -> checkCurrentVein(context.getSource())))
                .then(Commands.literal("scan").executes(context -> scanNearbyVeins(context.getSource())))
        );
    }

    // 1. 检查当前区块
    private static int checkCurrentVein(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            BlockPos pos = player.blockPosition();
            VeinRecipe vein = VeinGenerator.getVeinForChunk(player.level(), pos);

            if (vein != null) {
                player.sendSystemMessage(Component.literal("§a[采矿机测试] 当前区块的矿脉是: §6" + vein.getId().toString()));
            } else {
                player.sendSystemMessage(Component.literal("§c[采矿机测试] 当前区块是空的，没有任何矿脉！"));
            }
        }
        return 1;
    }

    // 2. 扫描周围 11x11 的区块，寻找特定矿脉
    private static int scanNearbyVeins(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            BlockPos playerPos = player.blockPosition();
            ChunkPos centerChunk = new ChunkPos(playerPos);

            player.sendSystemMessage(Component.literal("§b[采矿机测试] 开始扫描附近 11x11 区块的虚拟矿脉..."));
            int foundCount = 0;

            // 循环扫描周围的区块
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    ChunkPos targetChunk = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                    // 取该区块的中心点坐标去计算
                    BlockPos targetPos = targetChunk.getMiddleBlockPosition(64);

                    VeinRecipe vein = VeinGenerator.getVeinForChunk(player.level(), targetPos);
                    if (vein != null) {
                        foundCount++;
                        // 打印出矿脉名字和它所在的坐标，方便你传送过去
                        player.sendSystemMessage(Component.literal("§7- 找到 §6" + vein.getId().getPath() + " §7位于坐标: §e" + targetPos.getX() + ", " + targetPos.getZ()));
                    }
                }
            }
            player.sendSystemMessage(Component.literal("§a[采矿机测试] 扫描完毕！共发现 " + foundCount + " 个矿脉。"));
        }
        return 1;
    }
}