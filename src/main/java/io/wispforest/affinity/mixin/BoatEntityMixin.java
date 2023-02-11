package io.wispforest.affinity.mixin;

import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin {

    @Shadow
    public abstract BoatEntity.Type getVariant();

    @Inject(method = "asItem", at = @At("HEAD"), cancellable = true)
    private void thisIsNotAnOakBoat(CallbackInfoReturnable<Item> cir) {
        if (this.getVariant() != AffinityBlocks.AZALEA_BOAT_TYPE) return;
        cir.setReturnValue(AffinityItems.AZALEA_BOAT);
    }

}
