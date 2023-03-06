package io.wispforest.affinity.misc;

import com.google.common.collect.ImmutableList;
import io.wispforest.affinity.misc.util.CollectionUtil;
import net.minecraft.entity.Entity;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class EntityReference<E> {

    private static final ThreadLocal<Map<Entity, List<Releasable<?>>>> ENTITY_TO_REFERENCE = ThreadLocal.withInitial(WeakHashMap::new);

    // ---------

    public abstract void drop();

    public abstract E get();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean present();

    public void consume(Consumer<E> action) {
        if (this.present()) {
            action.accept(this.get());
            this.drop();
        }
    }

    // ---------

    public static <E extends Entity> EntityReference<Collection<E>> of(Collection<E> entities) {
        var ref = new EntityGroupReference<>(new ArrayList<>(entities));

        for (var entity : entities) {
            CollectionUtil.getOrAddList(ENTITY_TO_REFERENCE.get(), entity).add(ref);
        }

        return ref;
    }

    public static <E extends Entity> EntityReference<E> of(E entity) {
        var ref = new SingleEntityReference<>(entity);
        CollectionUtil.getOrAddList(ENTITY_TO_REFERENCE.get(), entity).add(ref);
        return ref;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Entity> void dropAll(E entity) {
        final var refs = (List<Releasable<E>>) (Object) ENTITY_TO_REFERENCE.get().get(entity);
        if (refs != null) {
            refs.forEach(releasable -> releasable.release(entity));
            if (refs.isEmpty()) ENTITY_TO_REFERENCE.get().remove(entity);
        }
    }

    // ---------

    private static class SingleEntityReference<E extends Entity> extends EntityReference<E> implements Releasable<E> {

        private final WeakReference<E> ref;

        private SingleEntityReference(E entity) {
            this.ref = new WeakReference<>(entity);
        }

        @Override
        public void release(E entity) {
            this.ref.clear();
        }

        @Override
        public boolean present() {
            return this.ref.get() != null;
        }

        @Override
        public void drop() {
            this.ref.clear();
        }

        @Override
        public E get() {
            return this.ref.get();
        }
    }

    private static class EntityGroupReference<E extends Entity> extends EntityReference<Collection<E>> implements Releasable<E> {

        private final List<WeakReference<E>> refList;

        private EntityGroupReference(List<E> entities) {
            this.refList = entities.stream().map(WeakReference::new).collect(Collectors.toList());
        }

        @Override
        public void release(E entity) {
            this.refList.removeIf(eWeakReference -> eWeakReference.get() == entity);
        }

        @Override
        public boolean present() {
            return !this.refList.isEmpty();
        }

        @Override
        public void drop() {
            this.refList.forEach(WeakReference::clear);
            this.refList.clear();
        }

        @Override
        public Collection<E> get() {
            final var list = new ImmutableList.Builder<E>();
            refList.forEach(eWeakReference -> {
                final var entity = eWeakReference.get();
                if (entity != null) list.add(entity);
            });
            return list.build();
        }
    }

    private interface Releasable<E> {
        void release(E entity);
    }
}
