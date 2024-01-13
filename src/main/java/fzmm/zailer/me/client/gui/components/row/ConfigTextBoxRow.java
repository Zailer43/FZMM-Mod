package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
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
    public Component[] getComponents(String id, String tooltipId) {
        Component configTextBox = new ConfigTextBox()
                .horizontalSizing(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(getConfigTextBoxId(id));


        return new Component[] {
            configTextBox
        };
    }

    public static String getConfigTextBoxId(String id) {
        return id + "-text-box";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigTextBox setup(FlowLayout rootComponent, String id, String defaultValue) {
        return setup(rootComponent, getConfigTextBoxId(id), id, defaultValue, null);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigTextBox setup(FlowLayout rootComponent, String textBoxId, String id, String defaultValue, @Nullable Consumer<String> changedListener) {
        return setup(rootComponent, textBoxId, id, defaultValue, changedListener, defaultValue::equals);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigTextBox setup(FlowLayout rootComponent, String textBoxId, String id, String defaultValue, @Nullable Consumer<String> changedListener, Predicate<String> defaultPredicate) {
        ConfigTextBox textBox = rootComponent.childById(ConfigTextBox.class, textBoxId);
        ButtonComponent resetButton = rootComponent.childById(ButtonComponent.class, getResetButtonId(id));

        BaseFzmmScreen.checkNull(textBox, "text-option", textBoxId);
        
        textBox.setText(defaultValue);
        textBox.setCursorToStart(false);
        textBox.onChanged().subscribe(s -> {
            if (resetButton != null)
                resetButton.active = !defaultPredicate.test(s);
            if (changedListener != null)
                changedListener.accept(s);
        });

        if (resetButton != null)
            resetButton.onPress(button -> textBox.setText(defaultValue));
        return textBox;
    }

    public static ConfigTextBoxRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new ConfigTextBoxRow(baseTranslationKey, id, tooltipId);
    }
}
