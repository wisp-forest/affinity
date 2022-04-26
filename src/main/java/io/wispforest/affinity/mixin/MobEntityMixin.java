package io.wispforest.affinity.mixin;

import io.wispforest.affinity.entity.goal.AttackWithMasterGoal;
import io.wispforest.affinity.entity.goal.TrackMasterAttackerGoal;
import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    @Shadow
    @Final
    protected GoalSelector targetSelector;

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

    @ModifyVariable(method = "tryAttack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;getFireAspect(Lnet/minecraft/entity/LivingEntity;)I"),
            ordinal = 0)
    private float applyExtraAttackDamage(float amount, Entity entity) {
        return MixinHooks.getExtraAttackDamage(this, entity, amount);
    }

}
