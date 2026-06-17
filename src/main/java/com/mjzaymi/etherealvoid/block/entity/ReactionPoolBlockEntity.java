package com.mjzaymi.etherealvoid.block.entity;

import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.reactionpool.ReactionPoolProcessor;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import com.mjzaymi.etherealvoid.registration.ModFluids;
import com.mjzaymi.etherealvoid.util.fluid.MultiFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
            tank.setCapacity(0);
        } else {
            tank.setCapacity(structure.interiors().size() * 1000);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (structure==null)
            return super.getRenderBoundingBox();
        BlockPos min = getStructure().min();
        BlockPos max = getStructure().max();
        return new AABB(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0
        );
    }


    public CuboidStructure getStructure() {
        return structure;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {

        if (level.isClientSide) return;

        CuboidStructure structure = getStructure();
        if (structure == null) return;

        var realStructureOpt = CuboidStructure.findFromCorner(level, getBlockPos());
        if (realStructureOpt.isEmpty()) {
            setStructure(null);
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            return;
        } else if (!structure.isEqual(realStructureOpt.get())){
            setStructure(realStructureOpt.get());
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            return;
        }

        for (BlockPos p : structure.interiors()) {
            BlockState s = level.getBlockState(p);
            FluidState fluid = s.getFluidState();
            if (!fluid.isEmpty() && fluid.isSource()) {
                tank.fill(
                        new FluidStack(fluid.getType(), 1000),
                        IFluidHandler.FluidAction.EXECUTE
                );
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                setChanged();
                level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
                level.updateNeighborsAt(p, Blocks.AIR.defaultBlockState().getBlock());
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                continue;
            }

            if (!s.isAir()) {
                Block.dropResources(s, level, p);
                level.removeBlock(p, false);
            }
        }
        BlockPos min = structure.min();
        BlockPos max = structure.max();
        AABB area = new AABB(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D
        );
        var entities = level.getEntitiesOfClass(ItemEntity.class, area);


        //Map<String, ReactionPoolProcessor.PoolContents> pools = new HashMap<>();
        for (ItemEntity entity : entities) {
            if (!entity.isAlive() || entity.getItem().isEmpty()) {
                continue;
            }

            /*Optional<CuboidStructure> structure = CuboidStructure.findFromInterior(serverLevel, entity.blockPosition());
            if (structure.isEmpty()) {
                entity.setExtendedLifetime();
                entity.setDefaultPickUpDelay();
                entity.getPersistentData().remove(PROGRESS_TAG);
                continue;
            }

            entity.setNeverPickUp();
            entity.setUnlimitedLifetime();

            CuboidStructure pool = structure.get();
            pools.computeIfAbsent(key(pool), ignored -> new ReactionPoolProcessor.PoolContents(pool)).items.add(entity);*/
        }

        //for (ReactionPoolProcessor.PoolContents contents : pools.values()) {
        //    processReaction(serverLevel, contents);
        //}
    }
}