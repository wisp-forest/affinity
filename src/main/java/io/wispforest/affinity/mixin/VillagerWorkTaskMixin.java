package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import net.minecraft.entity.ai.brain.task.VillagerWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerWorkTask.class)
public class VillagerWorkTaskMixin {

    @Inject(method = "shouldRun(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/VillagerEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/Brain;getOptionalRegisteredMemory(Lnet/minecraft/entity/ai/brain/MemoryModuleType;)Ljava/util/Optional;"), cancellable = true)
    private void villagersWithoutArmsCannotRestock(ServerWorld serverWorld, VillagerEntity villagerEntity, CallbackInfoReturnable<Boolean> cir) {
        if (AffinityComponents.ENTITY_FLAGS.get(villagerEntity).hasFlag(EntityFlagComponent.VILLAGER_HAS_NO_ARMS)) {
            cir.setReturnValue(false);
        }
    }

}
