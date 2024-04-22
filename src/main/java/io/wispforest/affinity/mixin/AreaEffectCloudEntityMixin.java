package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.misc.quack.ExtendedAreaEffectCloudEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(AreaEffectCloudEntity.class)
public class AreaEffectCloudEntityMixin implements ExtendedAreaEffectCloudEntity {

    @Shadow
    @Final
    private List<StatusEffectInstance> effects;

    @Shadow private Potion potion;

    private @Nullable NbtCompound affinity$extraPotionNbt;

    public void affinity$setExtraPotionNbt(NbtCompound tag) {
        affinity$extraPotionNbt = tag;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), index = 0)
    private Object doPotionApplication(Object e) {
        var entity = (LivingEntity) e;

        this.potion.getEffects().forEach(x -> MixinHooks.potionApplied(x, entity, affinity$extraPotionNbt));
        this.effects.forEach(x -> MixinHooks.potionApplied(x, entity, affinity$extraPotionNbt));

        return e;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;<init>(Lnet/minecraft/entity/effect/StatusEffect;IIZZ)V"), index = 1)
    private int changeDuration(int duration) {
        if (this.affinity$extraPotionNbt == null) return duration;
        duration *= this.affinity$extraPotionNbt.get(PotionMixture.EXTEND_DURATION_BY);
        return duration;
    }
}
