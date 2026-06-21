package com.mjzaymi.etherealvoid.common.electricity;

import net.minecraft.core.Direction;

public interface IMultimeterDetectable {
    ElectricalSpec getSpec(Direction direction);
}
