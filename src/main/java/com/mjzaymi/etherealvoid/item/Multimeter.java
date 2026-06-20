package com.mjzaymi.etherealvoid.item;

import com.mjzaymi.etherealvoid.block.entity.electricity.HydraulicGeneratorBlockEntity;
import com.mjzaymi.etherealvoid.common.electricity.CurrentType;
import com.mjzaymi.etherealvoid.common.electricity.IElectricalTerminal;
import com.mjzaymi.etherealvoid.common.electricity.WireRole;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class Multimeter extends Item {

    public Multimeter() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        if (level.isClientSide || player == null) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            List<IElectricalTerminal> terminals = null;

            if (be instanceof HydraulicGeneratorBlockEntity generator) {
                terminals = generator.getTerminals(clickedFace);
            }
            // 后续添加变压器、电线等...
            // else if (be instanceof WireBlockEntity wire) { ... }

            if (terminals != null && !terminals.isEmpty()) {
                // 用于存储解析后的聚合数据
                double vLiveOrPositive = 0.0;
                double vNeutralOrNegative = 0.0;
                double current = 0.0;
                CurrentType type = CurrentType.AC; // 默认给个类型

                // 遍历所有引脚，提取高低电势和电流
                for (IElectricalTerminal terminal : terminals) {
                    WireRole role = terminal.getSpec().getRole();
                    type = terminal.getSpec().getCurrentType();

                    // 如果是火线或正极，记录其电势，并以这根线上的电流作为整体工作电流
                    if (role == WireRole.LIVE || role == WireRole.POSITIVE) {
                        vLiveOrPositive = terminal.getPotential();
                        current = terminal.getCurrent();
                    }
                    // 如果是零线或负极，记录其电势作为参考基准
                    else if (role == WireRole.NEUTRAL || role == WireRole.NEGATIVE) {
                        vNeutralOrNegative = terminal.getPotential();
                    }
                }

                // 核心逻辑：计算电势差 (电压) = |高电势 - 低电势|
                double voltageDiff = Math.abs(vLiveOrPositive - vNeutralOrNegative);
                // 功率 = 电势差 * 实际电流
                double power = voltageDiff * current;

                String typeStr = (type == CurrentType.AC) ? "AC" : "DC";

                // 拼装出极简、友好的单行数据反馈
                // 效果：[AC 设备] 电压: 220.0V | 电流: 5.00A | 功率: 1100.0W
                Component message = Component.literal(
                        String.format("§b[%s 设备] §f电压: §e%.1fV §7| §f电流: §a%.2fA §7| §f功率: §c%.1fW",
                                typeStr, voltageDiff, current, power)
                );

                player.displayClientMessage(message, true);
                return InteractionResult.SUCCESS;
            }
        }

        player.displayClientMessage(Component.literal("§c目标没有可检测的电气网络"), true);
        return InteractionResult.PASS;
    }
}