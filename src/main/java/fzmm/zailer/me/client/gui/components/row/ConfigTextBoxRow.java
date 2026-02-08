package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConfigTextBoxRow extends AbstractRow {
    public ConfigTextBoxRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, true);
    }


    @Override
    @SuppressWarnings("UnstableApiUsage")
    public UIComponent[] getComponents(String id, String tooltipId) {
        UIComponent configTextBox = new ConfigTextBox()
                .horizontalSizing(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(getConfigTextBoxId(id));


        return new UIComponent[] {
            configTextBox
        };
    }

    public static String getConfigTextBoxId(String id) {
        return id + "-text-box";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigTextBox setup(EFlowLayout rootComponent, String id, String defaultValue) {
        return setup(rootComponent, getConfigTextBoxId(id), id, defaultValue, null);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigTextBox setup(EFlowLayout rootComponent, String textBoxId, String id, String defaultValue, @Nullable Consumer<String> changedListener) {
        return setup(rootComponent, textBoxId, id, defaultValue, changedListener, defaultValue::equals);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigTextBox setup(EFlowLayout rootComponent, String textBoxId, String id, String defaultValue, @Nullable Consumer<String> changedListener, Predicate<String> defaultPredicate) {
        ConfigTextBox textBox = rootComponent.childByIdOrThrow(ConfigTextBox.class, textBoxId);
        ButtonComponent resetButton = rootComponent.childById(ButtonComponent.class, getResetButtonId(id));
        
        textBox.text(defaultValue);
        textBox.onChanged().subscribe(s -> {
            if (resetButton != null)
                resetButton.active = !defaultPredicate.test(s);
            if (changedListener != null)
                changedListener.accept(s);
        });

        if (resetButton != null)
            resetButton.onPress(button -> textBox.text(defaultValue));
        return textBox;
    }

    public static ConfigTextBoxRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new ConfigTextBoxRow(baseTranslationKey, id, tooltipId);
    }
}
