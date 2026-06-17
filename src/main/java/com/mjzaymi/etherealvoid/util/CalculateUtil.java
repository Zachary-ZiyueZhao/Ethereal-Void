package com.mjzaymi.etherealvoid.util;

public class CalculateUtil {
    public static float getPreferredTotalPressure(float current) {
        if (current <= 10f) {
            return 10f;
        }
        return (float) Math.pow(10, Math.ceil(Math.log10(current)));
    }

    public static float getPreferredTotalTemperature(float current) {
        if (current <= 1000f) {
            return 1000f;
        }
        return (float) Math.pow(10, Math.ceil(Math.log10(current)));
    }
}
