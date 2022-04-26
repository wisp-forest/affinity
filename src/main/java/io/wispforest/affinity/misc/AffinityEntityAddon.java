package io.wispforest.affinity.misc;

import net.minecraft.entity.Entity;

import java.util.function.Supplier;

public interface AffinityEntityAddon {

    <V> V getData(DataKey<V> key);

    <V> void setData(DataKey<V> key, V value);

    <V> boolean hasData(DataKey<V> key);

    static <E extends Entity, V> V getData(E entity, DataKey<V> key) {
        return ((AffinityEntityAddon) entity).getData(key);
    }

    static <E extends Entity, V> V getDataOrSetDefault(E entity, DataKey<V> key) {
        final var addon = (AffinityEntityAddon) entity;

        if (!addon.hasData(key)) {
            addon.setData(key, key.makeDefaultValue());
        }

        return addon.getData(key);
    }

    static <E extends Entity, V> void setData(E entity, DataKey<V> key, V value) {
        ((AffinityEntityAddon) entity).setData(key, value);
    }

    static <E extends Entity, V> boolean hasData(E entity, DataKey<V> key) {
        return ((AffinityEntityAddon) entity).hasData(key);
    }

    class DataKey<V> {
        private final Supplier<V> defaultValueFactory;

        private DataKey(Supplier<V> defaultValueFactory) {
            this.defaultValueFactory = defaultValueFactory;
        }

        public static <V> DataKey<V> withDefaultFactory(Supplier<V> defaultValueFactory) {
            return new DataKey<>(defaultValueFactory);
        }

        public static <V> DataKey<V> withDefaultConstant(V constantDefaultValue) {
            return new DataKey<>(() -> constantDefaultValue);
        }

        public static <V> DataKey<V> withNullDefault() {
            return new DataKey<>(() -> null);
        }

        public V makeDefaultValue() {
            return defaultValueFactory.get();
        }
    }

}

