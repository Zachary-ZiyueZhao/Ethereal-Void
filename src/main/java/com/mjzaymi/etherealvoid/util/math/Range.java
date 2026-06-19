package com.mjzaymi.etherealvoid.util.math;

public class Range {
    public final Number min;
    public final Number max;

    public Range(Number min, Number max) {
        this.min = min;
        this.max = max;
    }

    public boolean match(Number value) {
        double v = value.doubleValue();
        return v >= min.doubleValue()
                && v <= max.doubleValue();
    }
}