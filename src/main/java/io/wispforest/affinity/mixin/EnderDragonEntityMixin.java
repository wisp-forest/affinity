package io.wispforest.affinity.mixin;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin extends MobEntity {
    @Shadow public int ticksSinceDeath;

    protected EnderDragonEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "updatePostDeath", at = @At("HEAD"))
    protected void dropDragonDrop(CallbackInfo ci) {
        if (this.world.isClient || this.ticksSinceDeath != 185) return;
        this.dropStack(AffinityItems.DRAGON_DROP.getDefaultStack());
    }
}
