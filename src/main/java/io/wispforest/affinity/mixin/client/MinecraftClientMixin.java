package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.item.KinesisStaffItem;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.network.AffinityNetwork;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Final
    public GameOptions options;

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, CallbackInfo ci) {
        MixinHooks.TEXT_OBFUSCATION = false;
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 2), cancellable = true)
    private void sendAttackPacket(CallbackInfo ci) {
        if (this.options.attackKey.wasPressed()) {
            var activeStack = this.player.getActiveItem();
            if (!(activeStack.getItem() instanceof KinesisStaffItem staff) || !staff.canThrow(activeStack, this.player)) return;

            AffinityNetwork.CHANNEL.clientHandle().send(new KinesisStaffItem.YeetPacket());
            this.player.swingHand(this.player.getActiveHand());
        }
    }

}
