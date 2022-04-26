package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.AffinityEntityAddon;
import io.wispforest.affinity.misc.EntityReferenceTracker;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Entity.class)
public class EntityMixin implements AffinityEntityAddon {

    @Unique
    private Map<DataKey<?>, Object> affinity$dataStorage = null;

    @Inject(method = "onRemoved", at = @At("TAIL"))
    private void hookRemove(CallbackInfo ci) {
        EntityReferenceTracker.releaseAll((Entity) (Object) this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getData(DataKey<V> key) {
        return (V) affinity$getStorage().getOrDefault(key, key.defaultValue);
    }

    @Override
    public <V> void setData(DataKey<V> key, V value) {
        affinity$getStorage().put(key, value);
    }

    @Unique
    private Map<DataKey<?>, Object> affinity$getStorage() {
        if (this.affinity$dataStorage == null) this.affinity$dataStorage = new HashMap<>();
        return this.affinity$dataStorage;
    }
}
