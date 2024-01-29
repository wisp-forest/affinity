package io.wispforest.affinity.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.client.AffinityClient;
import io.wispforest.affinity.client.render.PostEffectBuffer;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class PlayerAethumHud {

    public static final Identifier COMPONENT_ID = Affinity.id("player_aethum");

    private static final PostEffectBuffer BUFFER = new PostEffectBuffer();

    public static void initialize() {
        Hud.add(COMPONENT_ID, () -> new BaseComponent() {

            private double displayAethum = 0f;
            private double slowDisplayAethum = 0f;

            private double lastMaxAethum = 0f;
            private double maxAethumChange = 0f;
            private double maxAethumChangeAge = 1f;

            private float alpha = 0f;
            private float warningColorWeight = 0f;

            @Override
            public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
                var client = MinecraftClient.getInstance();
                if (client.player == null) return;

                var component = client.player.getComponent(AffinityComponents.PLAYER_AETHUM);
                var maxAethum = component.maxAethum();

                if (this.lastMaxAethum != maxAethum) {
                    this.maxAethumChange = Math.max(0, maxAethum - this.lastMaxAethum);
                    this.lastMaxAethum = maxAethum;
                    this.maxAethumChangeAge = 0f;
                }

                if (this.displayAethum > maxAethum) {
                    this.displayAethum = maxAethum;
                    this.slowDisplayAethum = maxAethum;
                }

                this.maxAethumChangeAge += Delta.compute(this.maxAethumChangeAge, 1f, delta * 0.05f);

                this.displayAethum += Delta.compute(this.displayAethum, component.getAethum(), delta);
                this.slowDisplayAethum += Delta.compute(this.slowDisplayAethum, component.getAethum(), delta * .1f);

                this.alpha += Delta.compute(this.alpha, component.getAethum() >= maxAethum ? 0 : 10, delta * .25f);
                this.warningColorWeight += Delta.compute(this.warningColorWeight, component.getAethum() < 3 ? 1 : 0, delta * .25f);

                var color = RenderSystem.getShaderColor().clone();
                RenderSystem.setShaderColor(1, 1, 1, Math.min(this.alpha, 1));
                BUFFER.beginWrite(true, 0);

                context.drawRing(
                        this.x + this.width / 2,
                        this.y + this.height / 2,
                        50, 3, 8,
                        Color.ofArgb(0x7f000000), Color.ofArgb(0x7f000000)
                );

                int maxAethumChangeColor = (int) ((0x7f) * (1 - this.maxAethumChangeAge)) << 24 | 0x03C988;
                context.drawRing(
                        this.x + this.width / 2,
                        this.y + this.height / 2,
                        90, 360 * (this.maxAethumChange / maxAethum) + 91,
                        50, 3, 8,
                        Color.ofArgb(maxAethumChangeColor), Color.ofArgb(maxAethumChangeColor)
                );

                if (this.slowDisplayAethum > this.displayAethum) {
                    this.drawAethumRing(context, this.slowDisplayAethum / maxAethum, Color.ofRgb(0xAD7BE9));
                }

                this.drawAethumRing(context, this.displayAethum / maxAethum, Affinity.AETHUM_FLUX_COLOR.interpolate(Color.ofRgb(0xce2424), this.warningColorWeight));

                RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
                BUFFER.endWrite();

                AffinityClient.DOWNSAMPLE_PROGRAM.prepare(BUFFER.buffer());
                AffinityClient.DOWNSAMPLE_PROGRAM.use();

                var transform = context.getMatrices().peek().getPositionMatrix();
                var buffer = Tessellator.getInstance().getBuffer();

                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                buffer.vertex(transform, 0f, 0f, 0f).next();
                buffer.vertex(transform, 0f, 1f * client.getWindow().getScaledHeight(), 0f).next();
                buffer.vertex(transform, 1f * client.getWindow().getScaledWidth(), 1f * client.getWindow().getScaledHeight(), 0f).next();
                buffer.vertex(transform, 1f * client.getWindow().getScaledWidth(), 0f, 0f).next();
                Tessellator.getInstance().draw();
            }

            private void drawAethumRing(OwoUIDrawContext context, double progress, Color color) {
                var colorHsv = color.hsv();
                context.drawRing(
                        this.x + this.width / 2,
                        this.y + this.height / 2,
                        360 - progress * 360 + 90, 360 + 90.05,
                        (int) Math.round(50 * progress),
                        3, 8,
                        color, Color.ofHsv(Math.min(colorHsv[0] + .065f, 1), colorHsv[1] * 1.3f, colorHsv[2])
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
