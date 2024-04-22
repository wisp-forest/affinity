package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.item.ArtifactBladeItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void blockArtifactBladeUseWhenNotAttacking(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        var playerStack = player.getStackInHand(hand);
        if (!(playerStack.getItem() instanceof ArtifactBladeItem) || MinecraftClient.getInstance().options.attackKey.isPressed()) return;

        cir.setReturnValue(ActionResult.PASS);
    }

}
