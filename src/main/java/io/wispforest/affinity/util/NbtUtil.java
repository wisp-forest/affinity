package io.wispforest.affinity.util;

import io.wispforest.affinity.aethumflux.net.AethumLink;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class NbtUtil {

    public static void writeBlockPosList(NbtCompound nbt, String key, List<BlockPos> set) {
        var posArray = new long[set.size()];

        int idx = 0;
        for (var pos : set) {
            posArray[idx] = pos.asLong();
            idx++;
        }

        nbt.putLongArray(key, posArray);
    }

    public static void readBlockPosList(NbtCompound nbt, String key, List<BlockPos> set) {
        set.clear();

        for (var pos : nbt.getLongArray(key)) {
            set.add(BlockPos.fromLong(pos));
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

        for (NbtElement element : nbtList) {
            var stackNbt = (NbtCompound) element;
            byte idx = stackNbt.getByte("Slot");

            if (idx >= 0 && idx < items.size()) items.set(idx, ItemStack.fromNbt(stackNbt));
        }
    }

    private static final long LINK_TYPE_MASK = ~(0xFFL << 56);

    public static void readLinks(NbtCompound nbt, String key, Map<BlockPos, AethumLink.Type> links) {
        links.clear();

        for (var link : nbt.getLongArray(key)) {
            long pos = link & LINK_TYPE_MASK;
            byte type = (byte) (link >> 56);

            links.put(BlockPos.fromLong(pos), AethumLink.Type.values()[type]);
        }
    }

    public static void writeLinks(NbtCompound nbt, String key, Map<BlockPos, AethumLink.Type> links) {
        var members = new long[links.size()];

        var idx = new AtomicInteger(-1);
        links.forEach((blockPos, type) -> {
            members[idx.incrementAndGet()] = blockPos.asLong() | ((long) type.ordinal() << 56);
        });

        nbt.putLongArray(key, members);
    }
}