package io.wispforest.affinity.mixin.access;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ParticleManager.class)
public interface ParticleManagerInvoker {

    @Invoker("createParticle")
    <T extends ParticleEffect> Particle affinity$createParticle(T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ);

}
