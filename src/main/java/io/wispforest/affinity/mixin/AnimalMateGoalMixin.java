package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.ChunkAethumComponent;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimalMateGoal.class)
public class AnimalMateGoalMixin {

    @Shadow
    @Final
    protected AnimalEntity animal;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void cannotMateWhenTheWorldIsDying(CallbackInfoReturnable<Boolean> cir) {
        var component = animal.getWorld().getChunk(animal.getBlockPos()).getComponent(AffinityComponents.CHUNK_AETHUM);
        if (!component.isEffectActive(ChunkAethumComponent.INFERTILITY)) return;

        cir.setReturnValue(false);
    }

}
