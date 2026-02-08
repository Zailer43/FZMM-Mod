package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import org.w3c.dom.Element;

public class ButtonRow extends AbstractRow {
    public ButtonRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, false);
    }

    @Override
    public UIComponent[] getComponents(String id, String tooltipId) {
        Font textRenderer = Minecraft.getInstance().font;
        Button resetButton = (Button) getResetButton("");

        UIComponent button = UIComponents.button(net.minecraft.network.chat.Component.translatable(BaseFzmmScreen.getOptionBaseTranslationKey(this.baseTranslationKey) + id + ".button"),
                        buttonComponent -> {})
                .horizontalSizing(Sizing.fixed(NORMAL_WIDTH + textRenderer.width(resetButton.getMessage()) + BaseFzmmScreen.COMPONENT_DISTANCE + BaseFzmmScreen.BUTTON_TEXT_PADDING))
                .id(id + "-button");

        return new UIComponent[] {
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
