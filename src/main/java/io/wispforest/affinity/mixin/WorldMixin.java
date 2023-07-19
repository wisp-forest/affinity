package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow
    public abstract WorldChunk getWorldChunk(BlockPos pos);

    @Redirect(method = "hasRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isRaining()Z"))
    private boolean useLocalWeather(World instance, BlockPos pos) {
        if (instance.isClient)
            return instance.isRaining();

        var chunk = getWorldChunk(pos);

        if (chunk instanceof EmptyChunk) {
            return instance.isRaining();
        }

        var component = chunk.getComponent(AffinityComponents.LOCAL_WEATHER);

        return component.getRainGradient() > 0.2;
    }
}
