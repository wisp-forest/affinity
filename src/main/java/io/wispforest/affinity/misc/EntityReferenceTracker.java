package io.wispforest.affinity.misc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;

import java.util.*;

public class EntityReferenceTracker {

    private static final Multimap<Entity, Releasable<?>> ENTITY_TO_REFERENCE = HashMultimap.create();

    public static <E extends Entity> Reference<Collection<E>> tracked(Collection<E> entities) {
        var ref = new EntityGroupReference<>(new ArrayList<>(entities));

        for (var entity : entities) {
            ENTITY_TO_REFERENCE.put(entity, ref);
        }

        return ref;
    }

    public static <E extends Entity> Reference<E> tracked(E entity) {
        var ref = new EntityReference<>(entity);
        ENTITY_TO_REFERENCE.put(entity, ref);
        return ref;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Entity> void releaseAll(E entity) {
        (ENTITY_TO_REFERENCE.containsKey(entity) ? (Collection<Releasable<E>>) (Object) ENTITY_TO_REFERENCE.get(entity) : Collections.<Releasable<E>>emptyList())
                .forEach(eReference -> eReference.release(entity));
    }

    private static class EntityReference<E extends Entity> implements Releasable<E>, Reference<E> {

        private E entity;

        private EntityReference(E entity) {
            this.entity = entity;
        }

        @Override
        public void release(E entity) {
            this.entity = null;
        }

        @Override
        public boolean present() {
            return entity != null;
        }

        @Override
        public E get() {
            return entity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntityReference<?> that = (EntityReference<?>) o;
            return Objects.equals(entity, that.entity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entity);
        }
    }

    private static class EntityGroupReference<E extends Entity> implements Releasable<E>, Reference<Collection<E>> {

        private final List<E> entities;
        private final List<E> view;

        private EntityGroupReference(List<E> entities) {
            this.entities = entities;
            this.view = Collections.unmodifiableList(entities);
        }

        @Override
        public void release(E entity) {
            this.entities.remove(entity);
        }

        @Override
        public boolean present() {
            return !this.entities.isEmpty();
        }

        @Override
        public Collection<E> get() {
            return view;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntityGroupReference<?> that = (EntityGroupReference<?>) o;
            return Objects.equals(entities, that.entities);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entities);
        }
    }

    public interface Reference<E> {
        E get();

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean present();
    }

    private interface Releasable<E extends Entity> {
        void release(E entity);
    }
}
