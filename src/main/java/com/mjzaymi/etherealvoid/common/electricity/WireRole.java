package com.mjzaymi.etherealvoid.common.electricity;

// 导线角色/极性（包揽现实中所有的线路种类）
public enum WireRole {
    // === 直流专属 (DC) ===
    POSITIVE, // 正极 (+)
    NEGATIVE, // 负极 (-)

    // === 交流专属 (AC) ===
    LIVE,     // 单相火线 (L)
    NEUTRAL,  // 零线 (N)

    // === 三相交流扩展 (3-Phase AC) ===
    PHASE_A,  // A相 (L1)
    PHASE_B,  // B相 (L2)
    PHASE_C,  // C相 (L3)

    // === 通用 ===
    GROUND    // 地线/保护线 (PE/E)
}