package com.mjzaymi.etherealvoid.block;

import net.minecraft.util.StringRepresentable;

public enum FluidIOMode implements StringRepresentable {
    INPUT("input"),
    OUTPUT("output");

    private final String name;

    FluidIOMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}