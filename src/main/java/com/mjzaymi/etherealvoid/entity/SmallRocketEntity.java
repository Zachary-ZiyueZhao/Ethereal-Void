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
    // 使用数据同步器来记录火箭是否处于飞行状态（方便客户端生成粒子特效）
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

        if (this.isFlying()) {
            // ─── 1. 纵向升天逻辑 ───
            Vec3 motion = this.getDeltaMovement();
            // 每个tick给 Y 轴施加向上的加速度，上限设为 1.5 块/tick（已经非常快了）
            double upwardSpeed = Math.min(motion.y + 0.05D, 1.5D);

            // ─── 2. 横向转向逻辑 ───
            double xSpeed = motion.x;
            double zSpeed = motion.z;

            // 获取坐在火箭上的驾驶员
            LivingEntity driver = this.getControllingPassenger();
            if (driver != null) {
                // 让火箭的朝向（Yaw）跟随玩家的视线转向
                this.setYRot(driver.getYRot());
                this.yRotO = this.getYRot();

                // 根据火箭当前的朝向，给它一个微弱的前进速度（0.1D），实现“略微转向”
                float radians = this.getYRot() * ((float)Math.PI / 180F);
                xSpeed = -Math.sin(radians) * 0.1D;
                zSpeed = Math.cos(radians) * 0.1D;
            }

            // 应用新速度并移动火箭（这会覆盖掉原生重力）
            this.setDeltaMovement(xSpeed, upwardSpeed, zSpeed);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

            // ─── 3. 跨维度检测 ───
            if (!this.level().isClientSide && this.getY() > 20000) {
                this.teleportToDimension();
            }
        } else {
            // 如果没起飞，应用常规重力，让它能稳稳停在地面上
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
            return InteractionResult.PASS; // 如果已经有人在上面了，不响应
        }

        if (!this.level().isClientSide) {
            // 在服务端将玩家设为乘客并激活起飞
            player.startRiding(this);
            this.setFlying(true);
        }

        // 返回 sidedSuccess：客户端返回 SUCCESS（触发手臂挥动等效果），服务端返回 CONSUME
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    // 跨维度传送逻辑
    private void teleportToDimension() {
        if (this.getControllingPassenger() instanceof ServerPlayer serverPlayer) {
            // 这里后续可以加入自定义的 Teleporter 逻辑，带玩家去你的自定义维度
            // 比如：serverPlayer.changeDimension(targetWorld, new ModTeleporter());
            System.out.println("火箭已穿透大气层！准备传送玩家...");
        }
        // 传送后销毁主世界的火箭实体，防止留在天上变成高空垃圾
        this.discard();
    }

    // 修正乘客（玩家）在火箭上的坐姿和位置
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            // 这里的 0.5D 是相对于火箭底部的 Y 轴偏移，你可以根据你的模型座舱高度自由调整
            moveFunction.accept(passenger, this.getX(), this.getY() + 0.5D, this.getZ());
        }
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        // 获取第一个乘客，如果它是 LivingEntity（如 Player），则作为控制者返回
        return this.getFirstPassenger() instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    // Getter 和 Setter
    public boolean isFlying() { return this.entityData.get(IS_FLYING); }
    public void setFlying(boolean flying) { this.entityData.set(IS_FLYING, flying); }

    // 必须实现的基础 NBT 读写（防止存档读档后状态丢失）
    @Override protected void readAdditionalSaveData(CompoundTag tag) { this.setFlying(tag.getBoolean("Flying")); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putBoolean("Flying", this.isFlying()); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return new ClientboundAddEntityPacket(this); }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }
}