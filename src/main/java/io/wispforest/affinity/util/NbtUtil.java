package io.wispforest.affinity.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class NbtUtil {

    public static void writeBlockPosList(NbtCompound nbt, String key, List<BlockPos> list) {
        var members = new long[list.size()];

        for (int i = 0; i < list.size(); i++) {
            members[i] = list.get(i).asLong();
        }

        nbt.putLongArray(key, members);
    }

    public static void readBlockPosList(NbtCompound nbt, String key, List<BlockPos> list) {
        list.clear();

        for (var pos : nbt.getLongArray(key)) {
            list.add(BlockPos.fromLong(pos));
        }
    }

}
