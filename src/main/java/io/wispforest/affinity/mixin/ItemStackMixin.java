package io.wispforest.affinity.mixin;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.ChunkAethumComponent;
import io.wispforest.affinity.misc.ArcaneFadeFluid;
import io.wispforest.affinity.misc.potion.GlowingPotion;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    private static final TagKey<Item> AFFINITY$CANNOT_USE_IN_DYING_CHUNKS = TagKey.of(RegistryKeys.ITEM, Affinity.id("cannot_use_in_dying_chunks"));

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void injectNameColorForIncandescence(CallbackInfoReturnable<Text> cir) {
        final var stack = (ItemStack) (Object) this;
        if (!(PotionUtil.getPotion(stack) instanceof GlowingPotion)) return;
        if (!stack.has(PotionMixture.EXTRA_DATA) || !stack.get(PotionMixture.EXTRA_DATA).has(GlowingPotion.COLOR_KEY)) return;

        var color = stack.get(PotionMixture.EXTRA_DATA).get(GlowingPotion.COLOR_KEY);
        cir.setReturnValue(cir.getReturnValue().copy().styled(style -> style.withColor(Color.ofDye(color).rgb())));
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void cancelInteractionsInDyingChunks(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (!context.getStack().isIn(AFFINITY$CANNOT_USE_IN_DYING_CHUNKS)) return;

        var component = context.getWorld().getChunk(context.getBlockPos()).getComponent(AffinityComponents.CHUNK_AETHUM);
        if (!component.isEffectActive(ChunkAethumComponent.INFERTILITY)) return;

        cir.setReturnValue(ActionResult.PASS);
    }

    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    private void removeGlint(CallbackInfoReturnable<Boolean> cir) {
        if (!((ItemStack) (Object) this).getOr(ArcaneFadeFluid.REMOVE_ENCHANTMENT_GLINT_KEY, false)) return;
        cir.setReturnValue(false);
    }

}
