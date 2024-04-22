package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.quack.AffinityExplosionExtension;
import io.wispforest.affinity.misc.quack.AffinityWorldExtension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin implements AffinityWorldExtension {

    @Unique
    private boolean nextExplosionNoDrops = false;

    @Shadow
    public abstract WorldChunk getWorldChunk(BlockPos pos);

    @Redirect(method = "hasRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isRaining()Z"))
    private boolean useLocalWeather(World instance, BlockPos pos) {
        if (instance.isClient) {return instance.isRaining();}

        var chunk = getWorldChunk(pos);

        if (chunk instanceof EmptyChunk) {
            return instance.isRaining();
        }

        var component = chunk.getComponent(AffinityComponents.LOCAL_WEATHER);

        return component.getRainGradient() > 0.2;
    }

    @Override
    public void affinity$markNextExplosionNoDrops() {
        this.nextExplosionNoDrops = true;
    }

    @Inject(
            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;collectBlocksAndDamageEntities()V")
    )
    private void makeExplosionInertForEntities(Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, boolean particles, CallbackInfoReturnable<Explosion> cir, @Local Explosion explosion) {
        if (!this.nextExplosionNoDrops) return;

        this.nextExplosionNoDrops = false;
        ((AffinityExplosionExtension) explosion).affinity$markNoEntityDrops();
    }
}
