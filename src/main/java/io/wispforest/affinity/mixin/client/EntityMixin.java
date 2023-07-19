package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.component.AffinityComponents;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void injectGlowingColor(CallbackInfoReturnable<Integer> cir) {
        var color = ((Entity) (Object) this).getComponent(AffinityComponents.GLOWING_COLOR).color();
        if (color == null) return;

        cir.setReturnValue(color.getSignColor());
    }

}
