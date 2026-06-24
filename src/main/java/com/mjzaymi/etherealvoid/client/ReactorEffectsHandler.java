package com.mjzaymi.etherealvoid.client;

import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber
public class ReactorEffectsHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        Level level = player.level();

        if (level.isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos pos = player.blockPosition();
        Optional<CuboidStructure> structure = CuboidStructure.findFromInterior(level, pos);

        if (structure.isEmpty()) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 0, true, false, true));
        if (player.tickCount % 20 == 0) player.hurt(level.damageSources().magic(), 8.0F);

        for (int i = 0; i < 20; i++) {
            double x = player.getX() + (level.random.nextDouble() - 0.5) * 2;
            double y = player.getY() + level.random.nextDouble() * 2;
            double z = player.getZ() + (level.random.nextDouble() - 0.5) * 2;
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}