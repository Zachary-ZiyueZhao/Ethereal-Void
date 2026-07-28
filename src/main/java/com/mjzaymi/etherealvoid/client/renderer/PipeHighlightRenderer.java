package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.block.FluidPipe;
import com.mjzaymi.etherealvoid.blockentity.ReactionPoolFluidIOBlockEntity;
import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PipeHighlightRenderer {

    private static BlockPos lastLookedPos = null;
    private static final Map<BlockPos, BlockState> cachedNetworkStates = new HashMap<>();

    // 💡 修复核心：新增 IO 节点的动态状态缓存 (坐标 -> 模式状态位)
    private static final Map<BlockPos, Integer> cachedIOStates = new HashMap<>();

    private static BlockPos networkOrigin = null;
    private static VoxelShape mergedNetworkShape = Shapes.empty();

    private static boolean isSelfLoopDetected = false;
    private static final List<VoxelShape> loopingPoolShapes = new ArrayList<>();

    // 淡入淡出动画控制
    private static float currentFade = 0.0f;
    private static long lastFrameTime = System.currentTimeMillis();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000.0f;
        lastFrameTime = currentTime;
        deltaTime = Math.min(deltaTime, 0.1f);

        HitResult hitResult = mc.hitResult;
        boolean isLookingAtPipe = false;
        BlockPos targetPos = null;

        if (hitResult instanceof BlockHitResult blockHit && hitResult.getType() == HitResult.Type.BLOCK) {
            targetPos = blockHit.getBlockPos();
            BlockState targetState = level.getBlockState(targetPos);
            if (targetState.getBlock() instanceof FluidPipe) {
                isLookingAtPipe = true;
            }
        }

        if (isLookingAtPipe) {
            checkAndUpdateNetworkCache(level, targetPos);
        }

        // 线性淡入淡出
        float fadeSpeed = 6.0f;
        if (isLookingAtPipe) {
            currentFade = Math.min(1.0f, currentFade + fadeSpeed * deltaTime);
        } else {
            currentFade = Math.max(0.0f, currentFade - fadeSpeed * deltaTime);
        }

        if (currentFade <= 0.001f) {
            return;
        }

        renderNetworkHighlight(event, mc, level);
    }

    private static void checkAndUpdateNetworkCache(Level level, BlockPos startPos) {
        Map<BlockPos, BlockState> currentFoundStates = new HashMap<>();
        Map<BlockPos, Integer> currentIOStates = new HashMap<>();

        Queue<BlockPos> queue = new LinkedList<>();
        Set<ReactionPoolFluidIOBlockEntity> inputIOs = new HashSet<>();
        Set<ReactionPoolFluidIOBlockEntity> outputIOs = new HashSet<>();

        BlockState startState = level.getBlockState(startPos);
        queue.add(startPos);
        currentFoundStates.put(startPos, startState);

        while (!queue.isEmpty()) {
            BlockPos curr = queue.poll();
            BlockState state = level.getBlockState(curr);
            if (!(state.getBlock() instanceof FluidPipe fluidPipe)) continue;

            for (Direction dir : Direction.values()) {
                if (state.getValue(fluidPipe.getPropertyForDirection(dir))) {
                    BlockPos neighbor = curr.relative(dir);
                    BlockState neighborState = level.getBlockState(neighbor);

                    if (!currentFoundStates.containsKey(neighbor)) {
                        if (neighborState.getBlock() instanceof FluidPipe) {
                            currentFoundStates.put(neighbor, neighborState);
                            queue.add(neighbor);
                        } else {
                            BlockEntity be = level.getBlockEntity(neighbor);
                            if (be instanceof ReactionPoolFluidIOBlockEntity ioBE) {
                                // 💡 记录当前 IO 口的状态标志位（输入/输出/水槽有效性）
                                int flags = (ioBE.isInputMode() ? 1 : 0)
                                        | (ioBE.isOutputMode() ? 2 : 0)
                                        | (ioBE.hasValidPool() ? 4 : 0);
                                currentIOStates.put(neighbor, flags);

                                if (ioBE.isInputMode()) inputIOs.add(ioBE);
                                else if (ioBE.isOutputMode()) outputIOs.add(ioBE);
                            }
                        }
                    }
                }
            }
        }

        // 💡 条件判定：管道 BlockState、IO 口模式标志位或瞄准位置变动时，立刻全盘重构！
        if (!currentFoundStates.equals(cachedNetworkStates)
                || !currentIOStates.equals(cachedIOStates)
                || !startPos.equals(lastLookedPos)) {

            lastLookedPos = startPos;
            cachedNetworkStates.clear();
            cachedNetworkStates.putAll(currentFoundStates);

            cachedIOStates.clear();
            cachedIOStates.putAll(currentIOStates);

            networkOrigin = startPos;
            mergedNetworkShape = Shapes.empty();
            loopingPoolShapes.clear();
            isSelfLoopDetected = false;

            // 1. 构建管道图层 Shape
            for (Map.Entry<BlockPos, BlockState> entry : cachedNetworkStates.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                VoxelShape localShape = state.getShape(level, pos);

                if (!localShape.isEmpty()) {
                    double offsetX = pos.getX() - networkOrigin.getX();
                    double offsetY = pos.getY() - networkOrigin.getY();
                    double offsetZ = pos.getZ() - networkOrigin.getZ();
                    mergedNetworkShape = Shapes.or(mergedNetworkShape, localShape.move(offsetX, offsetY, offsetZ));
                }
            }

            // 2. 客户端自环检测与水槽结构收集
            Set<CuboidStructure> matchedStructures = new HashSet<>();
            for (ReactionPoolFluidIOBlockEntity inIO : inputIOs) {
                Optional<CuboidStructure> inStruct = CuboidStructure.findFromWallAndCorner(level, inIO.getBlockPos());
                if (inStruct.isEmpty()) continue;

                for (ReactionPoolFluidIOBlockEntity outIO : outputIOs) {
                    Optional<CuboidStructure> outStruct = CuboidStructure.findFromWallAndCorner(level, outIO.getBlockPos());
                    if (outStruct.isEmpty()) continue;

                    if (inStruct.get().min().equals(outStruct.get().min()) && inStruct.get().max().equals(outStruct.get().max())) {
                        isSelfLoopDetected = true;
                        matchedStructures.add(inStruct.get());
                    }
                }
            }

            // 3. 将自环水槽的外框转换为相对 Shape，准备渲染大红框
            for (CuboidStructure struct : matchedStructures) {
                BlockPos min = struct.min();
                BlockPos max = struct.max();
                VoxelShape poolShape = Shapes.box(
                        min.getX() - networkOrigin.getX(),
                        min.getY() - networkOrigin.getY(),
                        min.getZ() - networkOrigin.getZ(),
                        max.getX() + 1 - networkOrigin.getX(),
                        max.getY() + 1 - networkOrigin.getY(),
                        max.getZ() + 1 - networkOrigin.getZ()
                );
                loopingPoolShapes.add(poolShape);
            }
        }
    }

    private static void renderNetworkHighlight(RenderLevelStageEvent event, Minecraft mc, Level level) {
        if (mergedNetworkShape.isEmpty() || networkOrigin == null) return;

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        long time = level.getGameTime();
        float partialTicks = event.getPartialTick();

        // 呼吸感计算
        float rawBreathe = 0.35f + 0.25f * (float) Math.sin((time + partialTicks) * 0.22f);
        float finalFaceAlpha = rawBreathe * 0.45f * currentFade;
        float finalFrameAlpha = Math.min(1.0f, (rawBreathe + 0.35f)) * currentFade;

        poseStack.pushPose();
        poseStack.translate(networkOrigin.getX() - camPos.x, networkOrigin.getY() - camPos.y, networkOrigin.getZ() - camPos.z);
        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer quadConsumer = bufferSource.getBuffer(RenderType.debugQuads());

        float faceR, faceG, faceB;
        float frameR, frameG, frameB;

        if (isSelfLoopDetected) {
            // 🚨 自环报警红 (Alert Red)
            faceR = 0.85f; faceG = 0.15f; faceB = 0.15f;
            frameR = 1.00f; frameG = 0.25f; frameB = 0.25f;
        } else {
            // 🟢 正常莫兰迪青绿 (Teal)
            faceR = 0.25f; faceG = 0.50f; faceB = 0.38f;
            frameR = 0.35f; frameG = 0.70f; frameB = 0.52f;
        }

        float zOffset = 0.001f;

        // 1. 绘制管道网络半透明面
        mergedNetworkShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            renderCustomFaces(
                    matrix, quadConsumer,
                    minX - zOffset, minY - zOffset, minZ - zOffset,
                    maxX + zOffset, maxY + zOffset, maxZ + zOffset,
                    faceR, faceG, faceB, finalFaceAlpha
            );
        });

        // 2. 绘制管道网络 3D 粗线框
        ThickOutlineRenderer.renderThickEdges(
                matrix, quadConsumer, mergedNetworkShape,
                0.015f, frameR, frameG, frameB, finalFrameAlpha
        );

        // 3. 绘制自环水槽外部的 3D 红色巨型长方体包边
        if (isSelfLoopDetected && !loopingPoolShapes.isEmpty()) {
            for (VoxelShape poolShape : loopingPoolShapes) {
                ThickOutlineRenderer.renderThickEdges(
                        matrix, quadConsumer, poolShape,
                        0.035f, 1.0f, 0.15f, 0.15f, finalFrameAlpha
                );
            }
        }

        bufferSource.endBatch(RenderType.debugQuads());

        poseStack.popPose();
    }

    private static void renderCustomFaces(Matrix4f matrix, VertexConsumer consumer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        float x1 = (float) minX, y1 = (float) minY, z1 = (float) minZ;
        float x2 = (float) maxX, y2 = (float) maxY, z2 = (float) maxZ;

        // Down (-Y) - 修正绕序
        addVertex(consumer, matrix, x1, y1, z2, r, g, b, a);
        addVertex(consumer, matrix, x2, y1, z2, r, g, b, a);
        addVertex(consumer, matrix, x2, y1, z1, r, g, b, a);
        addVertex(consumer, matrix, x1, y1, z1, r, g, b, a);

        // Up (+Y) - 修正绕序
        addVertex(consumer, matrix, x1, y2, z1, r, g, b, a);
        addVertex(consumer, matrix, x1, y2, z2, r, g, b, a);
        addVertex(consumer, matrix, x2, y2, z2, r, g, b, a);
        addVertex(consumer, matrix, x2, y2, z1, r, g, b, a);

        // North (-Z) - 修正绕序
        addVertex(consumer, matrix, x1, y2, z1, r, g, b, a);
        addVertex(consumer, matrix, x2, y2, z1, r, g, b, a);
        addVertex(consumer, matrix, x2, y1, z1, r, g, b, a);
        addVertex(consumer, matrix, x1, y1, z1, r, g, b, a);

        // South (+Z) - 修正绕序
        addVertex(consumer, matrix, x1, y1, z2, r, g, b, a);
        addVertex(consumer, matrix, x2, y1, z2, r, g, b, a);
        addVertex(consumer, matrix, x2, y2, z2, r, g, b, a);
        addVertex(consumer, matrix, x1, y2, z2, r, g, b, a);

        // West (-X) - 修正绕序
        addVertex(consumer, matrix, x1, y1, z1, r, g, b, a);
        addVertex(consumer, matrix, x1, y1, z2, r, g, b, a);
        addVertex(consumer, matrix, x1, y2, z2, r, g, b, a);
        addVertex(consumer, matrix, x1, y2, z1, r, g, b, a);

        // East (+X) - 修正绕序
        addVertex(consumer, matrix, x2, y1, z2, r, g, b, a);
        addVertex(consumer, matrix, x2, y1, z1, r, g, b, a);
        addVertex(consumer, matrix, x2, y2, z1, r, g, b, a);
        addVertex(consumer, matrix, x2, y2, z2, r, g, b, a);
    }

    // 💡 移除不匹配的 .normal() 调用，确保与 POSITION_COLOR 格式完美契合
    private static void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float r, float g, float b, float a) {
        consumer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
    }
}