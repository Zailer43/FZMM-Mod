package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.IMode;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

public class EnumRow extends AbstractRow {
    public EnumRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, true);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        Component enumWidget = new EnumWidget()
                .horizontalSizing(Sizing.fixed(NORMAL_WIDTH))
                .id(getEnumId(id));

        return new Component[] {
                enumWidget
        };
    }

    public static String getEnumId(String id) {
        return id + "-enum-option";
    }

    public static EnumWidget setup(FlowLayout rootComponent, String id, Enum<? extends IMode> defaultValue, @Nullable ButtonComponent.PressAction callback) {
        return setup(rootComponent, id, defaultValue, false, callback);
    }

        @SuppressWarnings("UnstableApiUsage")
    public static EnumWidget setup(FlowLayout rootComponent, String id, Enum<? extends IMode> defaultValue, boolean showTooltip, @Nullable ButtonComponent.PressAction callback) {
        EnumWidget enumButton = rootComponent.childById(EnumWidget.class, getEnumId(id));
        ButtonComponent resetButton = rootComponent.childById(ButtonComponent.class, getResetButtonId(id));

        BaseFzmmScreen.checkNull(enumButton, "enum-option", getEnumId(id));
        BaseFzmmScreen.checkNull(resetButton, "button", getResetButtonId(id));

        enumButton.setShowTooltip(showTooltip);
        enumButton.init(defaultValue);
        enumButton.onPress(button -> {
            if (callback != null)
                callback.onPress(button);
            resetButton.active = enumButton.parsedValue() != defaultValue;
        });

        resetButton.onPress(button -> {
            enumButton.select(defaultValue.ordinal());
            resetButton.active = false;
        });
        resetButton.onPress();
        return enumButton;
    }

    public static EnumRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new EnumRow(baseTranslationKey, id, tooltipId);
    }
}
