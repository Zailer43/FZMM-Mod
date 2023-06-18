package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.compat.symbolChat.font.FontTextBoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TextBoxRow extends AbstractRow {

    public TextBoxRow(String baseTranslationKey, String id, String tooltipId, boolean symbolChatButtons) {
        super(baseTranslationKey, id, tooltipId, true);

        FontTextBoxComponent fontTextBoxComponent = this.childById(FontTextBoxComponent.class, getTextBoxId(id));
        if (fontTextBoxComponent != null)
            fontTextBoxComponent.setEnabled(symbolChatButtons);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        Component textBox = new FontTextBoxComponent(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(getTextBoxId(id));

        return new Component[]{
                textBox
        };
    }

    public static String getTextBoxId(String id) {
        return id + "-text-box";
    }


    public static TextFieldWidget setup(FlowLayout rootComponent, String id, String defaultValue, int maxLength) {
        return setup(rootComponent, id, defaultValue, maxLength, null);
    }

    public static TextFieldWidget setup(FlowLayout rootComponent, String id, String defaultValue, int maxLength, @Nullable Consumer<String> changedListener) {
        TextFieldWidget textField = rootComponent.childById(TextFieldWidget.class, getTextBoxId(id));
        ButtonComponent resetButton = rootComponent.childById(ButtonComponent.class, getResetButtonId(id));

        BaseFzmmScreen.checkNull(textField, "text-box", getTextBoxId(id));

        textField.setChangedListener(text -> {
            if (resetButton != null)
                resetButton.active = !textField.getText().equals(defaultValue);
            if (changedListener != null)
                changedListener.accept(text);
        });
        textField.setMaxLength(maxLength);
        textField.setText(defaultValue);
        textField.setCursor(0);

        if (resetButton != null)
            resetButton.onPress(button -> textField.setText(defaultValue));
        return textField;
    }

    public static TextBoxRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);
        boolean removeResetButton = UIParsing.childElements(element).containsKey("removeResetButton") &&
                UIParsing.parseBool(UIParsing.childElements(element).get("removeResetButton"));

        boolean removeHorizontalMargins = UIParsing.childElements(element).containsKey("removeHorizontalMargins") &&
                UIParsing.parseBool(UIParsing.childElements(element).get("removeHorizontalMargins"));

        boolean symbolChatButtons = UIParsing.childElements(element).containsKey("symbolChatButtons") &&
                UIParsing.parseBool(UIParsing.childElements(element).get("symbolChatButtons"));


        int fieldSize = UIParsing.childElements(element).containsKey("fieldSize") ? UIParsing.parseSignedInt(UIParsing.childElements(element).get("fieldSize")) : -1;
        fieldSize = UIParsing.childElements(element).containsKey("removeHorizontalMargins") ? fieldSize : -1;

        TextBoxRow row = new TextBoxRow(baseTranslationKey, id, tooltipId, symbolChatButtons);
        if (removeHorizontalMargins)
            row.removeHorizontalMargins();

        if (removeResetButton)
            row.removeResetButton();

        if (fieldSize > 0) {
            TextBoxComponent textBox = row.childById(TextBoxComponent.class, getTextBoxId(id));
            if (textBox != null)
                textBox.horizontalSizing(Sizing.fixed(fieldSize));
        }

        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (symbolChatButtons && screen instanceof BaseFzmmScreen baseFzmmScreen)
            row.addSymbolChatButtons(baseFzmmScreen);
        return row;
    }


    public void addSymbolChatButtons(BaseFzmmScreen screen) {
        if (!FzmmClient.CONFIG.general.showSymbolButton())
            return;

        Optional<FlowLayout> rightLayoutOptional = this.getRightLayout();
        if (rightLayoutOptional.isEmpty())
            return;

        FlowLayout rightLayout = rightLayoutOptional.get();
        List<Component> componentList = List.copyOf(rightLayout.children());
        TextBoxComponent textBoxComponent = rightLayout.childById(TextBoxComponent.class, getTextBoxId(this.getId()));
        rightLayout.clearChildren();

        rightLayout.child(screen.getSymbolChatCompat().getOpenFontSelectionDropDownButton(textBoxComponent));
        rightLayout.child(screen.getSymbolChatCompat().getOpenSymbolChatPanelButton(textBoxComponent));

        rightLayout.children(componentList);
    }

}
