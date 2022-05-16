package io.wispforest.affinity.misc.quack;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public interface AffinityEntityAddon {

    <V> V getData(DataKey<V> key);

    <V> void setData(DataKey<V> key, V value);

    <V> V removeData(DataKey<V> key);

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

    static <E extends Entity, V> void createDefaultData(E entity, DataKey<V> key) {
        ((AffinityEntityAddon) entity).setData(key, key.makeDefaultValue());
    }

    static <E extends Entity, V> void setData(E entity, DataKey<V> key, V value) {
        ((AffinityEntityAddon) entity).setData(key, value);
    }

    static <E extends Entity, V> V removeData(E entity, DataKey<V> key) {
        return ((AffinityEntityAddon) entity).removeData(key);
    }

    static <E extends Entity, V> boolean hasData(E entity, DataKey<V> key) {
        return ((AffinityEntityAddon) entity).hasData(key);
    }

    static <E1 extends Entity, E2 extends Entity, V> boolean haveEqualData(@Nullable E1 entity1, @Nullable E2 entity2, DataKey<V> key) {
        if (entity1 == null || entity2 == null) return false;
        return hasData(entity1, key) && hasData(entity2, key) && Objects.equals(getData(entity1, key), getData(entity2, key));
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

