package com.mjzaymi.etherealvoid.multiblock;

import com.mjzaymi.etherealvoid.block.ElectrodePlate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

public class HugeElectrodePlate {
    public static final ResourceLocation STRUCTURE_RL = new ResourceLocation("ethereal_void", "huge_electrode_plate");

    /**
     * 当玩家放置一个电极板时，尝试以此方块为基础，寻找并验证 3x3x3 巨型电极板
     */
    public static void tryFormStructure(Level level, BlockPos placedPos, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        StructureTemplateManager manager = serverLevel.getStructureManager();
        Optional<StructureTemplate> templateOpt = manager.get(STRUCTURE_RL);

        // 1. 检查 NBT 是否成功加载
        if (templateOpt.isEmpty()) {
            System.out.println("[Ethereal Void] 警告: 找不到 NBT 结构文件！请检查路径: data/ethereal_void/structures/huge_electrode_plate.nbt");
            return;
        }

        StructureTemplate template = templateOpt.get();

        // 获取当前放置的方块，用于传入 NBT 过滤器
        Block electrodeBlock = level.getBlockState(placedPos).getBlock();

        // 穷举 27 种可能的结构起点
        for (int dx = -2; dx <= 0; dx++) {
            for (int dy = -2; dy <= 0; dy++) {
                for (int dz = -2; dz <= 0; dz++) {
                    BlockPos potentialOrigin = placedPos.offset(dx, dy, dz);

                    // 2. 传入当前方块进行精确 NBT 匹配
                    if (matchNbtStructure(serverLevel, template, potentialOrigin, electrodeBlock)) {

                        // 3. 验证外围 5x5x5 防干扰以及泡水限制
                        if (validateEnvironment(serverLevel, potentialOrigin, electrodeBlock)) {
                            if (player != null) {
                                player.displayClientMessage(Component.literal("§a[Ethereal Void] 巨型电极板结构已完美成型！(纯观赏)"), false);
                            }
                            return; // 成功找到一个组合，直接退出
                        }
                    }
                }
            }
        }
    }

    /**
     * 验证世界方块是否与 NBT 匹配
     */
    private static boolean matchNbtStructure(ServerLevel level, StructureTemplate template, BlockPos origin, Block electrodeBlock) {
        // 关键修复：第三个参数传入 electrodeBlock，只获取 NBT 中该方块的信息
        var infoList = template.filterBlocks(origin, new StructurePlaceSettings(), electrodeBlock);

        // 安全检查：如果 NBT 匹配出来的方块数量不是 27 个，说明这个起点是不对的
        if (infoList.size() != 27) {
            return false;
        }

        for (StructureTemplate.StructureBlockInfo info : infoList) {
            BlockPos worldPos = info.pos();
            BlockState worldState = level.getBlockState(worldPos);

            // 检查世界上对应的位置是不是也是电极板
            if (worldState.getBlock() != electrodeBlock) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证环境：任何一个电极板不能处于水/流体中；且外围 5x5x5 范围内不得有额外的电极板。
     */
    private static boolean validateEnvironment(ServerLevel level, BlockPos origin, Block electrodeBlock) {
        // 遍历 5x5x5 的外部环境 (比 3x3x3 各往外拓宽 1 格)
        for (int x = -1; x <= 3; x++) {
            for (int y = -1; y <= 3; y++) {
                for (int z = -1; z <= 3; z++) {
                    BlockPos currentPos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(currentPos);

                    // 检查是否在 3x3x3 内部
                    boolean isInsideStructure = x >= 0 && x < 3 && y >= 0 && y < 3 && z >= 0 && z < 3;

                    if (isInsideStructure) {
                        // 【内部检测】不能泡在水（或任何流体）里
                        if (!state.getFluidState().isEmpty()) {
                            return false;
                        }
                    } else {
                        // 【外围检测】5x5x5 的外壳中，如果出现了额外的电极板，则结构失效
                        if (state.getBlock() == electrodeBlock) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}