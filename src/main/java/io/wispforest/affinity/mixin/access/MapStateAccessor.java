package io.wispforest.affinity.mixin.access;

import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapState.class)
public interface MapStateAccessor {

    @Mutable
    @Accessor("centerX")
    void affinity$setCenterX(int centerX);

    @Mutable
    @Accessor("centerZ")
    void affinity$setCenterZ(int centerZ);

}
