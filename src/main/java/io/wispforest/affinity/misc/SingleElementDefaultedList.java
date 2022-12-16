package io.wispforest.affinity.misc;

import com.google.common.base.Preconditions;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SingleElementDefaultedList<E> extends DefaultedList<E> {

    private final Supplier<@NotNull E> getter;
    private final Consumer<@NotNull E> setter;
    private final @NotNull E initial;

    public SingleElementDefaultedList(@NotNull E initial, Supplier<@NotNull E> getter, Consumer<@NotNull E> setter) {
        super(Collections.singletonList(initial), initial);
        this.getter = getter;
        this.setter = setter;
        this.initial = initial;
    }

    @NotNull
    @Override
    public E get(int index) {
        Preconditions.checkElementIndex(index, 1);
        return this.getter.get();
    }

    @Override
    public E set(int index, E element) {
        Preconditions.checkElementIndex(index, 1);
        Preconditions.checkNotNull(element);

        var previous = this.getter.get();
        this.setter.accept(element);
        return previous;
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        this.setter.accept(this.initial);
    }

}
