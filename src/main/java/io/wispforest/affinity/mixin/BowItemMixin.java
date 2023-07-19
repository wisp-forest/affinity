package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.item.AzaleaBowItem;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantValue")
@Mixin(BowItem.class)
public class BowItemMixin {

    @ModifyArg(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V"), index = 4)
    private float azaleaBowHigherProjectileSpeed(float arrowSpeed) {
        if ((Object) this != AffinityItems.AZALEA_BOW) return arrowSpeed;
        return arrowSpeed * 1.5f;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "onStoppedUsing", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private float azaleaBowChargesQuicker(float pullProgress) {
        if ((Object) this != AffinityItems.AZALEA_BOW) return pullProgress;
        return Math.min(1f, pullProgress * 2f);
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "onStoppedUsing", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private boolean azaleaBowAlwaysHasProjectile(boolean hasProjectile) {
        if ((Object) this != AffinityItems.AZALEA_BOW) return hasProjectile;
        return true;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "use", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private boolean azaleaBowAlwaysHasProjectileEpisode2(boolean hasProjectile) {
        if ((Object) this != AffinityItems.AZALEA_BOW) return hasProjectile;
        return true;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "onStoppedUsing", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private PersistentProjectileEntity decreaseAzaleaBowArrowDamage(PersistentProjectileEntity arrow) {
        if ((Object) this != AffinityItems.AZALEA_BOW) return arrow;

        arrow.setDamage(arrow.getDamage() / 1.5f);
        arrow.getComponent(AffinityComponents.ENTITY_FLAGS).setFlag(EntityFlagComponent.SHOT_BY_AZALEA_BOW);
        return arrow;
    }

    @Inject(method = "onStoppedUsing", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/world/World;isClient:Z", ordinal = 0), cancellable = true)
    private void enforceAzaleaBowAethumCost(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (((Object) this != AffinityItems.AZALEA_BOW) || !(user instanceof PlayerEntity player)) return;

        var aethum = player.getComponent(AffinityComponents.PLAYER_AETHUM);
        if (!aethum.tryConsumeAethum(AzaleaBowItem.AETHUM_COST_PER_SHOT)) {
            ci.cancel();
        }
    }

}
