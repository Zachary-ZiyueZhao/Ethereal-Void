package com.mjzaymi.etherealvoid.common.electricity;

public interface IElectricalTerminal {
    ElectricalSpec getSpec(); // 获取该端子的电气规格
    double getPotential();    // 获取当前实际电势 (伏特)
    double getResistance();   // 获取内阻/负载
    // 【新增】获取当前流经该端子的实际电流 (A)。由全局电网管理器负责更新这个值。
    double getCurrent();

    // 以下两个方法由 NetworkManager (求解器) 在计算完成后回写
    void setNodePotential(double potential);
    void setCurrentFlow(double current);
}