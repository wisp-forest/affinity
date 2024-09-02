package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import io.wispforest.affinity.fixers.ItemTransferNodeFix;
import net.minecraft.datafixer.Schemas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Schemas.class)
public class SchemasMixin {

    @Inject(method = "build", at = @At("TAIL"))
    private static void injectAffinityFixes(DataFixerBuilder builder, CallbackInfo ci, @Local(ordinal = 225) Schema schema226) {
        builder.addFixer(new ItemTransferNodeFix(schema226));
    }

}
