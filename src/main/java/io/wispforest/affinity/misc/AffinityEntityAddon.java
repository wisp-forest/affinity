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
        public final V defaultValue;

        private DataKey(V defaultValue) {
            this.defaultValue = defaultValue;
        }

        public static <V> DataKey<V> of(V defaultValue) {
            return new DataKey<>(defaultValue);
        }
    }

}

