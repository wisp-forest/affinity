package io.wispforest.affinity.misc;

import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NbtQuery<T> {

    private final List<Function<NbtElement, ?>> steps;

    private NbtQuery(List<Function<NbtElement, ?>> steps) {
        this.steps = steps;
    }

    public T getOr(@Nullable NbtElement nbt, T defaultValue) {
        var result = this.get(nbt);
        return result != null ? result : defaultValue;
    }

    public @Nullable <R> R getMapped(@Nullable NbtElement nbt, Function<T, R> mapper) {
        var result = this.get(nbt);
        return result != null ? mapper.apply(result) : null;
    }

    public @Nullable T get(@Nullable NbtElement nbt) {
        if (nbt == null) return null;

        Object value = nbt;
        for (var step : this.steps) {
            value = step.apply((NbtElement) value);
            if (value == null) return null;
        }

        return (T) value;
    }

    public static Builder begin() {
        return new Builder();
    }

    public static class Builder {
        private final List<Function<NbtElement, ?>> steps = new ArrayList<>();

        public Builder key(String key) {
            this.steps.add(nbtElement -> nbtElement instanceof NbtCompound compound ? compound.get(key) : null);
            return this;
        }

        public Builder nth(int n) {
            this.steps.add(nbtElement -> nbtElement instanceof NbtList list ? list.get(n) : null);
            return this;
        }

        public NbtQuery<Byte> i8() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtByte i8 ? i8.byteValue() : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<byte[]> i8Array() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtByteArray i8Array ? i8Array.getByteArray() : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<Short> i16() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtShort i16 ? i16.shortValue() : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<Integer> i32() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtInt i32 ? i32.intValue() : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<Integer> i32Array() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtIntArray i32Array ? i32Array.getIntArray() : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<Long> i64() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtLong i64 ? i64.longValue() : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<Long> i64Array() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtLongArray i64Array ? i64Array.getLongArray() : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<Long> f32() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtFloat f32 ? f32.floatValue() : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<Long> f64() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtDouble f64 ? f64.doubleValue() : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<String> string() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtString string ? string.asString() : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<NbtCompound> compound() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtCompound compound ? compound : null);
            return new NbtQuery<>(this.steps);
        }

        public NbtQuery<NbtList> list() {
            this.steps.add(nbtElement -> nbtElement instanceof NbtList list ? list : null);
            return new NbtQuery<>(this.steps);
        }
    }

}
