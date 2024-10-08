package io.wispforest.affinity.mixin.access;

import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {

    @Mutable
    @Accessor("x")
    void affinity$setX(int x);

    @Mutable
    @Accessor("y")
    void affinity$setY(int y);
}
