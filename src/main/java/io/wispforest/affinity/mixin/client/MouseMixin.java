package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.block.template.ScrollInteractionReceiver;
import io.wispforest.affinity.network.AffinityNetwork;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void captureScroll(long window, double horizontal, double vertical, CallbackInfo ci, double d, int i) {
        if (client.world == null || !client.player.isSneaking()) return;
        if (!(client.crosshairTarget instanceof BlockHitResult blockHit)) return;

        var pos = blockHit.getBlockPos();
        var state = client.world.getBlockState(pos);
        if (!(state.getBlock() instanceof ScrollInteractionReceiver receiver)) return;

        var result = receiver.onScroll(client.world, state, pos, client.player, vertical > 0);
        if (result.isAccepted()) {
            client.player.swingHand(Hand.MAIN_HAND);
            AffinityNetwork.CHANNEL.clientHandle().send(
                    new ScrollInteractionReceiver.InteractionPacket(pos, vertical > 0)
            );

            ci.cancel();
        }
    }

}
