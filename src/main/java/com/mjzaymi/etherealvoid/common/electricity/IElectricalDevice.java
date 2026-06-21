package com.mjzaymi.etherealvoid.common.electricity;

public interface IElectricalDevice {
    ElectricalSpec getSpec();
    void onBreakdown();
}