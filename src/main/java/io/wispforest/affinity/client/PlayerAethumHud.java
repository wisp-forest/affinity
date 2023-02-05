package io.wispforest.affinity.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.hud.Hud;
import io.wispforest.owo.ui.util.Delta;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class PlayerAethumHud {

    public static final Identifier COMPONENT_ID = Affinity.id("player_aethum");

    static void initialize() {
        Hud.add(COMPONENT_ID, () -> new BaseComponent() {

            private double displayAethum = 1f;
            private double slowDisplayAethum = 1f;
            private float alpha = 0;

            @Override
            public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
                var player = MinecraftClient.getInstance().player;
                if (player == null) return;

                var component = AffinityComponents.PLAYER_AETHUM.get(player);
                double aethumProgress = component.getAethum() / component.getMaxAethum();

                this.displayAethum += Delta.compute(this.displayAethum, aethumProgress, delta);
                this.slowDisplayAethum += Delta.compute(this.slowDisplayAethum, aethumProgress, delta * .1f);

                this.alpha += Delta.compute(this.alpha, aethumProgress == 1 ? 0 : 10, delta * .25f);

                RenderSystem.setShaderColor(1, 1, 1, Math.min(this.alpha, 1));

                Drawer.drawRing(
                        matrices,
                        this.x + this.width / 2,
                        this.y + this.height / 2,
                        50,
                        3, 8,
                        Color.ofArgb(0x7f000000), Color.ofArgb(0x7f000000)
                );

                if (this.slowDisplayAethum > this.displayAethum) {
                    this.drawAethumRing(matrices, this.slowDisplayAethum, Color.ofRgb(0xce2424));
                }
                this.drawAethumRing(matrices, this.displayAethum, Color.ofRgb(Affinity.AETHUM_FLUX_COLOR));

                RenderSystem.setShaderColor(1, 1, 1, 1);
            }

            private void drawAethumRing(MatrixStack matrices, double progress, Color color) {
                Drawer.drawRing(
                        matrices,
                        this.x + this.width / 2,
                        this.y + this.height / 2,
                        360 - progress * 360 + 90, 360 + 90,
                        (int) Math.round(50 * progress),
                        3, 8,
                        color, color
                );
            }

            @Override
            protected int determineHorizontalContentSize(Sizing sizing) {
                return 16;
            }

            @Override
            protected int determineVerticalContentSize(Sizing sizing) {
                return 16;
            }
        }.positioning(Positioning.relative(50, 50)).margins(Insets.left(32)));
    }

}
