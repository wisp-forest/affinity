package io.wispforest.affinity.mixin.client;

import net.minecraft.client.particle.BlockFallingDustParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockFallingDustParticle.class)
public interface BlockFallingDustParticleInvoker {
    @Invoker("<init>")
    static BlockFallingDustParticle affinity$invokeNew(ClientWorld world, double x, double y, double z, float red, float green, float blue, SpriteProvider spriteProvider) {
        throw new UnsupportedOperationException();
    }
}
