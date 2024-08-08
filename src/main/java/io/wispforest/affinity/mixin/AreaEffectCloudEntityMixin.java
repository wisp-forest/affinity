package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.misc.quack.ExtendedAreaEffectCloudEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(AreaEffectCloudEntity.class)
public class AreaEffectCloudEntityMixin implements ExtendedAreaEffectCloudEntity {

    @Shadow private PotionContentsComponent potionContentsComponent;
    @Unique private @Nullable ComponentMap affinity$extraPotionData;

    public void affinity$setExtraPotionData(ComponentMap data) {
        affinity$extraPotionData = data;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), index = 0)
    private Object doPotionApplication(Object e) {
        var entity = (LivingEntity) e;

        potionContentsComponent.forEachEffect(x -> MixinHooks.potionApplied(x, entity, affinity$extraPotionData));

        return e;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;<init>(Lnet/minecraft/registry/entry/RegistryEntry;IIZZ)V"), index = 1)
    private int changeDuration(int duration) {
        if (this.affinity$extraPotionData == null) return duration;
        duration *= this.affinity$extraPotionData.getOrDefault(PotionMixture.EXTEND_DURATION_BY, 1f);
        return duration;
    }
}
