package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

public class BooleanRow extends AbstractRow {
    public BooleanRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, true);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        Component buttonComponent = new BooleanButton(Text.translatable("text.owo.config.boolean_toggle.enabled"), Text.translatable("text.owo.config.boolean_toggle.disabled"))
                .id(getBooleanButtonId(id));

        return new Component[] {
                buttonComponent
        };
    }

    public static String getBooleanButtonId(String id) {
        return id + "-boolean-button";
    }

    public static BooleanButton setup(FlowLayout rootComponent, String id, boolean defaultValue) {
        return setup(rootComponent, id, defaultValue, null);
    }

    public static BooleanButton setup(FlowLayout rootComponent, String id, boolean defaultValue, @Nullable ButtonComponent.PressAction toggledListener) {
        BooleanButton booleanButton = rootComponent.childById(BooleanButton.class, getBooleanButtonId(id));
        ButtonComponent resetButton = rootComponent.childById(ButtonComponent.class, getResetButtonId(id));

        BaseFzmmScreen.checkNull(booleanButton, "boolean-button", getBooleanButtonId(id));

        booleanButton.enabled(defaultValue);
        booleanButton.onPress(button -> {
            if (resetButton != null)
                resetButton.active = booleanButton.enabled() != defaultValue;

            if (toggledListener != null)
                toggledListener.onPress(button);
        });
        booleanButton.horizontalSizing(Sizing.fixed(NORMAL_WIDTH));

        if (resetButton != null) {
            resetButton.onPress(button -> booleanButton.onPress());
            resetButton.active = booleanButton.enabled() != defaultValue;
        }

        return booleanButton;
    }

    public static BooleanRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new BooleanRow(baseTranslationKey, id, tooltipId);
    }
}
