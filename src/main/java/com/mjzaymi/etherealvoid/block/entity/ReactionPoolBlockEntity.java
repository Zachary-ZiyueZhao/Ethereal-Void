package com.mjzaymi.etherealvoid.block.entity;

import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import com.mjzaymi.etherealvoid.registration.ModFluids;
import com.mjzaymi.etherealvoid.util.fluid.MultiFluidTank;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class ReactionPoolBlockEntity extends BlockEntity {

    private CuboidStructure structure;

    private final MultiFluidTank tank = new MultiFluidTank(0);

    public ReactionPoolBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTION_POOL_BE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        if (structure!=null) pTag.put("structure", structure.serializeNBT());
        pTag.put("tank", tank.writeToNBT(new CompoundTag()));
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        structure = CuboidStructure.deserializeNBT(pTag.getCompound("structure"));
        tank.readFromNBT(pTag.getCompound("tank"));
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
        CompoundTag tag = pkt.getTag();
        if (tag != null) this.load(tag);
    }

    public MultiFluidTank getTank() {
        return tank;
    }

    public void setStructure(CuboidStructure structure) {
        this.structure = structure;
        if (structure==null) {
            tank.drainAll();
        } else {
            tank.setCapacity(structure.interiors().size() * 1000);
            //tank.fill(new FluidStack(Fluids.WATER, 5000), IFluidHandler.FluidAction.EXECUTE);
            //tank.fill(new FluidStack(ModFluids.SOURCE_SOAP_WATER.get(), 3000), IFluidHandler.FluidAction.EXECUTE);
            //tank.fill(new FluidStack(Fluids.FLOWING_WATER, 2000), IFluidHandler.FluidAction.EXECUTE);
            //tank.fill(new FluidStack(Fluids.LAVA, 3000), IFluidHandler.FluidAction.EXECUTE);
            //tank.fill(new FluidStack(Fluids.FLOWING_LAVA, 2000), IFluidHandler.FluidAction.EXECUTE);
        }
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