package com.mjzaymi.etherealvoid.block.entity;

import com.mjzaymi.etherealvoid.reactionpool.CuboidStructure;
import com.mjzaymi.etherealvoid.registration.ModBlockEntities;
import com.mjzaymi.etherealvoid.util.NBTUtil;
import com.mjzaymi.etherealvoid.util.fluid.MultiFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.*;

public class ReactionPoolBlockEntity extends BlockEntity {

    private CuboidStructure structure;

    private final MultiFluidTank tank = new MultiFluidTank(0);
    private final List<ItemStack> precipitates = new ArrayList<>();

    public ReactionPoolBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTION_POOL_BE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        if (structure!=null) pTag.put("structure", structure.serializeNBT());
        pTag.put("tank", tank.writeToNBT(new CompoundTag()));
        ListTag list = new ListTag();
        for (ItemStack itemStack : precipitates) list.add(itemStack.save(new CompoundTag()));
        pTag.put("precipitates", list);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        structure = CuboidStructure.deserializeNBT(pTag.getCompound("structure"));
        tank.readFromNBT(pTag.getCompound("tank"));
        synchronized (precipitates) {
            precipitates.clear();
            for (Tag t : pTag.getList("precipitates", Tag.TAG_COMPOUND)) {
                precipitates.add(ItemStack.of((CompoundTag) t));
            }
        }
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

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {

        if (pLevel.isClientSide) return;

        CuboidStructure structure = getStructure();
        if (structure == null) return;

        var realStructureOpt = CuboidStructure.findFromCorner(pLevel, getBlockPos());
        if (realStructureOpt.isEmpty()) {
            setStructure(null);
            setChanged(pLevel, pPos, pState);
            updateChangeState(true);
            return;
        } else if (!structure.isEqual(realStructureOpt.get())){
            setStructure(realStructureOpt.get());
            setChanged(pLevel, pPos, pState);
            updateChangeState(true);
            return;
        }

        for (BlockPos p : structure.interiors()) {
            BlockState s = pLevel.getBlockState(p);
            FluidState fluid = s.getFluidState();
            if (!fluid.isEmpty() && fluid.isSource()) {
                tank.fill(
                        new FluidStack(fluid.getType(), 1000),
                        IFluidHandler.FluidAction.EXECUTE
                );
                pLevel.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                setChanged(pLevel, pPos, pState);
                pLevel.updateNeighborsAt(p, Blocks.AIR.defaultBlockState().getBlock());
                updateChangeState(true);
                continue;
            }

            if (!s.isAir()) {
                Block.dropResources(s, pLevel, p);
                pLevel.removeBlock(p, false);
            }
        }
        BlockPos min = structure.min();
        BlockPos max = structure.max();
        AABB area = new AABB(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D
        );
        var entities = pLevel.getEntitiesOfClass(ItemEntity.class, area);
        final List<ItemStack> original = new ArrayList<>(precipitates);
        synchronized (precipitates) {
            precipitates.clear();


            //Map<String, ReactionPoolProcessor.PoolContents> pools = new HashMap<>();
            for (ItemEntity entity : entities) {
                if (!entity.isAlive() || entity.getItem().isEmpty()) {
                    continue;
                }
                precipitates.add(entity.getItem());

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
        }
        if (!precipitates.equals(original)) updateChangeState(true);

        //for (ReactionPoolProcessor.PoolContents contents : pools.values()) {
        //    processReaction(serverLevel, contents);
        //}
    }

    public void updateChangeState(boolean sendBlockUpdate) {
        setChanged();
        if (level!=null) {
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
            if (sendBlockUpdate) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public List<ItemStack> getPrecipitates() {
        return precipitates;
    }
}