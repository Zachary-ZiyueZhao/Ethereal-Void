package com.mjzaymi.etherealvoid.common.electricity;

import net.minecraft.core.Direction;

import java.util.List;

// 3. 设备接口：让 BlockEntity 实现它
public interface IElectricalDevice {
    // 获取该设备在指定面上的所有端子
    List<IElectricalTerminal> getTerminals(Direction side);

    // 当设备被高压击穿、过流烧毁时调用
    void onBreakdown();
}