package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.enchantment.impl.GravecallerEnchantment;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(MobSpawnerLogic.class)
public class MobSpawnerLogicMixin {

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;playSpawnEffects()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onEntitySpawn(ServerWorld world, BlockPos pos, CallbackInfo ci, boolean bl, Random random, MobSpawnerEntry mobSpawnerEntry, int i, NbtCompound nbtCompound, Optional optional, NbtList nbtList, int j, double d, double e, double f, BlockPos blockPos, Entity entity) {
        if (!((Object) this instanceof GravecallerEnchantment.SpawnerLogic)) return;

        ((MobEntity) entity).addStatusEffect(new StatusEffectInstance(AffinityStatusEffects.IMPENDING_DOOM, 3000));
        entity.getItemsEquipped().forEach(stack -> stack.setCount(0));

        if (entity instanceof ZombieEntity zombie) {
            zombie.setCanPickUpLoot(true);
        }

        entity.getComponent(AffinityComponents.ENTITY_FLAGS).setFlag(EntityFlagComponent.NO_DROPS);
    }

}
