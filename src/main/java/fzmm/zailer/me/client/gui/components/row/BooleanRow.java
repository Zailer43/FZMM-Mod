package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

public class BooleanRow extends AbstractRow {
    public BooleanRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, true);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public Component[] getComponents(String id, String tooltipId) {
        Component buttonComponent = new ConfigToggleButton()
                .id(getToggleButtonId(id));

        return new Component[] {
                buttonComponent
        };
    }

    public static String getToggleButtonId(String id) {
        return id + "-toggle-button";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigToggleButton setup(FlowLayout rootComponent, String id, boolean defaultValue) {
        return setup(rootComponent, id, defaultValue, null);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigToggleButton setup(FlowLayout rootComponent, String id, boolean defaultValue, @Nullable ButtonComponent.PressAction toggledListener) {
        ConfigToggleButton toggleButton = rootComponent.childById(ConfigToggleButton.class, getToggleButtonId(id));
        ButtonComponent resetButton = rootComponent.childById(ButtonComponent.class, getResetButtonId(id));

        BaseFzmmScreen.checkNull(toggleButton, "toggle-button", getToggleButtonId(id));
        BaseFzmmScreen.checkNull(resetButton, "button", getResetButtonId(id));

        toggleButton.enabled(defaultValue);
        toggleButton.onPress(button -> {
            resetButton.active = ((boolean) toggleButton.parsedValue()) != defaultValue;
            if (toggledListener != null)
                toggledListener.onPress(button);
        });
        toggleButton.horizontalSizing(Sizing.fixed(NORMAL_WIDTH));

        resetButton.onPress(button -> toggleButton.onPress());
        resetButton.active = ((boolean) toggleButton.parsedValue()) != defaultValue;
        return toggleButton;
    }

    public static BooleanRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new BooleanRow(baseTranslationKey, id, tooltipId);
    }
}
