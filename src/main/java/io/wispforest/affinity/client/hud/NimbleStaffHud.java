package io.wispforest.affinity.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.NimbleStaffItem;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.hud.Hud;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class NimbleStaffHud {

    public static final Identifier COMPONENT_ID = Affinity.id("nimble_staff_indicator");
    public static final Identifier TEXTURE_ID = Affinity.id("nimble_staff_indicator");

    public static void initialize() {
        Hud.add(COMPONENT_ID, () -> new BaseComponent() {

            private float opacity = 0f;

            @Override
            protected int determineHorizontalContentSize(Sizing sizing) {
                return 4;
            }

            @Override
            protected int determineVerticalContentSize(Sizing sizing) {
                return 20;
            }

            @Override
            public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
                if (!MinecraftClient.getInstance().player.isHolding(AffinityItems.NIMBLE_STAFF) && MathHelper.approximatelyEquals(this.opacity, 0)) return;
                this.opacity += Delta.compute(this.opacity, NimbleStaffItem.findFlingTarget(MinecraftClient.getInstance().player) != null ? 1 : 0, delta * 1.25f);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                var color = RenderSystem.getShaderColor().clone();
                RenderSystem.setShaderColor(1f, 1f, 1f, this.opacity);
                context.drawGuiTexture(TEXTURE_ID, this.x, this.y, 3, 19);
                RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);

                RenderSystem.disableBlend();
            }
        }.positioning(Positioning.relative(50, 50)));
    }

}
