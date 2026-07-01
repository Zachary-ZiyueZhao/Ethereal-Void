package com.mjzaymi.etherealvoid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

public class ResistiveHeater extends Block {
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 12, 16),
            Block.box(0, 12, 0, 3, 16, 16),
            Block.box(13, 12, 0, 16, 16, 16),
            Block.box(4, 12, 1, 6, 15.5, 15),
            Block.box(7, 12, 1, 9, 15.5, 15),
            Block.box(10, 12, 1, 12, 15.5, 15)
    );

    public ResistiveHeater() {
        super(BlockBehaviour.Properties.of()
                .strength(5f, 3f)
                .sound(SoundType.METAL)
                .noOcclusion());
        // 默认关闭
        this.registerDefaultState(this.stateDefinition.any().setValue(ENABLED, false));
    }

    // 玩家右键点击切换开关
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            // 切换状态并更新方块
            BlockState newState = state.cycle(ENABLED);
            level.setBlock(pos, newState, 3);

            // 播放开关音效
            float pitch = newState.getValue(ENABLED) ? 0.6F : 0.5F;
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, pitch);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // 加热器开启时模拟岩浆块行为
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && state.getValue(ENABLED)) {
            if (entity instanceof LivingEntity livingEntity && !entity.fireImmune() && !EnchantmentHelper.hasFrostWalker(livingEntity)) {
                entity.hurt(level.damageSources().hotFloor(), 1.0F);
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ENABLED)) {
            // 侧面粒子
            if (random.nextFloat() < 0.2F) {
                Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                double x = pos.getX() + 0.5 + dir.getStepX() * 0.55;
                double y = pos.getY() + 0.4 + random.nextDouble() * 0.5;
                double z = pos.getZ() + 0.5 + dir.getStepZ() * 0.55;

                // 小火焰
                level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, dir.getStepX() * 0.03, 0.01, dir.getStepZ() * 0.03);
            }

            // 噼啪声
            if (random.nextFloat() < 0.02F) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.4F, 1.5F, false);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 0.2F;
    }
}