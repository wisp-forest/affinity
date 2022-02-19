package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.util.MathUtil;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.particle.CrackParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrackParticle.class)
public abstract class CrackParticleMixin extends Particle {

    protected CrackParticleMixin(ClientWorld world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
    private void setColorFromProvider(ClientWorld world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {

        final var provider = ColorProviderRegistry.ITEM.get(stack.getItem());
        if (provider != null) {
            final var colors = MathUtil.splitRGBToFloats(provider.getColor(stack, 0));

            this.colorRed = colors[0];
            this.colorGreen = colors[1];
            this.colorBlue = colors[2];
        }
    }

}
