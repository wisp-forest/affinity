package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.enchantment.GravecallerEnchantmentLogic;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MobSpawnerLogic.class)
public class MobSpawnerLogicMixin {

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;playSpawnEffects()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onEntitySpawn(ServerWorld world, BlockPos pos, CallbackInfo ci, @Local Entity entity) {
        if (!((Object) this instanceof GravecallerEnchantmentLogic.SpawnerLogic)) return;

        ((MobEntity) entity).addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(AffinityStatusEffects.IMPENDING_DOOM), 3000));
        ((LivingEntity) entity).getEquippedItems().forEach(stack -> stack.setCount(0));

        if (entity instanceof ZombieEntity zombie) {
            zombie.setCanPickUpLoot(true);
        }

        entity.getComponent(AffinityComponents.ENTITY_FLAGS).setFlag(EntityFlagComponent.NO_DROPS);
    }

}
