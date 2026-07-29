package com.mjzaymi.etherealvoid.multiblock.hugeelectrodeplate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class HugeElectrodePlateSavedData extends SavedData {
    private static final String FILE_NAME = "ethereal_void_huge_electrode_plates";

    public enum StructureState {
        NONE,           // 未成型
        FORMED,         // 完美成型
        INTERFERED,     // 错误连接无法成型 (5x5x5有干扰)
        TANK_CONFLICT   // 无法组装！属于水槽的一部分
    }

    private final Map<BlockPos, StructureState> trackedStructures = new HashMap<>();

    public Map<BlockPos, StructureState> getTrackedStructures() {
        return trackedStructures;
    }

    public static HugeElectrodePlateSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                HugeElectrodePlateSavedData::load,
                HugeElectrodePlateSavedData::new,
                FILE_NAME
        );
    }

    public static HugeElectrodePlateSavedData load(CompoundTag tag) {
        HugeElectrodePlateSavedData data = new HugeElectrodePlateSavedData();
        ListTag list = tag.getList("structures", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BlockPos pos = BlockPos.of(entry.getLong("pos"));
            StructureState state = StructureState.valueOf(entry.getString("state"));
            data.trackedStructures.put(pos, state);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, StructureState> entry : trackedStructures.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("pos", entry.getKey().asLong());
            entryTag.putString("state", entry.getValue().name());
            list.add(entryTag);
        }
        tag.put("structures", list);
        return tag;
    }
}