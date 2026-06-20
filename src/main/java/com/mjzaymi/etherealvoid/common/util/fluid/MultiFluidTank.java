package com.mjzaymi.etherealvoid.common.util.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class MultiFluidTank {

    protected final List<FluidStack> fluids = new ArrayList<>();
    protected int capacity;

    public MultiFluidTank(int capacity) {
        this.capacity = capacity;
    }

    public MultiFluidTank setCapacity(int capacity) {
        if (getFluidsAmount() > capacity) drainAll();
        this.capacity = capacity;
        return this;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<FluidStack> getFluids() {
        return fluids;
    }

    public int getFluidsTypeAmount() {
        return fluids.size();
    }

    public boolean containsType(FluidStack resource) {
        for (FluidStack fluid : fluids) {
            if (fluid.isFluidEqual(resource)) return true;
        }
        return false;
    }

    public int getFluidsAmount() {
        int amount = 0;
        for (FluidStack fluid : fluids) {
            amount += fluid.getAmount();
        }
        return amount;
    }

    public MultiFluidTank readFromNBT(CompoundTag nbt) {
        drainAll();
        setCapacity(nbt.getInt("capacity"));
        nbt.getList("fluids", Tag.TAG_COMPOUND).forEach(e -> {
            if (!(e instanceof CompoundTag tag)) return;
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag);
            fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        });
        return this;
    }

    public synchronized int drainAll() {
        int drained = getFluidsAmount();
        fluids.clear();
        return drained;
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putInt("capacity", capacity);
        ListTag list = new ListTag();
        for (FluidStack fluid : fluids) {
            CompoundTag tag = new CompoundTag();
            fluid.writeToNBT(tag);
            list.add(tag);
        }
        nbt.put("fluids", list);
        return nbt;
    }

    public synchronized int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        int space = getSpace();
        if (resource.isEmpty()) return 0;
        if (action.simulate()) return Math.min(space, resource.getAmount());
        for (FluidStack fluid : fluids) {
            if (!fluid.isFluidEqual(resource)) continue;
            int filled = space;
            if (resource.getAmount() < space) {
                fluid.grow(resource.getAmount());
                filled = resource.getAmount();
            } else {
                fluid.grow(space);
            }
            return filled;
        }
        FluidStack fluid = new FluidStack(resource, Math.min(space, resource.getAmount()));
        fluids.add(fluid);
        onContentsChanged();
        return fluid.getAmount();
    }

    public synchronized FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        for (int i=0;i<fluids.size();i++) {
            FluidStack fluid = fluids.get(i);
            if (!fluid.isFluidEqual(resource)) continue;

            int drained = resource.getAmount();
            if (fluid.getAmount() < drained) {
                drained = fluid.getAmount();
            }
            FluidStack stack = new FluidStack(fluid, drained);
            if (action.execute() && drained > 0) {
                fluid.shrink(drained);
                onContentsChanged();
            }
            if (fluid.getAmount()<=0) fluids.remove(i);
            return stack;
        }
        return FluidStack.EMPTY;
    }

    protected void onContentsChanged() {

    }

    public boolean isEmpty() {
        return fluids.isEmpty();
    }

    public int getSpace() {
        return Math.max(0, capacity - getFluidsAmount());
    }

    public MultiFluidTank copy() {
        var copy = new MultiFluidTank(capacity);
        for (var fluidStack : fluids) copy.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        return copy;
    }

    public void copyFrom(MultiFluidTank tank) {
        setCapacity(tank.getCapacity());
        drainAll();
        for (var fluidStack : tank.getFluids()) fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
    }
}
