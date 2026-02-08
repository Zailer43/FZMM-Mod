package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.Minecraft;
import org.w3c.dom.Element;


public class ContextMenuButtonRow extends AbstractRow {
    public ContextMenuButtonRow(String baseTranslationKey, String id, String tooltipId, boolean translate) {
        super(baseTranslationKey, id, tooltipId, false, translate);
    }

    @Override
    public UIComponent[] getComponents(String id, String tooltipId) {
        int width = NORMAL_WIDTH + BaseFzmmScreen.COMPONENT_DISTANCE + Minecraft.getInstance().font
                .width(net.minecraft.network.chat.Component.translatable("fzmm.gui.button.reset").getString()) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        UIComponent button = new ContextMenuButton(net.minecraft.network.chat.Component.empty())
                .horizontalSizing(Sizing.fixed(width))
                .id(getButtonId(id));

        return new UIComponent[] {
                button
        };
    }

    public static String getButtonId(String id) {
        return id + "-context-menu-option";
    }

    public static ContextMenuButtonRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new ContextMenuButtonRow(baseTranslationKey, id, tooltipId, true);
    }
}
