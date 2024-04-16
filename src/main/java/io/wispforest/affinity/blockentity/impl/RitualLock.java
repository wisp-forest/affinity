package io.wispforest.affinity.blockentity.impl;

import org.jetbrains.annotations.Nullable;

public class RitualLock<T> {

    @Nullable private T holder = null;

    public boolean isHeld() {
        return this.holder != null;
    }

    public boolean acquire(T holder) {
        if (this.holder != null) return false;
        this.holder = holder;
        return true;
    }

    public boolean release() {
        boolean wasLocked = this.holder != null;
        this.holder = null;
        return wasLocked;
    }

    public @Nullable T holder() {
        return this.holder;
    }

}
