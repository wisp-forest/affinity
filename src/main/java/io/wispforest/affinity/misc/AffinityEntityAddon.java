package io.wispforest.affinity.misc;

import net.minecraft.entity.Entity;

public interface AffinityEntityAddon {

    <V> V getData(DataKey<V> key);

    <V> void setData(DataKey<V> key, V value);

    static <E extends Entity, V> V getData(E entity, DataKey<V> key) {
        return ((AffinityEntityAddon) entity).getData(key);
    }

    static <E extends Entity, V> void setData(E entity, DataKey<V> key, V value) {
        ((AffinityEntityAddon) entity).setData(key, value);
    }

    class DataKey<V> {
        private DataKey(Class<V> valueType) {}

        public static <V> DataKey<V> of(Class<V> valueType) {
            return new DataKey<>(valueType);
        }
    }

}

