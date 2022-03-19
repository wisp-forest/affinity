package io.wispforest.affinity.misc;

import io.wispforest.affinity.misc.util.NbtUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class NbtKey<T> {

    protected final String key;
    protected final Type<T> type;

    public NbtKey(String key, Type<T> type) {
        this.key = key;
        this.type = type;
    }

    public T read(NbtCompound nbt) {
        return this.type.getter.apply(nbt, this.key);
    }

    public T readOr(NbtCompound nbt, T defaultValue) {
        return nbt.contains(this.key, this.type.nbtEquivalent) ? this.read(nbt) : defaultValue;
    }

    public void write(NbtCompound nbt, T value) {
        this.type.setter.accept(nbt, this.key, value);
    }

    public void delete(NbtCompound nbt) {
        nbt.remove(this.key);
    }

    public boolean isIn(NbtCompound nbt) {
        return nbt.contains(this.key, this.type.nbtEquivalent);
    }

    public boolean maybeIsIn(@Nullable NbtCompound nbt) {
        return nbt != null && nbt.contains(this.key, this.type.nbtEquivalent);
    }

    public static final class ListKey<T> extends NbtKey<NbtList> {

        private final Type<T> elementType;

        public ListKey(String key, Type<T> elementType) {
            super(key, null);
            this.elementType = elementType;
        }

        @Override
        public NbtList read(NbtCompound nbt) {
            return nbt.getList(this.key, this.elementType.nbtEquivalent);
        }

        @Override
        public NbtList readOr(NbtCompound nbt, NbtList defaultValue) {
            return nbt.contains(this.key, NbtElement.LIST_TYPE) ? this.read(nbt) : defaultValue;
        }

        @Override
        public void write(NbtCompound nbt, NbtList value) {
            nbt.put(this.key, value);
        }
    }

    public static class Type<T> {
        public static final Type<Byte> BYTE = new Type<>(NbtElement.BYTE_TYPE, NbtCompound::getByte, NbtCompound::putByte);
        public static final Type<Short> SHORT = new Type<>(NbtElement.SHORT_TYPE, NbtCompound::getShort, NbtCompound::putShort);
        public static final Type<Integer> INT = new Type<>(NbtElement.INT_TYPE, NbtCompound::getInt, NbtCompound::putInt);
        public static final Type<Long> LONG = new Type<>(NbtElement.LONG_TYPE, NbtCompound::getLong, NbtCompound::putLong);
        public static final Type<Float> FLOAT = new Type<>(NbtElement.FLOAT_TYPE, NbtCompound::getFloat, NbtCompound::putFloat);
        public static final Type<Double> DOUBLE = new Type<>(NbtElement.DOUBLE_TYPE, NbtCompound::getDouble, NbtCompound::putDouble);
        public static final Type<byte[]> BYTE_ARRAY = new Type<byte[]>(NbtElement.BYTE_ARRAY_TYPE, NbtCompound::getByteArray, NbtCompound::putByteArray);
        public static final Type<String> STRING = new Type<>(NbtElement.STRING_TYPE, NbtCompound::getString, NbtCompound::putString);
        public static final Type<NbtCompound> COMPOUND = new Type<>(NbtElement.COMPOUND_TYPE, NbtCompound::getCompound, NbtCompound::put);
        public static final Type<int[]> INT_ARRAY = new Type<>(NbtElement.INT_ARRAY_TYPE, NbtCompound::getIntArray, NbtCompound::putIntArray);
        public static final Type<long[]> LONG_ARRAY = new Type<>(NbtElement.LONG_ARRAY_TYPE, NbtCompound::getLongArray, NbtCompound::putLongArray);
        public static final Type<ItemStack> ITEM_STACK = new Type<>(NbtElement.COMPOUND_TYPE, NbtUtil::readItemStack, NbtUtil::writeItemStack);

        private final byte nbtEquivalent;
        private final BiFunction<NbtCompound, String, T> getter;
        private final TriConsumer<NbtCompound, String, T> setter;

        private Type(byte nbtEquivalent, BiFunction<NbtCompound, String, T> getter, TriConsumer<NbtCompound, String, T> setter) {
            this.nbtEquivalent = nbtEquivalent;
            this.getter = getter;
            this.setter = setter;
        }
    }

}