package com.mjzaymi.etherealvoid.item;

import com.mjzaymi.etherealvoid.common.electricity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class Multimeter extends Item {

    public Multimeter() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        var pos = context.getClickedPos();
        var clickedFace = context.getClickedFace();

        if (level.isClientSide || player == null) {
            return InteractionResult.SUCCESS;
        }

        var be = level.getBlockEntity(pos);
        if (!(be instanceof IMultimeterDetectable detectable)) return displayPass(player);
        var spec = detectable.getSpec(clickedFace);
        if (spec==null) return displayPass(player);
        var voltage = spec.getVoltage();
        var current = spec.getCurrent();
        var currentType = spec.getCurrentType();

        var power = voltage * current;

        String typeStr = (currentType == CurrentType.AC) ? "AC" : "DC";

        // 拼装出极简、友好的单行数据反馈
        // 效果：[AC 设备] 电压: 220.0V | 电流: 5.00A | 功率: 1100.0W
        Component message = Component.literal(
                String.format("§b[%s 设备] §f电压: §e%.1fV §7| §f电流: §a%.2fA §7| §f功率: §c%.1fW",
                        typeStr, voltage, current, power)
        );

        player.displayClientMessage(message, true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult displayPass(Player player) {

        player.displayClientMessage(Component.literal("§c目标没有可检测的电气网络"), true);
        return InteractionResult.PASS;
    }
}