package io.wispforest.affinity.client.screen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.screenhandler.OuijaBoardScreenHandler;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.w3c.dom.Element;

import java.util.Map;

public class OuijaBoardScreen extends BaseUIModelHandledScreen<FlowLayout, OuijaBoardScreenHandler> {

    private static final Identifier SGA_FONT = new Identifier("minecraft", "alt");

    private final TrimmedLabelComponent[] curseLabels = new TrimmedLabelComponent[3];
    private final LabelComponent[] costLabels = new LabelComponent[3];
    private final ButtonComponent[] curseButtons = new ButtonComponent[3];

    public OuijaBoardScreen(OuijaBoardScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, FlowLayout.class, Affinity.id("ouija_board"));

        this.backgroundHeight += 2;
        this.playerInventoryTitleY += 2;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        for (int i = 0; i < 3; i++) {
            int index = i + 1;

            rootComponent.childById(ButtonComponent.class, "curse-button-" + index).onPress(button -> {
                this.handler.executeCurse(new OuijaBoardScreenHandler.CurseMessage(index));
            });

            this.curseLabels[i] = rootComponent.childById(TrimmedLabelComponent.class, "curse-label-" + index);
            this.costLabels[i] = rootComponent.childById(LabelComponent.class, "cost-label-" + index);

            this.curseButtons[i] = rootComponent.childById(ButtonComponent.class, "curse-button-" + index).onPress(button -> {
                this.handler.executeCurse(new OuijaBoardScreenHandler.CurseMessage(index));
            });
        }
    }

    public void updateCurses() {
        for (int i = 0; i < 3; i++) {
            var curse = this.handler.currentCurses()[i];

            if (curse == null) {
                this.curseButtons[i].parent().sizing(Sizing.fixed(0));
                continue;
            } else {
                this.curseButtons[i].parent().sizing(Sizing.content());
            }

            int cost = this.handler.enchantmentCost(curse);
            boolean canAfford = this.handler.canAfford(cost);

            this.curseButtons[i].active = canAfford;

            var curseName = curse.getName(1);
            this.curseButtons[i].tooltip(curseName);

            this.curseLabels[i]
                    .text(curseName.copy().styled(style -> Style.EMPTY.withFont(SGA_FONT)))
                    .color(canAfford ? Color.ofRgb(0x663333) : Color.ofRgb(0x331a1a));

            this.costLabels[i]
                    .text(Text.literal(String.valueOf(cost)))
                    .color(canAfford ? Color.ofRgb(0x80ff20) : Color.ofRgb(0x407f10));
        }
    }

    public static class TrimmedLabelComponent extends BaseComponent {

        private final TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

        private Color textColor = Color.WHITE;
        private Text text = Text.empty();

        private OrderedText renderText = OrderedText.EMPTY;

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            context.drawText(this.renderer, this.renderText, this.x, this.y, this.textColor.argb(), false);
        }

        @Override
        public void inflate(Size space) {
            super.inflate(space);
            this.renderText = Language.getInstance().reorder(this.renderer.trimToWidth(this.text, this.width));
        }

        public TrimmedLabelComponent text(Text text) {
            this.text = text;
            this.renderText = Language.getInstance().reorder(this.renderer.trimToWidth(this.text, this.width));

            return this;
        }

        public Text text() {
            return text;
        }

        public TrimmedLabelComponent color(Color textColor) {
            this.textColor = textColor;
            return this;
        }

        public Color color() {
            return textColor;
        }

        @Override
        public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
            super.parseProperties(model, element, children);

            UIParsing.apply(children, "text", UIParsing::parseText, this::text);
            UIParsing.apply(children, "color", Color::parse, this::color);
        }
    }

    static {
        UIParsing.registerFactory("affinity.trimmed-label", element -> new TrimmedLabelComponent());
    }
}
