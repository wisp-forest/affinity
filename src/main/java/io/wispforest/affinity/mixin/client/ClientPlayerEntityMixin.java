package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.item.KinesisStaffItem;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends LivingEntity {

    @Shadow public Input input;

    protected ClientPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;noClip:Z", opcode = Opcodes.GETFIELD))
    private void swiftKinesisMoment(CallbackInfo ci) {
        if (!(this.activeItemStack.getItem() instanceof KinesisStaffItem)) return;

        this.input.movementSideways *= 5;
        this.input.movementForward *= 5;
    }

}
