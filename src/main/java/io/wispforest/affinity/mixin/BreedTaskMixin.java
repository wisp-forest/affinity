package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.ChunkAethumComponent;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreedTask.class)
public class BreedTaskMixin {

    @Inject(method = "shouldRun(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/AnimalEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void cannotMateWhenTheWorldIsDying(ServerWorld serverWorld, AnimalEntity animal, CallbackInfoReturnable<Boolean> cir) {
        var component = AffinityComponents.CHUNK_AETHUM.get(animal.getWorld().getChunk(animal.getBlockPos()));
        if (!component.hasEffectActive(ChunkAethumComponent.INFERTILITY)) return;

        cir.setReturnValue(false);
    }

}
