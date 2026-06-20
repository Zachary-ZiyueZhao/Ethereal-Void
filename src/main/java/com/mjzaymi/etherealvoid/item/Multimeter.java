package com.mjzaymi.etherealvoid.item;

import com.mjzaymi.etherealvoid.block.entity.electricity.HydraulicGeneratorBlockEntity;
import com.mjzaymi.etherealvoid.common.electricity.CurrentType;
import com.mjzaymi.etherealvoid.common.electricity.ElectricalSpec;
import com.mjzaymi.etherealvoid.common.electricity.IElectricalTerminal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Multimeter extends Item {

    public Multimeter() {
        super(new Item.Properties()
                .stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        // 仅在服务端进行逻辑运算，避免客户端和服务端数据不同步
        if (level.isClientSide || player == null) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            // 这里假设你用的是自定义接口直接强转（如果你用的是 Forge Capability，请替换为 getCapability 逻辑）
            IElectricalTerminal terminal = null;

            // 比如我们之前写的水力发电机，通过 getTerminal(点击的面) 来获取接口
            if (be instanceof HydraulicGeneratorBlockEntity generator) {
                terminal = generator.getTerminal(clickedFace);
            }
            // 这里可以继续写 else if 判断电线、变压器等其他设备...

            if (terminal != null) {
                // 1. 获取规格信息
                ElectricalSpec spec = terminal.getSpec();
                String typeStr = spec.getCurrentType() == CurrentType.AC ? "交流(AC)" : "直流(DC)";
                String roleStr = spec.getRole().name(); // 比如 LIVE, NEUTRAL

                // 2. 获取实时物理数据
                double voltage = terminal.getPotential();
                double resistance = terminal.getResistance();
                double current = terminal.getCurrent();

                // 3. 计算功率 (P = U * I)
                double power = voltage * current;

                // 4. 拼装成友好的文本信息
                // 格式：[交流 LIVE] 220.0V | 5.0A | 1100.0W | 0.5Ω
                Component message = Component.literal(
                        String.format("[%s %s] %.1fV | %.2fA | %.1fW | %.2fΩ",
                                typeStr, roleStr, voltage, current, power, resistance)
                );

                // 5. 发送到 Action Bar（屏幕正下方，快捷栏上方），第二个参数 true 代表是 Action Bar
                player.displayClientMessage(message, true);

                return InteractionResult.SUCCESS;
            }
        }

        // 如果点的方块没有电气接口，提示玩家
        player.displayClientMessage(Component.literal("§c该面上没有可检测的电气端子"), true);
        return InteractionResult.PASS;
    }
}