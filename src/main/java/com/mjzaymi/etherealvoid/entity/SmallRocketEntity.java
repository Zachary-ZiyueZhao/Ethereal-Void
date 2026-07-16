package com.mjzaymi.etherealvoid.entity;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SmallRocketEntity extends Entity {
    private static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(SmallRocketEntity.class, EntityDataSerializers.BOOLEAN);

    private static final ResourceKey<Level> SPACE_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "low_earth_orbit")
    );

    public SmallRocketEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(IS_FLYING, false);
    }

    @Override
    public void tick() {
        super.tick();

        // ─── 1. 特效处理（仅客户端） ───
        if (this.level().isClientSide && this.isFlying()) {
            // TODO: 在这里添加你的火箭喷火、冒烟等粒子特效
            // 例如: this.level().addParticle(ParticleTypes.FLAME, ...);
        }

        // ─── 2. 核心物理与位移计算（双端运行，消除抖动！） ───
        if (this.isFlying()) {
            // 纵向升天物理逻辑
            Vec3 motion = this.getDeltaMovement();
            double upwardSpeed = Math.min(motion.y + 0.05D, 1.5D);

            // 横向保持
            double xSpeed = motion.x;
            double zSpeed = motion.z;

            this.setDeltaMovement(xSpeed, upwardSpeed, zSpeed);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

            // ─── 3. 跨维度检测（严格在服务端执行，防止客户端越权） ───
            if (!this.level().isClientSide) {
                if (this.getY() > 20000) {
                    this.teleportToDimension();
                }
            }
        } else {
            // 未起飞时的重力逻辑（同样双端运行，保证下落平滑）
            if (!this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
            } else {
                this.setDeltaMovement(Vec3.ZERO);
            }
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.isVehicle()) {
            return InteractionResult.PASS;
        }

        if (!this.level().isClientSide) {
            player.startRiding(this);
            this.setFlying(true);
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    // 跨维度传送逻辑
    private void teleportToDimension() {
        if (this.getFirstPassenger() instanceof ServerPlayer serverPlayer) {
            System.out.println("火箭已穿透大气层！准备传送玩家...");
            // 这里执行你的玩家维度传送逻辑
        }
        this.discard();
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            moveFunction.accept(passenger, this.getX(), this.getY() + 0.5D, this.getZ());
        }
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return null;
    }

    // 🌟🌟🌟 核心优化 1：过滤由于服务器卡顿导致的微小网络抖动 🌟🌟🌟
    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        // 如果火箭正在飞行中
        if (this.level().isClientSide && this.isFlying()) {
            // 计算当前客户端预测坐标与服务端发来同步坐标的距离平方
            double distSqr = this.distanceToSqr(x, y, z);
            // 如果偏差小于 4.0D (也就是距离小于 2 格)，说明只是细微的同步漂移/卡顿
            // 我们直接忽略服务端的强制修正，使用客户端自己平滑预测的物理轨迹
            if (distSqr > 4.0D) {
                // 🌟 这里追加了第 7 个参数 teleport
                super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
            }
        } else {
            // 如果没起飞，或者差距过大，依然走原版的正常插值逻辑
            super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
        }
    }

    // 🌟🌟🌟 核心优化 2：彻底关闭火箭的实体挤压物理 🌟🌟🌟
    @Override
    public boolean isPushable() {
        return false; // 火箭是重型设备，无法被其他实体推动
    }

    @Override
    public void push(Entity entity) {
        // 空实现：不允许任何实体（包括其他火箭）挤压、反弹这台火箭
    }

    @Override
    public boolean canBeCollidedWith() {
        return false; // 不允许其他实体的物理体积与它发生物理碰撞，穿透即可
    }

    // Getter 和 Setter 以及其他必要的方法
    public boolean isFlying() { return this.entityData.get(IS_FLYING); }
    public void setFlying(boolean flying) { this.entityData.set(IS_FLYING, flying); }

    @Override protected void readAdditionalSaveData(CompoundTag tag) { this.setFlying(tag.getBoolean("Flying")); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putBoolean("Flying", this.isFlying()); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return new ClientboundAddEntityPacket(this); }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }
}