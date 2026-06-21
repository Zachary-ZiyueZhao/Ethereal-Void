package com.mjzaymi.etherealvoid.common.electricity;

import com.mjzaymi.etherealvoid.common.util.math.Range;

public class ElectricalSpec {
    private final CurrentType currentType;
    private final float voltage;
    private final Range allowedVoltage;
    private final float resistant;
    private final float current;

    public ElectricalSpec(final CurrentType currentType, final float voltage, final Range allowedVoltage) {
        this(currentType, voltage, allowedVoltage, 0);
    }

    public ElectricalSpec(final CurrentType currentType, final float voltage, final Range allowedVoltage, final float resistant) {
        this(currentType, voltage, allowedVoltage, resistant, 0);
    }

    public ElectricalSpec(final CurrentType currentType, final float voltage, final Range allowedVoltage, final float resistant, float current) {
        this.currentType = currentType;
        this.voltage = voltage;
        this.resistant = resistant;
        this.allowedVoltage = allowedVoltage;
        this.current = current;
    }

    public ElectricalSpec voltage(float voltage) {
        return new ElectricalSpec(currentType, voltage, allowedVoltage, resistant, current);
    }

    public ElectricalSpec current(float current) {
        return new ElectricalSpec(currentType, voltage, allowedVoltage, resistant, current);
    }

    public CurrentType getCurrentType() { return currentType; }
    public float getVoltage() { return voltage; }
    public float getResistant() { return resistant; }
    public Range getAllowedVoltage() { return allowedVoltage; }
    public float getCurrent() { return current; }

    // 判定两个端子是否匹配（例如：同为交流、同为火线、电压等级相同才能相连）
    public boolean canConnectTo(ElectricalSpec other) {
        return this.currentType == other.currentType
                && this.voltage == other.voltage; // 变压器前后的网段电压不同，不能直连
    }
}