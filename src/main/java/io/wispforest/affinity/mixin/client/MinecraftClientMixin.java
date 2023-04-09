package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.block.template.AttackInteractionReceiver;
import io.wispforest.affinity.item.KinesisStaffItem;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityItems;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Final
    public GameOptions options;

    @Shadow
    @Nullable
    public ClientWorld world;

    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, CallbackInfo ci) {
        MixinHooks.TEXT_OBFUSCATION = false;
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 2))
    private void sendYeetPacket(CallbackInfo ci) {
        if (this.options.attackKey.wasPressed()) {
            var activeStack = this.player.getActiveItem();
            if (!(activeStack.getItem() instanceof KinesisStaffItem staff) || !staff.canThrow(activeStack, this.player)) return;

            var dataBuffer = PacketByteBufs.create();
            staff.writeExtraThrowData(activeStack, this.player, dataBuffer);

            AffinityNetwork.CHANNEL.clientHandle().send(new KinesisStaffItem.YeetPacket(dataBuffer));
            this.player.swingHand(this.player.getActiveHand());
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void sendAttackPacket(CallbackInfoReturnable<Boolean> cir) {
        if (this.player != null && this.player.getMainHandStack().isOf(AffinityItems.WAND_OF_INQUIRY)) return;
        if (this.world == null || !(this.crosshairTarget instanceof BlockHitResult blockHit)) return;

        var pos = blockHit.getBlockPos();
        var state = this.world.getBlockState(pos);
        if (!(state.getBlock() instanceof AttackInteractionReceiver receiver)) return;

        var result = receiver.onAttack(this.world, state, pos, this.player);
        if (!result.isAccepted()) return;

        this.player.swingHand(Hand.MAIN_HAND);
        AffinityNetwork.CHANNEL.clientHandle().send(
                new AttackInteractionReceiver.InteractionPacket(pos)
        );

        this.options.attackKey.setPressed(false);
        cir.setReturnValue(true);
    }

}
