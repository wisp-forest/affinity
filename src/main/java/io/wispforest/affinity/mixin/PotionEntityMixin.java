package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionUtil;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.misc.quack.ExtendedAreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity {

    @Unique
    private static ItemStack affinity$stackContext = null;

    public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "applySplashPotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/PotionEntity;squaredDistanceTo(Lnet/minecraft/entity/Entity;)D"))
    private Entity doPotionApplication(Entity entity) {
        if (!(entity instanceof LivingEntity target)) return entity;

        var stack = this.getStack();
        PotionUtil.getPotionEffects(stack).forEach(x -> MixinHooks.potionApplied(x, target, stack.getComponents()));

        return entity;
    }

    @Inject(method = "applySplashPotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;mapDuration(Lit/unimi/dsi/fastutil/ints/Int2IntFunction;)I"))
    private void captureStackContext(Iterable<StatusEffectInstance> effects, @Nullable Entity entity, CallbackInfo ci) {
        affinity$stackContext = this.getStack();
    }

    @ModifyVariable(method = "method_48575", at = @At(value = "HEAD"), argsOnly = true)
    private static double extendDuration(double duration) {
        duration *= affinity$stackContext.getOrDefault(PotionMixture.EXTEND_DURATION_BY, 1f);

        affinity$stackContext = null;
        return duration;
    }

    @ModifyArg(method = "applyLingeringPotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private Entity addExtraData(Entity entity) {
        var stack = this.getStack();

        ((ExtendedAreaEffectCloudEntity) entity).affinity$setExtraPotionData(stack.getComponents());

        return entity;
    }
}
