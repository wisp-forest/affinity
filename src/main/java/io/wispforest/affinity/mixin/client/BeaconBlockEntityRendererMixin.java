package io.wispforest.affinity.mixin.client;

import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BeaconBlockEntityRenderer.class)
public class BeaconBlockEntityRendererMixin {

    @ModifyArg(method = "renderBeam(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/util/Identifier;FFJIIIFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getBeaconBeam(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;"))
    private static boolean makeTranslucent(boolean translucent) {
        return true;
    }

}
