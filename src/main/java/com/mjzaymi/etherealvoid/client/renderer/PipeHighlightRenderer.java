package com.mjzaymi.etherealvoid.client.renderer;

import com.mjzaymi.etherealvoid.block.FluidPipe;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PipeHighlightRenderer {

    private static BlockPos lastLookedPos = null;
    // 💡 关键修复：缓存位置 + BlockState 的映射，确保状态变化时能感知到
    private static final Map<BlockPos, BlockState> cachedNetworkStates = new HashMap<>();
    private static BlockPos networkOrigin = null;
    private static VoxelShape mergedNetworkShape = Shapes.empty();

    // 淡入淡出控制
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

        // 线性淡入淡出插值
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

    /**
     * 💡 深度检查：比对坐标 + 方块状态 (BlockState)
     */
    private static void checkAndUpdateNetworkCache(Level level, BlockPos startPos) {
        Map<BlockPos, BlockState> currentFoundStates = new HashMap<>();
        Queue<BlockPos> queue = new LinkedList<>();

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

                    if (!currentFoundStates.containsKey(neighbor) && neighborState.getBlock() instanceof FluidPipe) {
                        currentFoundStates.put(neighbor, neighborState);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // 💡 只有当网络位置集 OR 任何位置上的 BlockState 发生改变时，才重新渲染
        if (!currentFoundStates.equals(cachedNetworkStates) || !startPos.equals(lastLookedPos)) {
            lastLookedPos = startPos;
            cachedNetworkStates.clear();
            cachedNetworkStates.putAll(currentFoundStates);

            networkOrigin = startPos;
            mergedNetworkShape = Shapes.empty();

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

        // 呼吸感系数
        float rawBreathe = 0.35f + 0.25f * (float) Math.sin((time + partialTicks) * 0.22f);
        float finalFaceAlpha = rawBreathe * 0.45f * currentFade;
        float finalFrameAlpha = Math.min(1.0f, (rawBreathe + 0.35f)) * currentFade;

        poseStack.pushPose();
        poseStack.translate(networkOrigin.getX() - camPos.x, networkOrigin.getY() - camPos.y, networkOrigin.getZ() - camPos.z);
        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer quadConsumer = bufferSource.getBuffer(RenderType.debugQuads());

        float faceR = 0.25f, faceG = 0.50f, faceB = 0.38f;
        float frameR = 0.35f, frameG = 0.70f, frameB = 0.52f;

        float zOffset = 0.001f;

        // 1. 绘制半透明填充面
        mergedNetworkShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            renderCustomFaces(
                    matrix, quadConsumer,
                    minX - zOffset, minY - zOffset, minZ - zOffset,
                    maxX + zOffset, maxY + zOffset, maxZ + zOffset,
                    faceR, faceG, faceB, finalFaceAlpha
            );
        });

        // 2. 绘制 3D 粗线条包边
        ThickOutlineRenderer.renderThickEdges(
                matrix, quadConsumer, mergedNetworkShape,
                0.015f, frameR, frameG, frameB, finalFrameAlpha
        );

        bufferSource.endBatch(RenderType.debugQuads());

        poseStack.popPose();
    }

    private static void renderCustomFaces(Matrix4f matrix, VertexConsumer consumer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        float x1 = (float) minX, y1 = (float) minY, z1 = (float) minZ;
        float x2 = (float) maxX, y2 = (float) maxY, z2 = (float) maxZ;

        // Down
        addVertex(consumer, matrix, x1, y1, z1, r, g, b, a, 0, -1, 0); addVertex(consumer, matrix, x2, y1, z1, r, g, b, a, 0, -1, 0); addVertex(consumer, matrix, x2, y1, z2, r, g, b, a, 0, -1, 0); addVertex(consumer, matrix, x1, y1, z2, r, g, b, a, 0, -1, 0);
        // Up
        addVertex(consumer, matrix, x1, y2, z1, r, g, b, a, 0, 1, 0); addVertex(consumer, matrix, x1, y2, z2, r, g, b, a, 0, 1, 0); addVertex(consumer, matrix, x2, y2, z2, r, g, b, a, 0, 1, 0); addVertex(consumer, matrix, x2, y2, z1, r, g, b, a, 0, 1, 0);
        // North
        addVertex(consumer, matrix, x1, y1, z1, r, g, b, a, 0, 0, -1); addVertex(consumer, matrix, x1, y2, z1, r, g, b, a, 0, 0, -1); addVertex(consumer, matrix, x2, y2, z1, r, g, b, a, 0, 0, -1); addVertex(consumer, matrix, x2, y1, z1, r, g, b, a, 0, 0, -1);
        // South
        addVertex(consumer, matrix, x1, y1, z2, r, g, b, a, 0, 0, 1); addVertex(consumer, matrix, x2, y1, z2, r, g, b, a, 0, 0, 1); addVertex(consumer, matrix, x2, y2, z2, r, g, b, a, 0, 0, 1); addVertex(consumer, matrix, x1, y2, z2, r, g, b, a, 0, 0, 1);
        // West
        addVertex(consumer, matrix, x1, y1, z1, r, g, b, a, -1, 0, 0); addVertex(consumer, matrix, x1, y1, z2, r, g, b, a, -1, 0, 0); addVertex(consumer, matrix, x1, y2, z2, r, g, b, a, -1, 0, 0); addVertex(consumer, matrix, x1, y2, z1, r, g, b, a, -1, 0, 0);
        // East
        addVertex(consumer, matrix, x2, y1, z1, r, g, b, a, 1, 0, 0); addVertex(consumer, matrix, x2, y2, z1, r, g, b, a, 1, 0, 0); addVertex(consumer, matrix, x2, y2, z2, r, g, b, a, 1, 0, 0); addVertex(consumer, matrix, x2, y1, z2, r, g, b, a, 1, 0, 0);
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float r, float g, float b, float a, float nx, float ny, float nz) {
        consumer.vertex(matrix, x, y, z).color(r, g, b, a).normal(nx, ny, nz).endVertex();
    }
}