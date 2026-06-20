package com.mjzaymi.etherealvoid.common.electricity;

public class ElectricalSpec {
    private final CurrentType currentType;
    private final WireRole role;
    private final double nominalVoltage; // 额定/设计电压等级（用于变压器匹配和电线烧毁判定）

    public ElectricalSpec(CurrentType currentType, WireRole role, double nominalVoltage) {
        this.currentType = currentType;
        this.role = role;
        this.nominalVoltage = nominalVoltage;
    }

    public CurrentType getCurrentType() { return currentType; }
    public WireRole getRole() { return role; }
    public double getNominalVoltage() { return nominalVoltage; }

    // 判定两个端子是否匹配（例如：同为交流、同为火线、电压等级相同才能相连）
    public boolean canConnectTo(ElectricalSpec other) {
        return this.currentType == other.currentType
                && this.role == other.role
                && this.nominalVoltage == other.nominalVoltage; // 变压器前后的网段电压不同，不能直连
    }
}