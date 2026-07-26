package com.mjzaymi.etherealvoid.dimensions.space;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
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
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        return new PortalInfo(
                new Vec3(this.x, this.y, this.z),
                new Vec3(this.motionX, this.motionY, this.motionZ),
                entity.getYRot(),
                entity.getXRot()
        );
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {

        // 玩家的传送是由原版代码内部处理的，直接放行
        if (entity instanceof ServerPlayer) {
            return repositionEntity.apply(false);
        }

        // 🚀【核心修复】：完全绕过原版传送门寻找机制，手动生成新维度的火箭！
        Entity newEntity = entity.getType().create(destWorld);
        if (newEntity != null) {
            newEntity.restoreFrom(entity); // 复制原火箭的所有 NBT 和状态

            // 🌟 必须在 addDuringTeleport 之前精准设定坐标！
            // 这样客户端收到的第一个生成包就是在 -60，绝不会在 320！
            newEntity.moveTo(this.x, this.y, this.z, yaw, entity.getXRot());
            newEntity.setDeltaMovement(new Vec3(this.motionX, this.motionY, this.motionZ));

            // 正式将正确坐标的火箭加入新世界
            destWorld.addDuringTeleport(newEntity);
        }

        return newEntity;
    }
}