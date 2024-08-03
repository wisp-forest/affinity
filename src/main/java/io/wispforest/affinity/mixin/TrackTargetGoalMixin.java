package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrackTargetGoal.class)
public abstract class TrackTargetGoalMixin extends Goal {

    @Shadow
    @Final
    protected MobEntity mob;

    @Shadow
    @Nullable
    protected LivingEntity target;

    @Inject(method = "shouldContinue", at = @At("TAIL"), cancellable = true)
    private void cancelIfUndeadAndHasMaster(CallbackInfoReturnable<Boolean> cir) {
        if (this.target == null) return;

        // TODO: fix this when gravecaller is ported.

//        if (GravecallerEnchantment.isMaster(this.mob, this.target) ||
//                AffinityEntityAddon.haveEqualData(this.mob, this.target, GravecallerEnchantment.MASTER_KEY)) {
//            cir.setReturnValue(false);
//        }
    }

}
