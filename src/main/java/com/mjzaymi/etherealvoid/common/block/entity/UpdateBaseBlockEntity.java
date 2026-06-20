package com.mjzaymi.etherealvoid.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class UpdateBaseBlockEntity extends BlockEntity {
    public UpdateBaseBlockEntity(BlockEntityType<?> block, BlockPos pos, BlockState state) {
        super(block, pos, state);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag!=null) load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithFullMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        var tag = pkt.getTag();
        if (tag != null) this.load(tag);
    }

    public void updateChangeState(boolean update) {
        updateChangeState(level, update);
    }

    public void updateChangeState(Level level, boolean update) {
        setChanged();
        if (level==null) return;
        level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        if (update) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }
}
