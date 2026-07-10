package com.mjzaymi.etherealvoid.dimensions.space;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class SpaceTeleporter implements ITeleporter {
    private final double x, y, z;
    private final double motionX, motionY, motionZ;

    public SpaceTeleporter(double x, double y, double z, double mx, double my, double mz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        // 让系统克隆实体到新维度
        Entity teleportedEntity = repositionEntity.apply(false);

        // 🌟 强行将坐标设置到天际交界处，并恢复传送前的速度，实现无缝惯性
        teleportedEntity.teleportTo(x, y, z);
        teleportedEntity.setDeltaMovement(new Vec3(motionX, motionY, motionZ));

        return teleportedEntity;
    }
}