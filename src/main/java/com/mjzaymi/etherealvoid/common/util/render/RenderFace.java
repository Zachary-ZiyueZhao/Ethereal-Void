package com.mjzaymi.etherealvoid.common.util.render;

public class RenderFace {
    public static final RenderFace ALL = new RenderFace(true, true, true, true, true, true);
    public static final RenderFace NO_TOP = new RenderFace(true, true, true, true, false, true);
    public static final RenderFace NO_BOTTOM = new RenderFace(true, true, true, true, true, false);
    public final boolean NORTH;
    public final boolean SOUTH;
    public final boolean EAST;
    public final boolean WEST;
    public final boolean TOP;
    public final boolean BOTTOM;
    public RenderFace(boolean ...booleans) {
        if (booleans.length<6) {
            NORTH = SOUTH = EAST = WEST = TOP = BOTTOM = false;
            return;
        }
        NORTH = booleans[0];
        SOUTH = booleans[1];
        EAST = booleans[2];
        WEST = booleans[3];
        TOP = booleans[4];
        BOTTOM = booleans[5];
    }
}
