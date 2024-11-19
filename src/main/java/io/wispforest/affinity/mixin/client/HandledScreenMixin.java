package io.wispforest.affinity.mixin.client;

import io.wispforest.affinity.item.PhantomBundleItem;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    @Shadow @Final protected T handler;

    @Shadow protected abstract List<Text> getTooltipFromItem(ItemStack stack);

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("TAIL"))
    private void drawPhantomBundleTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        var stack = this.handler.getCursorStack();
        if (!stack.isOf(AffinityItems.PHANTOM_BUNDLE)) return;

        var phantomStacks = stack.get(PhantomBundleItem.STACKS);
        if (phantomStacks == null || phantomStacks.stacks().isEmpty()) return;

        context.drawTooltip(this.textRenderer, this.getTooltipFromItem(stack), stack.getTooltipData(), x, y);
    }

}
