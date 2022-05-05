package io.wispforest.affinity.mixin;

import io.wispforest.affinity.enchantment.impl.GravecallerEnchantment;
import io.wispforest.affinity.misc.AffinityEntityAddon;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ActiveTargetGoal.class)
public abstract class ActiveTargetGoalMixin extends TrackTargetGoal {

    @Shadow
    @Nullable
    protected LivingEntity targetEntity;

    private ActiveTargetGoalMixin(MobEntity mob, boolean checkVisibility) {
        super(mob, checkVisibility);
    }

    @Inject(method = "canStart", at = @At("TAIL"), cancellable = true)
    private void cancelIfUndeadAndHasMaster(CallbackInfoReturnable<Boolean> cir) {
        if (GravecallerEnchantment.isMaster(this.mob, this.targetEntity)
                || AffinityEntityAddon.haveEqualData(this.mob, this.targetEntity, GravecallerEnchantment.MASTER_KEY)) {
            this.targetEntity = null;
            cir.setReturnValue(false);
        }
    }
}
