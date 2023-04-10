package io.wispforest.affinity.misc;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.item.ItemStack;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class SingleStackStorageProvider extends SingleStackStorage {

    public final Supplier<ItemStack> getter;
    public final Consumer<ItemStack> setter;
    private final Runnable commitCallback;

    private int capacity;
    private BooleanSupplier active = () -> true;
    private Predicate<ItemVariant> canInsert = variant -> true;
    private Predicate<ItemVariant> canExtract = variant -> true;

    public SingleStackStorageProvider(Supplier<ItemStack> getter, Consumer<ItemStack> setter, Runnable commitCallback) {
        this.getter = getter;
        this.setter = setter;
        this.commitCallback = commitCallback;
    }

    public SingleStackStorageProvider capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public SingleStackStorageProvider active(BooleanSupplier active) {
        this.active = active;
        return this;
    }

    public SingleStackStorageProvider canInsert(Predicate<ItemVariant> canInsert) {
        this.canInsert = canInsert;
        return this;
    }

    public BooleanSupplier active() {
        return active;
    }

    public Predicate<ItemVariant> canInsert() {
        return canInsert;
    }

    public Predicate<ItemVariant> canExtract() {
        return canExtract;
    }

    @Override
    protected boolean canInsert(ItemVariant itemVariant) {
        return this.active.getAsBoolean() && this.canInsert.test(itemVariant);
    }

    @Override
    protected boolean canExtract(ItemVariant itemVariant) {
        return this.active.getAsBoolean() && this.canExtract.test(itemVariant);
    }

    @Override
    protected int getCapacity(ItemVariant itemVariant) {
        return this.capacity;
    }

    @Override
    public long getCapacity() {
        return Math.max(this.getter.get().getCount(), this.capacity);
    }

    @Override
    protected ItemStack getStack() {
        return this.getter.get();
    }

    @Override
    protected void setStack(ItemStack stack) {
        this.setter.accept(stack);
    }

    @Override
    protected void onFinalCommit() {
        this.commitCallback.run();
    }
}
