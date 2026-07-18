package com.mjzaymi.etherealvoid.entity.smallrocket;

import com.mjzaymi.etherealvoid.EtherealVoid;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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
    // 0 = 未点火(IDLE), 1 = 倒计时(COUNTDOWN), 2 = 飞行中(FLYING)
    private static final EntityDataAccessor<Integer> LAUNCH_STAGE = SynchedEntityData.defineId(SmallRocketEntity.class, EntityDataSerializers.INT);
    // 📊 同步倒计时时间（200 到 0）
    private static final EntityDataAccessor<Integer> COUNTDOWN = SynchedEntityData.defineId(SmallRocketEntity.class, EntityDataSerializers.INT);

    private boolean forbidDismount = false;

    private static final ResourceKey<Level> SPACE_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "low_earth_orbit")
    );

    public static final SoundEvent SOUND_STAGE_1 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "rocket_ignite_stage1"));
    public static final SoundEvent SOUND_STAGE_2 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(EtherealVoid.MOD_ID, "rocket_ignite_stage2"));

    public SmallRocketEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(LAUNCH_STAGE, 0);
        this.entityData.define(COUNTDOWN, 200); // 默认 10 秒
    }

    @Override
    public void tick() {
        super.tick();

        int stage = this.getLaunchStage();
        int timeLeft = this.getCountdown();

        // ─── 1. ✨ 客户端特效与屏幕震动处理 ───
        if (this.level().isClientSide) {
            // 💨 阶段 1：倒计时最后 5 秒（<= 100 ticks）剧烈预热点火
            if (stage == 1 && timeLeft <= 100) {
                int smokeIntensity = (101 - timeLeft) / 8;

                // 📳 【预热震动】：随着倒计时归零，震动幅度从微颤线性暴增
                Entity passenger = this.getFirstPassenger();
                if (passenger instanceof Player player && player.level().isClientSide) {
                    // 强度随时间推进从 0.05 增至 0.45
                    float shakeFactor = (101 - timeLeft) * 0.004F + 0.05F;
                    float shakeX = (this.random.nextFloat() - 0.5F) * shakeFactor;
                    float shakeY = (this.random.nextFloat() - 0.5F) * shakeFactor;
                    player.setXRot(player.getXRot() + shakeX);
                    player.setYRot(player.getYRot() + shakeY);
                }

                for (int i = 0; i < smokeIntensity + 6; i++) {
                    double angle = this.random.nextDouble() * 2.0D * Math.PI;
                    double speed = 0.3D + this.random.nextDouble() * 0.5D;
                    double motionX = Math.cos(angle) * speed;
                    double motionZ = Math.sin(angle) * speed;

                    double spawnX = this.getX() + Math.cos(angle) * 0.6D;
                    double spawnZ = this.getZ() + Math.sin(angle) * 0.6D;

                    // 【白色巨型气浪】
                    this.level().addParticle(
                            ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                            spawnX, this.getY() - 0.2D, spawnZ,
                            motionX, 0.08D, motionZ
                    );

                    // 【深色高压排气】
                    if (this.random.nextBoolean()) {
                        this.level().addParticle(
                                ParticleTypes.LARGE_SMOKE,
                                spawnX, this.getY() - 0.1D, spawnZ,
                                motionX * 1.3D, 0.02D, motionZ * 1.3D
                        );
                    }

                    // 【地表火舌】最后 2 秒
                    if (timeLeft <= 40 && this.random.nextInt(2) == 0) {
                        this.level().addParticle(
                                ParticleTypes.FLAME,
                                this.getX() + (this.random.nextDouble() - 0.5D) * 1.2D,
                                this.getY() - 0.2D,
                                this.getZ() + (this.random.nextDouble() - 0.5D) * 1.2D,
                                motionX * 0.4D, 0.05D, motionZ * 0.4D
                        );
                    }
                }
            }
            // 🔥 阶段 2：正式起飞，全功率超大粒子矩阵与持续重载震动
            else if (stage == 2) {
                // 📳 【起飞狂震】：模拟高空突破大气层时极强的机身撕裂震动
                Entity passenger = this.getFirstPassenger();
                if (passenger instanceof Player player && player.level().isClientSide) {
                    // 持续保持高频狂震（可以适当根据高度或速度调整，这里给一个稳定的硬核大震动）
                    float shakeX = (this.random.nextFloat() - 0.5F) * 0.6F;
                    float shakeY = (this.random.nextFloat() - 0.5F) * 0.6F;
                    player.setXRot(player.getXRot() + shakeX);
                    player.setYRot(player.getYRot() + shakeY);
                }

                // 1. 【黄色/明橙色】核心炽热高压火柱
                for (int i = 0; i < 12; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5D) * 0.6D;
                    double offsetZ = (this.random.nextDouble() - 0.5D) * 0.6D;
                    this.level().addParticle(
                            ParticleTypes.FLAME,
                            this.getX() + offsetX, this.getY() - 0.3D, this.getZ() + offsetZ,
                            offsetX * 0.5D, -1.2D, offsetZ * 0.5D
                    );
                }

                // 2. 【深灰色/黑色】广角燃料尾气
                for (int i = 0; i < 8; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5D) * 0.8D;
                    double offsetZ = (this.random.nextDouble() - 0.5D) * 0.8D;
                    this.level().addParticle(
                            ParticleTypes.LARGE_SMOKE,
                            this.getX() + offsetX, this.getY() - 0.5D, this.getZ() + offsetZ,
                            offsetX * 1.5D, -0.6D, offsetZ * 1.5D
                    );
                }

                // 3. 【浅白色】受尾流冲击剧烈膨胀四散的白烟
                for (int i = 0; i < 5; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5D) * 1.2D;
                    double offsetZ = (this.random.nextDouble() - 0.5D) * 1.2D;
                    this.level().addParticle(
                            ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            this.getX() + offsetX, this.getY() - 0.8D, this.getZ() + offsetZ,
                            (this.random.nextDouble() - 0.5D) * 1.5D, -0.1D, (this.random.nextDouble() - 0.5D) * 1.5D
                    );
                }

                // 4. 💥【火星弹射】高热火星爆裂
                for (int i = 0; i < 2; i++) {
                    if (this.random.nextBoolean()) {
                        double offsetX = (this.random.nextDouble() - 0.5D) * 0.5D;
                        double offsetZ = (this.random.nextDouble() - 0.5D) * 0.5D;
                        this.level().addParticle(
                                ParticleTypes.LAVA,
                                this.getX() + offsetX, this.getY() - 0.4D, this.getZ() + offsetZ,
                                (this.random.nextDouble() - 0.5D) * 2.0D, -0.4D, (this.random.nextDouble() - 0.5D) * 2.0D
                        );
                    }
                }
            }
        }

        // ─── 2. 服务端核心逻辑 ───
        if (!this.level().isClientSide) {
            if (stage == 1) {
                int newTime = timeLeft - 1;
                this.setCountdown(newTime);

                if (newTime % 20 == 0 && newTime >= 0) {
                    int secondsLeft = newTime / 20;
                    if (this.getFirstPassenger() instanceof ServerPlayer player) {
                        player.displayClientMessage(Component.literal("§c 点火倒计时: §e" + secondsLeft + "秒 §c"), true);
                    }
                }

                if (newTime == 0) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SOUND_STAGE_2, SoundSource.NEUTRAL, 4.0F, 1.0F);
                }

                if (newTime <= 0) {
                    this.setLaunchStage(2);
                }
            }
            else if (stage == 2) {
                if (this.getY() > 20000) {
                    this.teleportToDimension();
                }
            }
        }

        // ─── 3. 双端共同执行的物理位移计算 ───
        if (stage == 2) {
            Vec3 motion = this.getDeltaMovement();
            double upwardSpeed = Math.min(motion.y + 0.003D, 5.0D);
            this.setDeltaMovement(motion.x, upwardSpeed, motion.z);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        }
        else if (stage == 1) {
            this.setDeltaMovement(Vec3.ZERO);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        }
        else {
            if (!this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
            } else {
                this.setDeltaMovement(Vec3.ZERO);
            }
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        }
    }

    public void startCountdown() {
        if (this.getLaunchStage() == 0) {
            this.setLaunchStage(1);
            this.setCountdown(200);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.isVehicle()) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide) {
            player.startRiding(this);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("§a已进入火箭驾驶舱。按下 [空格键] 点火发射！"), true);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    public void setForbidDismount(boolean forbid) { this.forbidDismount = forbid; }
    public boolean isDismountForbidden() { return this.forbidDismount; }

    private void teleportToDimension() {
        if (this.getFirstPassenger() instanceof ServerPlayer serverPlayer) {
            System.out.println("火箭已穿透大气层！准备传送玩家...");
        }
        this.discard();
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            moveFunction.accept(passenger, this.getX(), this.getY() + 0.5D, this.getZ());
        }
    }

    @Nullable @Override public LivingEntity getControllingPassenger() { return null; }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        if (this.level().isClientSide && this.getLaunchStage() == 2) {
            return;
        }
        super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
    }

    @Override public boolean isPushable() { return false; }
    @Override public void push(Entity entity) {}
    @Override public boolean canBeCollidedWith() { return false; }

    public int getLaunchStage() { return this.entityData.get(LAUNCH_STAGE); }
    public void setLaunchStage(int stage) { this.entityData.set(LAUNCH_STAGE, stage); }
    public int getCountdown() { return this.entityData.get(COUNTDOWN); }
    public void setCountdown(int time) { this.entityData.set(COUNTDOWN, time); }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setLaunchStage(tag.getInt("LaunchStage"));
        this.setCountdown(tag.getInt("CountdownTimer"));
        this.forbidDismount = tag.getBoolean("ForbidDismount");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("LaunchStage", this.getLaunchStage());
        tag.putInt("CountdownTimer", this.getCountdown());
        tag.putBoolean("ForbidDismount", this.forbidDismount);
    }

    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return new ClientboundAddEntityPacket(this); }
    @Override public boolean isPickable() { return !this.isRemoved(); }
}