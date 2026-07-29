package com.mjzaymi.etherealvoid.multiblock.hugeelectrodeplate;

import com.mjzaymi.etherealvoid.block.ElectrodePlate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = "ethereal_void")
public class HugeElectrodePlateManager {

    private static final Map<ServerLevel, Set<BlockPos>> DIRTY_CENTERS = new HashMap<>();

    public static void markBlockChanged(ServerLevel level, BlockPos changedPos) {
        DIRTY_CENTERS.computeIfAbsent(level, k -> new HashSet<>());
        Set<BlockPos> set = DIRTY_CENTERS.get(level);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    set.add(changedPos.offset(dx, dy, dz));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTickEnd(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }

        Set<BlockPos> dirty = DIRTY_CENTERS.remove(level);
        if (dirty == null || dirty.isEmpty()) return;

        HugeElectrodePlateSavedData data = HugeElectrodePlateSavedData.get(level);
        Map<BlockPos, HugeElectrodePlateSavedData.StructureState> tracked = data.getTrackedStructures();

        for (BlockPos center : dirty) {
            HugeElectrodePlateSavedData.StructureState oldState = tracked.getOrDefault(center, HugeElectrodePlateSavedData.StructureState.NONE);

            if (oldState == HugeElectrodePlateSavedData.StructureState.NONE && !(level.getBlockState(center).getBlock() instanceof ElectrodePlate)) {
                continue;
            }

            HugeElectrodePlateSavedData.StructureState newState = evaluateActualState(level, center);

            if (oldState == newState) continue;

            // 核心状态机转移逻辑与消息通知
            String message = null;
            String xyzStr = center.getX() + ", " + center.getY() + ", " + center.getZ();
            BlockPos origin = center.offset(-1, -1, -1); // 3x3x3的起点

            if (newState == HugeElectrodePlateSavedData.StructureState.FORMED) {
                message = "§a[Ethereal Void] 结构成型！中心位于 " + xyzStr;
                tracked.put(center, newState);
                data.setDirty();

                // 【新增：成功时替换模型】
                // 将中心点设置为 CENTER，其余 26 个点设置为 INVISIBLE
                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        for (int z = 0; z < 3; z++) {
                            BlockPos currentPos = origin.offset(x, y, z);
                            BlockState currentState = level.getBlockState(currentPos);
                            if (currentState.getBlock() instanceof ElectrodePlate) {
                                if (currentPos.equals(center)) {
                                    level.setBlock(currentPos, currentState.setValue(ElectrodePlate.RENDER_KIND, ElectrodePlate.RenderKind.CENTER), 3);
                                } else {
                                    level.setBlock(currentPos, currentState.setValue(ElectrodePlate.RENDER_KIND, ElectrodePlate.RenderKind.INVISIBLE), 3);
                                }
                            }
                        }
                    }
                }
            }
            else {
                // 进入任何失败分支（TANK_CONFLICT, INTERFERED, NONE），只要原来是成型的，或者现在彻底碎了
                // 都需要确保把原本可能处于特殊渲染状态的 27 个方块全部【还原回 DEFAULT】
                if (oldState == HugeElectrodePlateSavedData.StructureState.FORMED || oldState == HugeElectrodePlateSavedData.StructureState.TANK_CONFLICT || oldState == HugeElectrodePlateSavedData.StructureState.INTERFERED) {
                    for (int x = 0; x < 3; x++) {
                        for (int y = 0; y < 3; y++) {
                            for (int z = 0; z < 3; z++) {
                                BlockPos currentPos = origin.offset(x, y, z);
                                BlockState currentState = level.getBlockState(currentPos);
                                // 只有当这个位置依然是我们的电极板时（未被挖掉/炸掉），才恢复它的默认渲染
                                if (currentState.getBlock() instanceof ElectrodePlate) {
                                    level.setBlock(currentPos, currentState.setValue(ElectrodePlate.RENDER_KIND, ElectrodePlate.RenderKind.DEFAULT), 3);
                                }
                            }
                        }
                    }
                }

                // 以下为你原本的消息判定分支
                if (newState == HugeElectrodePlateSavedData.StructureState.TANK_CONFLICT) {
                    message = "§c[Ethereal Void] 位于 " + xyzStr + " 的立方体无法组装！电极片均不能属于水槽";
                    tracked.put(center, newState);
                    data.setDirty();
                }
                else if (newState == HugeElectrodePlateSavedData.StructureState.INTERFERED) {
                    message = "§e[Ethereal Void] 位于 " + xyzStr + " 的立方体结构由于错误连接无法成型。";
                    tracked.put(center, newState);
                    data.setDirty();
                }
                else if (newState == HugeElectrodePlateSavedData.StructureState.NONE) {
                    message = "§c[Ethereal Void] 位于 " + xyzStr + " 的立方体结构被破坏。";
                    tracked.remove(center);
                    data.setDirty();
                }
            }

            if (message != null) {
                Component chatComponent = Component.literal(message);
                for (Player player : level.players()) {
                    if (player.distanceToSqr(Vec3.atCenterOf(center)) < 64 * 64) {
                        player.displayClientMessage(chatComponent, false);
                    }
                }
            }
        }
    }

    private static HugeElectrodePlateSavedData.StructureState evaluateActualState(ServerLevel level, BlockPos center) {
        BlockPos origin = center.offset(-1, -1, -1);

        boolean hasTankConflict = false;

        // 1. 验证内部 3x3x3 核心是否全都是电极板，并检查是否属于水槽
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos currentPos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(currentPos);

                    if (!(state.getBlock() instanceof ElectrodePlate)) {
                        return HugeElectrodePlateSavedData.StructureState.NONE;
                    }

                    // ============================== 【这里已完美对接你的源码】 ==============================
                    // 只要 MODE 属性不是 UNASSIGNED（即属于 NEGATIVE 或 POSITIVE），就判定为属于水槽
                    if (state.hasProperty(ElectrodePlate.MODE) && state.getValue(ElectrodePlate.MODE) != ElectrodePlate.ElectrodeMode.UNASSIGNED) {
                        hasTankConflict = true;
                    }
                    // ========================================================================================
                }
            }
        }

        // 如果 3x3x3 结构存在，但其中有任何一个方块已经属于水槽，直接判定为水槽冲突
        if (hasTankConflict) {
            return HugeElectrodePlateSavedData.StructureState.TANK_CONFLICT;
        }

        // 2. 验证 5x5x5 环境和液体（保持你原本的代码不变）
        boolean hasInterference = false;
        for (int x = -1; x <= 3; x++) {
            for (int y = -1; y <= 3; y++) {
                for (int z = -1; z <= 3; z++) {
                    BlockPos currentPos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(currentPos);

                    boolean isInsideCore = x >= 0 && x < 3 && y >= 0 && y < 3 && z >= 0 && z < 3;

                    if (isInsideCore) {
                        if (!state.getFluidState().isEmpty()) {
                            return HugeElectrodePlateSavedData.StructureState.NONE;
                        }
                    } else {
                        if (state.getBlock() instanceof ElectrodePlate) {
                            hasInterference = true;
                        }
                    }
                }
            }
        }

        return hasInterference ? HugeElectrodePlateSavedData.StructureState.INTERFERED : HugeElectrodePlateSavedData.StructureState.FORMED;
    }
}