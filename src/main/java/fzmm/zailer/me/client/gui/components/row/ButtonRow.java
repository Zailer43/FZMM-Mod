package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

public class ButtonRow extends AbstractRow {
    public ButtonRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, false);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        ButtonWidget resetButton = (ButtonWidget) getResetButton("");

        Component button = Components.button(Text.translatable(BaseFzmmScreen.getOptionBaseTranslationKey(this.baseTranslationKey) + id + ".button"),
                        buttonComponent -> {})
                .horizontalSizing(Sizing.fixed(NORMAL_WIDTH + textRenderer.getWidth(resetButton.getMessage()) + BaseFzmmScreen.COMPONENT_DISTANCE + BaseFzmmScreen.BUTTON_TEXT_PADDING))
                .id(id + "-button");

        return new Component[] {
                button
        };
    }

    public static ButtonRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new ButtonRow(baseTranslationKey, id, tooltipId);
    }
}
