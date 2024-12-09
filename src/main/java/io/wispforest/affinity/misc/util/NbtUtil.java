package io.wispforest.affinity.misc.util;

import io.wispforest.affinity.aethumflux.net.AethumLink;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class NbtUtil {

    public static void writeItemStackList(NbtCompound nbt, String key, DefaultedList<ItemStack> items, RegistryWrapper.WrapperLookup registries) {
        final var nbtList = new NbtList();

        for (int i = 0; i < items.size(); i++) {
            var stackNbt = new NbtCompound();
            stackNbt = (NbtCompound) ItemStack.OPTIONAL_CODEC.encode(items.get(i), registries.getOps(NbtOps.INSTANCE), stackNbt).getOrThrow();
            stackNbt.putByte("Slot", (byte) i);
            nbtList.add(stackNbt);
        }

        nbt.put(key, nbtList);
    }

    public static void readItemStackList(NbtCompound nbt, String key, DefaultedList<ItemStack> items, RegistryWrapper.WrapperLookup registries) {
        final var nbtList = nbt.getList(key, NbtElement.COMPOUND_TYPE);
        items.clear();

        for (NbtElement element : nbtList) {
            var stackNbt = (NbtCompound) element;
            byte idx = stackNbt.contains("Slot", NbtElement.BYTE_TYPE) ? stackNbt.getByte("Slot") : -1;

            if (stackNbt.contains("id") && idx >= 0 && idx < items.size()) {
                items.set(idx, ItemStack.fromNbtOrEmpty(registries, stackNbt));
            }
        }
    }

    public static void readLinks(NbtCompound nbt, String key, Map<BlockPos, AethumLink.Type> links) {
        links.clear();

        for (var element : nbt.getList(key, NbtElement.COMPOUND_TYPE)) {
            var linkData = (NbtCompound) element;

            long pos = linkData.getLong("Target");
            byte type = linkData.getByte("Type");

            links.put(BlockPos.fromLong(pos), AethumLink.Type.values()[type]);
        }
    }

    public static void writeLinks(NbtCompound nbt, String key, Map<BlockPos, AethumLink.Type> links) {
        var members = new NbtList();

        var idx = new AtomicInteger(-1);
        links.forEach((blockPos, type) -> {
            var linkData = new NbtCompound();
            linkData.putLong("Target", blockPos.asLong());
            linkData.putByte("Type", (byte) type.ordinal());
            members.add(linkData);
        });

        nbt.put(key, members);
    }
}
