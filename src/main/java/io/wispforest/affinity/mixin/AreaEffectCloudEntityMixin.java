package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.quack.ExtendedAreaEffectCloudEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(AreaEffectCloudEntity.class)
public class AreaEffectCloudEntityMixin implements ExtendedAreaEffectCloudEntity {
    @Shadow @Final private List<StatusEffectInstance> effects;
    @Shadow private Potion potion;
    private NbtCompound affinity$extraPotionNbt;

    public void affinity$setExtraPotionNbt(NbtCompound tag) {
        affinity$extraPotionNbt = tag;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), index = 0)
    private Object doPotionApplication(Object e) {
        var entity = (LivingEntity) e;

        potion.getEffects().forEach(x -> MixinHooks.tryInvokePotionApplied(x, entity, affinity$extraPotionNbt));
        effects.forEach(x -> MixinHooks.tryInvokePotionApplied(x, entity, affinity$extraPotionNbt));

        return e;
    }
}
