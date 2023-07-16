package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.item.SpecialTransformItem;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Unique
    private static final ItemStack AFFINITY$FILLED_MAP = new ItemStack(Items.FILLED_MAP);

    @Unique
    private ItemStack affinity$cachedItem = null;

    @ModifyVariable(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0), argsOnly = true)
    private ItemStack injectMap(ItemStack value) {
        if (!value.isOf(AffinityItems.REALIZED_AETHUM_MAP)) return value;
        this.affinity$cachedItem = value;
        return AFFINITY$FILLED_MAP;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "renderFirstPersonItem", at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 4), argsOnly = true)
    private ItemStack restoreMap(ItemStack value) {
        if (this.affinity$cachedItem == null) return value;
        var item = this.affinity$cachedItem;
        this.affinity$cachedItem = null;
        return item;
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 2))
    private void applyFireExtinguisherTransform(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(item.getItem() instanceof SpecialTransformItem transformItem)) return;
        transformItem.applyUseActionTransform(item, player, matrices, tickDelta, swingProgress);
    }

}
