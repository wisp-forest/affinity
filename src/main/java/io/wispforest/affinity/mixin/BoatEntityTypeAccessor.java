package io.wispforest.affinity.mixin;

import io.wispforest.affinity.registries.AffinityBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(BoatEntity.Type.class)
public interface BoatEntityTypeAccessor {

    @Invoker("<init>")
    static BoatEntity.Type affinity$invokeNew(String internalName, int ordinal, Block baseBlock, String name) {
        throw new IllegalStateException("How did this mixin stub get called conc");
    }

    @Mutable
    @Accessor(value = "field_7724", remap = false)
    static void affinity$setValues(BoatEntity.Type[] values) {
        throw new IllegalStateException("How did this mixin stub get called conc");
    }

    @Mixin(BoatEntity.Type.class)
    class BoatEntityTypeMixin {

        @Inject(method = "getType(I)Lnet/minecraft/entity/vehicle/BoatEntity$Type;", at = @At("HEAD"), cancellable = true)
        private static void returnCorrectType(int type, CallbackInfoReturnable<BoatEntity.Type> cir) {
            if (type != AffinityBlocks.AZALEA_BOAT_TYPE.ordinal()) return;
            cir.setReturnValue(AffinityBlocks.AZALEA_BOAT_TYPE);
        }

        @Inject(method = "getType(Ljava/lang/String;)Lnet/minecraft/entity/vehicle/BoatEntity$Type;", at = @At("HEAD"), cancellable = true)
        private static void returnCorrectType(String name, CallbackInfoReturnable<BoatEntity.Type> cir) {
            if (!Objects.equals(name, "azalea")) return;
            cir.setReturnValue(AffinityBlocks.AZALEA_BOAT_TYPE);
        }

    }
}
