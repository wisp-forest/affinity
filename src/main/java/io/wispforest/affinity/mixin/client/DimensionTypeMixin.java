package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.util.CelestialZoomer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {

    @ModifyArg(method = "getSkyAngle", at = @At(value = "INVOKE", target = "Ljava/util/OptionalLong;orElse(J)J"))
    private long weSmoothen(long original) {
        if (!CelestialZoomer.isZooming() || MinecraftClient.getInstance().isPaused()) return original;
        return (long) MathHelper.lerp(MinecraftClient.getInstance().getTickDelta(), CelestialZoomer.lastWorldTime, original);
    }

}
