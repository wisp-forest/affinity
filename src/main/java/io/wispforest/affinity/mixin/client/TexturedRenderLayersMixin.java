package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.AzaleaChestBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TexturedRenderLayers.class)
public abstract class TexturedRenderLayersMixin {

    @Shadow
    private static SpriteIdentifier getChestTextureId(ChestType type, SpriteIdentifier single, SpriteIdentifier left, SpriteIdentifier right) {
        return null;
    }

    @Shadow
    @Final
    public static Identifier CHEST_ATLAS_TEXTURE;

    @Unique
    private static final SpriteIdentifier AZALEA_CHEST_SINGLE = new SpriteIdentifier(CHEST_ATLAS_TEXTURE, Affinity.id("entity/chest/azalea"));
    @Unique
    private static final SpriteIdentifier AZALEA_CHEST_LEFT = new SpriteIdentifier(CHEST_ATLAS_TEXTURE, Affinity.id("entity/chest/azalea_left"));
    @Unique
    private static final SpriteIdentifier AZALEA_CHEST_RIGHT = new SpriteIdentifier(CHEST_ATLAS_TEXTURE, Affinity.id("entity/chest/azalea_right"));

    @Inject(method = "getChestTextureId(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/block/enums/ChestType;Z)Lnet/minecraft/client/util/SpriteIdentifier;", at = @At("HEAD"), cancellable = true)
    private static void injectAzaleaChestTexture(BlockEntity blockEntity, ChestType type, boolean christmas, CallbackInfoReturnable<SpriteIdentifier> cir) {
        if (!(blockEntity instanceof AzaleaChestBlockEntity)) return;

        cir.setReturnValue(getChestTextureId(
                type,
                AZALEA_CHEST_SINGLE,
                AZALEA_CHEST_LEFT,
                AZALEA_CHEST_RIGHT
        ));
    }

}
