package io.wispforest.affinity.mixin;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.ArtifactBladeItem;
import io.wispforest.affinity.misc.ArcaneFadeFluid;
import io.wispforest.affinity.misc.EntityReference;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(Entity.class)
public abstract class EntityMixin implements AffinityEntityAddon {

    @Shadow
    public abstract boolean updateMovementInFluid(TagKey<Fluid> tag, double speed);

    @Shadow
    protected boolean touchingWater;

    @Unique
    private static final TagKey<Fluid> ARCANE_FADE = TagKey.of(RegistryKeys.FLUID, Affinity.id("arcane_fade"));

    @Unique
    private Map<DataKey<?>, Object> affinity$dataStorage = null;

    @Unique
    private boolean affinity$touchingBleach = false;

    @Inject(method = "setRemoved", at = @At("TAIL"))
    private void hookRemove(CallbackInfo ci) {
        EntityReference.dropAll((Entity) (Object) this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getData(DataKey<V> key) {
        final var data = this.affinity$getStorage().get(key);
        return data != null
                ? (V) data
                : key.makeDefaultValue();
    }

    @Override
    public <V> void setData(DataKey<V> key, V value) {
        affinity$getStorage().put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V removeData(DataKey<V> key) {
        return hasData(key) ? (V) affinity$dataStorage.remove(key) : null;
    }

    @Override
    public <V> boolean hasData(DataKey<V> key) {
        return affinity$dataStorage != null && affinity$dataStorage.containsKey(key);
    }

    @Unique
    private Map<DataKey<?>, Object> affinity$getStorage() {
        if (this.affinity$dataStorage == null) this.affinity$dataStorage = new HashMap<>();
        return this.affinity$dataStorage;
    }

    @Inject(method = "getJumpVelocityMultiplier", at = @At("TAIL"), cancellable = true)
    protected void lessJumping(CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof PlayerEntity player)) return;

        var weapon = player.getMainHandStack();
        if (!(weapon.getItem() instanceof ArtifactBladeItem blade) || ArtifactBladeItem.getAbilityTicks(player.getWorld(), weapon) < 0 || blade.tier.ordinal() < 2) {
            return;
        }

        cir.setReturnValue(cir.getReturnValueF() * 2.5f);
    }

    @ModifyVariable(method = "updateWaterState", at = @At("LOAD"))
    protected boolean updateFadeState(boolean value) {
        boolean wasTouchingFade = this.affinity$touchingBleach;
        this.touchingWater |= this.affinity$touchingBleach = this.updateMovementInFluid(ARCANE_FADE, 0.014);

        if (this.affinity$touchingBleach && !wasTouchingFade) {
            ArcaneFadeFluid.ENTITY_TOUCH_EVENT.invoker().onTouch((Entity) (Object) this);
        }

        return value || this.touchingWater;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    protected void invokeFadeTickEvent(CallbackInfo ci) {
        if (!this.affinity$touchingBleach) return;
        ArcaneFadeFluid.ENTITY_TICK_IN_FADE_EVENT.invoker().onTouch((Entity) (Object) this);
    }
}
