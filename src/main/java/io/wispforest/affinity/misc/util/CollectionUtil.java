package io.wispforest.affinity.misc.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class CollectionUtil {

    public static <K, V> Function<K, V> instance(Supplier<V> creator) {
        return k -> creator.get();
    }

    public static <K, E> List<E> getOrAddList(Map<K, List<E>> map, K key) {
        return map.computeIfAbsent(key, k -> new ArrayList<>());
    }

    public static <K, E> Set<E> getOrAddSet(Map<K, Set<E>> map, K key) {
        return map.computeIfAbsent(key, k -> new HashSet<>());
    }

}
