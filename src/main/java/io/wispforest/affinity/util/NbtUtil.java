package io.wispforest.affinity.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
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

    public static void writeItemStackList(NbtCompound nbt, String key, DefaultedList<ItemStack> items) {
        final var nbtList = new NbtList();

        for (int i = 0; i < items.size(); i++) {
            var stackNbt = new NbtCompound();
            stackNbt.putByte("Slot", (byte) i);
            items.get(i).writeNbt(stackNbt);
            nbtList.add(stackNbt);
        }

        nbt.put(key, nbtList);
    }

    public static void readItemStackList(NbtCompound nbt, String key, DefaultedList<ItemStack> items) {
        final var nbtList = nbt.getList(key, NbtElement.COMPOUND_TYPE);
        items.clear();

        for (int i = 0; i < nbtList.size(); i++) {
            var stackNbt =(NbtCompound) nbtList.get(i);
            byte idx = stackNbt.getByte("Slot");

            if (i > 0 && i < items.size()) items.set(idx, ItemStack.fromNbt(stackNbt));
        }
    }

}
