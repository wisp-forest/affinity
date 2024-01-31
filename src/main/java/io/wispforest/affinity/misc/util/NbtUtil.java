package io.wispforest.affinity.misc.util;

import io.wispforest.affinity.aethumflux.net.AethumLink;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class NbtUtil {

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

    public static void processBlockEntityNbt(ItemStack stack, BlockEntity blockEntity, Consumer<NbtCompound> sanitizer) {
        var nbt = blockEntity.createNbt();
        sanitizer.accept(nbt);

        BlockItem.setBlockEntityNbt(stack, blockEntity.getType(), nbt);

        // did I really just over-engineer the tooltip
        // when I was actually trying to remove the links when
        // pick-stacking aethum BEs? yes, yes I did
        //
        // glisco, 25.02.2023
        var loreList = new NbtList();
        loreList.add(NbtString.of(Text.Serialization.toJsonString(
                Text.empty().styled(style -> style.withItalic(false)).formatted(Formatting.DARK_GRAY)
                        .append(Text.literal("["))
                        .append(Text.literal("+").formatted(Formatting.GRAY))
                        .append(Text.literal("]"))
                        .append(Text.literal(" NBT").formatted(Formatting.GOLD))
        )));
        stack.getOrCreateSubNbt("display").put("Lore", loreList);
    }
}
