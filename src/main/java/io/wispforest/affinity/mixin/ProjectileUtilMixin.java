package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import net.minecraft.entity.projectile.ProjectileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {

    @ModifyArg(method = "raycast", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(D)Lnet/minecraft/util/math/Box;"))
    private static double increaseMargin(double value) {
        return value + MixinHooks.EXTRA_TARGETING_MARGIN;
    }

}
