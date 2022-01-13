package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.util.components.AffinityComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void injectGlowingColor(CallbackInfoReturnable<Integer> cir) {
        if (!((Object) this instanceof PlayerEntity player)) return;

        var color = AffinityComponents.GLOWING_COLOR.get(player).getColor();
        if (color == null) return;

        cir.setReturnValue(color.getSignColor());
    }

}
