package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.enchantment.impl.GravecallerEnchantment;
import io.wispforest.affinity.entity.goal.AttackWithMasterGoal;
import io.wispforest.affinity.entity.goal.TrackMasterAttackerGoal;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    @Shadow
    @Final
    protected GoalSelector targetSelector;

    @Shadow
    private @Nullable LivingEntity target;

    @Shadow
    public abstract void setTarget(@Nullable LivingEntity target);

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectGravecallerGoals(CallbackInfo ci) {
        if (this.getGroup() != EntityGroup.UNDEAD) return;

        final var mob = (MobEntity) (Object) this;
        this.targetSelector.add(-2, new TrackMasterAttackerGoal(mob));
        this.targetSelector.add(-1, new AttackWithMasterGoal(mob));
    }

    @Inject(method = "tickNewAi", at = @At("TAIL"))
    private void afterTick(CallbackInfo ci) {
        if (this.target == null) return;

        if (GravecallerEnchantment.isMaster(this, this.target) ||
                AffinityEntityAddon.haveEqualData(this, this.target, GravecallerEnchantment.MASTER_KEY)) {
            this.setTarget(null);
        }
    }

    @ModifyVariable(method = "tryAttack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;getFireAspect(Lnet/minecraft/entity/LivingEntity;)I"),
            ordinal = 0)
    private float applyExtraAttackDamage(float amount, Entity entity) {
        return MixinHooks.getExtraAttackDamage(this, entity, amount);
    }

    @Redirect(method = "isAffectedByDaylight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isDay()Z"))
    private boolean iNeedMixinExtrasSoHardRn(World world) {
        if (world.isClient || world.getDimension().hasFixedTime())
            return world.isDay();

        var chunk = world.getWorldChunk(getBlockPos());

        if (chunk instanceof EmptyChunk) {
            return world.isDay();
        }

        var component = AffinityComponents.LOCAL_WEATHER.get(chunk);

        return component.getAmbientDarkness() < 4;
    }
}
