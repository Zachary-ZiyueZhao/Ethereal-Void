package com.mjzaymi.etherealvoid.block.entity;

import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class ReactionPoolBlockEntity extends BlockEntity {

    private CuboidStructure structure;

    private final FluidTank tank = new FluidTank(10000);

    public ReactionPoolBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTION_POOL_BE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        if (structure!=null) pTag.put("structure", structure.serializeNBT());
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        structure = CuboidStructure.deserializeNBT(pTag.getCompound("structure"));
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag!=null)
            load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        System.out.println(tag);
        return saveWithFullMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // 创建一个包含当前 BE 数据的 vanilla 数据包
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.load(tag); // 客户端加载新数据
        }
    }

    public FluidTank getTank() {
        return tank;
    }

    public void setStructure(CuboidStructure structure) {
        this.structure = structure;
        setChanged();
    }

    public CuboidStructure getStructure() {
        return structure;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {

        if (level.isClientSide) return;

        CuboidStructure structure = getStructure();
        if (structure == null) return;

        // 扫描内部
        for (BlockPos p : structure.interiors()) {

            BlockState s = level.getBlockState(p);

            FluidState fluid = s.getFluidState();
            if (!fluid.isEmpty()) {
                getTank().fill(
                        new FluidStack(fluid.getType(), 1000),
                        IFluidHandler.FluidAction.EXECUTE
                );

                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                continue;
            }

            if (!s.isAir()) {
                Block.dropResources(s, level, p);
                level.removeBlock(p, false);
            }
        }
    }
}