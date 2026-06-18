package com.mjzaymi.etherealvoid.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class NBTUtil {
    @SafeVarargs
    public static <T extends ICapabilitySerializable<CompoundTag>>ListTag listSerializeNBT(T ... list) {
        return listSerializeNBT(Arrays.asList(list));
    }
    public static <T extends ICapabilitySerializable<CompoundTag>> ListTag listSerializeNBT(List<T> list) {
        ListTag result = new ListTag();
        for (T t : list) {
            result.add(t.serializeNBT());
        }
        return result;
    }
    public static <T extends ICapabilitySerializable<CompoundTag>> List<T> listDeserializeNBT(ListTag list, Supplier<T> supplier) {
        List<T> result = new ArrayList<>();
        for (Tag tag : list) {
            T obj = supplier.get();
            obj.deserializeNBT((CompoundTag) tag);
            result.add(obj);
        }
        return result;
    }
}
