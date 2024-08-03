package io.wispforest.affinity.mixin;

import io.wispforest.affinity.item.ResplendentGemItem;
import io.wispforest.affinity.object.AffinityEnchantments;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow
    @Final
    private Property levelCost;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @ModifyVariable(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isDamageable()Z", ordinal = 0))
    private boolean allowResplendentGem(boolean allowed) {
        final var addition = this.input.getStack(1);
        return allowed || (addition.isOf(AffinityItems.RESPLENDENT_GEM) && !ResplendentGemItem.getEnchantmentNbt(addition).isEmpty());
    }

    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;set(Ljava/util/Map;Lnet/minecraft/item/ItemStack;)V"))
    private void setResplendentGemCost(CallbackInfo ci) {
        if (!this.input.getStack(1).isOf(AffinityItems.RESPLENDENT_GEM)) return;
        this.levelCost.set(30);
    }

    @ModifyArg(method = {"updateResult", "setNewItemName"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private @Nullable Object applyIlliteracy(ComponentType<?> type, @Nullable Object name) {
        if (type != DataComponentTypes.CUSTOM_NAME) return name;
        if (!this.isIlliterate()) return name;

        return ((Text) name).copy().styled(style -> style.withObfuscated(true));
    }

    @Unique
    private boolean isIlliterate() {
        return EnchantmentHelper.getEquipmentLevel(AffinityEnchantments.CURSE_OF_ILLITERACY, this.player) > 0;
    }
}
