package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.function.Consumer;

public class TextBoxRow extends AbstractRow {
    public TextBoxRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, true);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        Component textBox = Components
                .textBox(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(getTextBoxId(id));

        return new Component[] {
                textBox
        };
    }

    public static String getTextBoxId(String id) {
        return id + "-text-box";
    }


    public static TextFieldWidget setup(FlowLayout rootComponent, String id, String defaultValue) {
        return setup(rootComponent, id, defaultValue, null);
    }

    public static TextFieldWidget setup(FlowLayout rootComponent, String id, String defaultValue, @Nullable Consumer<String> changedListener) {
        TextFieldWidget textField = rootComponent.childById(TextFieldWidget.class, getTextBoxId(id));
        ButtonComponent resetButton = rootComponent.childById(ButtonComponent.class, getResetButtonId(id));

        BaseFzmmScreen.checkNull(textField, "text-box", getTextBoxId(id));

        textField.setChangedListener(text -> {
            if (resetButton != null)
                resetButton.active = !textField.getText().equals(defaultValue);
            if (changedListener != null)
                changedListener.accept(text);
        });
        if (defaultValue.length() > 32)
            textField.setMaxLength(defaultValue.length());
        textField.setText(defaultValue);
        textField.setCursor(0);

        if (resetButton != null)
            resetButton.onPress(button -> textField.setText(defaultValue));
        return textField;
    }

    public static TextBoxRow parse(Element element) {
        String baseTranslationKey = getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new TextBoxRow(baseTranslationKey, id, tooltipId);
    }
}
