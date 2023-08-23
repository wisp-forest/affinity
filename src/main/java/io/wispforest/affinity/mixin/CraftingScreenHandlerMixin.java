package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.object.AffinityScreenHandlerTypes;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin {

    @ModifyArg(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/AbstractRecipeScreenHandler;<init>(Lnet/minecraft/screen/ScreenHandlerType;I)V"), index = 0)
    private static ScreenHandlerType<?> injectAugmentType(ScreenHandlerType<?> in) {
        if (!MixinHooks.injectAssemblyAugmentScreen) return in;
        MixinHooks.injectAssemblyAugmentScreen = false;

        return AffinityScreenHandlerTypes.ASSEMBLY_AUGMENT;
    }

}
