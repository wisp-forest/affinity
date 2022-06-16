package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.OptionalInt;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin<T extends ItemFrameEntity> {

    @Unique
    private static final ItemStack AFFINITY$MAP_STACK = Items.FILLED_MAP.getDefaultStack();

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "render(Lnet/minecraft/entity/decoration/ItemFrameEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "JUMP",
                    opcode = Opcodes.IFEQ,
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            ),
            ordinal = 0
    )
    private OptionalInt weRenderAethumMaps(OptionalInt value, T entity) {
        if (entity.getHeldItemStack().isOf(AffinityItems.REALIZED_AETHUM_MAP)) return OptionalInt.of(FilledMapItem.getMapId(entity.getHeldItemStack()));
        return value;
    }

    @ModifyVariable(method = "getModelId", at = @At("HEAD"), argsOnly = true)
    private ItemStack weHaveCorrectModels(ItemStack stack) {
        if (!stack.isOf(AffinityItems.REALIZED_AETHUM_MAP)) return stack;
        return AFFINITY$MAP_STACK;
    }

}
