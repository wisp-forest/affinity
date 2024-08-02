package io.wispforest.affinity.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.OptionalInt;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin<T extends ItemFrameEntity> {

    @ModifyVariable(
            method = "render(Lnet/minecraft/entity/decoration/ItemFrameEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "STORE", ordinal = 0)
    )
    private MapIdComponent weRenderAethumMaps(MapIdComponent value, T entity) {
        if (entity.getHeldItemStack().isOf(AffinityItems.REALIZED_AETHUM_MAP)) {
            return entity.getHeldItemStack().get(DataComponentTypes.MAP_ID);
        }
        return value;
    }

    @ModifyExpressionValue(method = "getModelId", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean weHaveCorrectModels(boolean original, @Local(argsOnly = true) ItemStack stack) {
        if (!stack.isOf(AffinityItems.REALIZED_AETHUM_MAP)) return original;
        return true;
    }

}
