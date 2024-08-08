package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.item.AzaleaBowItem;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

    @ModifyArg(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V"), index = 4)
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

    @ModifyExpressionValue(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getProjectileType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack azaleaBowAlwaysHasProjectile(ItemStack original) {
        if ((Object) this != AffinityItems.AZALEA_BOW || !original.isEmpty()) return original;

        return Items.ARROW.getDefaultStack();
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "use", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private boolean azaleaBowAlwaysHasProjectileEpisode2(boolean hasProjectile) {
        if ((Object) this != AffinityItems.AZALEA_BOW) return hasProjectile;
        return true;
    }

    @Inject(method = "shoot", at = @At("HEAD"))
    private void decreaseAzaleaBowArrowDamage(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, LivingEntity target, CallbackInfo ci) {
        if ((Object) this != AffinityItems.AZALEA_BOW) return;
        if (!(projectile instanceof PersistentProjectileEntity arrow)) return;

        arrow.setDamage(arrow.getDamage() / 1.5f);
        arrow.getComponent(AffinityComponents.ENTITY_FLAGS).setFlag(EntityFlagComponent.SHOT_BY_AZALEA_BOW);
    }

    @Inject(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;load(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)Ljava/util/List;"), cancellable = true)
    private void enforceAzaleaBowAethumCost(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (((Object) this != AffinityItems.AZALEA_BOW) || !(user instanceof PlayerEntity player)) return;

        var aethum = player.getComponent(AffinityComponents.PLAYER_AETHUM);
        if (!aethum.tryConsumeAethum(AzaleaBowItem.AETHUM_COST_PER_SHOT)) {
            ci.cancel();
        }
    }

}
