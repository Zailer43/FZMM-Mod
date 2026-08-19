package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.compat.symbol_chat.components.FontTextBoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

public class TextBoxRow extends AbstractRow {

    public TextBoxRow(String baseTranslationKey, String id, String tooltipId, boolean symbolChatButtons) {
        super(baseTranslationKey, id, tooltipId, true);

        FontTextBoxComponent fontTextBoxComponent = this.childById(FontTextBoxComponent.class, getTextBoxId(id));
        if (fontTextBoxComponent != null)
            fontTextBoxComponent.enableFontProcess(symbolChatButtons);
    }

    @Override
    public UIComponent[] getComponents(String id, String tooltipId) {
        UIComponent textBox = new FontTextBoxComponent(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(getTextBoxId(id));

        return new UIComponent[]{
                textBox
        };
    }

    public static String getTextBoxId(String id) {
        return id + "-text-box";
    }


    public static TextBoxComponent setup(EFlowLayout rootComponent, String id, String defaultValue, int maxLength) {
        return setup(rootComponent, id, defaultValue, maxLength, null);
    }

    public static TextBoxComponent setup(EFlowLayout rootComponent, String id, String defaultValue, int maxLength, @Nullable Consumer<String> changedListener) {
        TextBoxComponent textBox = rootComponent.childByIdOrThrow(TextBoxComponent.class, getTextBoxId(id));
        ButtonComponent resetButton = rootComponent.childById(ButtonComponent.class, getResetButtonId(id));

        textBox.onChanged().subscribe(text -> {
            if (resetButton != null)
                resetButton.active = !textBox.getValue().equals(defaultValue);
            if (changedListener != null)
                changedListener.accept(text);
        });
        textBox.setMaxLength(maxLength);
        textBox.text(defaultValue);

        if (resetButton != null)
            resetButton.onPress(button -> textBox.text(defaultValue));
        return textBox;
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

        TextBoxRow row = new TextBoxRow(baseTranslationKey, id, tooltipId, symbolChatButtons);
        if (removeHorizontalMargins)
            row.removeHorizontalMargins();

        if (removeResetButton)
            row.removeResetButton();

        TextBoxComponent textBox = row.childById(TextBoxComponent.class, getTextBoxId(id));
        Screen screen = Minecraft.getInstance().gui.screen();

        if (symbolChatButtons && screen instanceof BaseFzmmScreen baseFzmmScreen && textBox != null)
            row.addSymbolChatButtons(baseFzmmScreen, textBox);
        return row;
    }


    public void addSymbolChatButtons(BaseFzmmScreen screen, EditBox textFieldWidget) {
        List<UIComponent> symbolChatButtons = screen.getSymbolChatCompat().getButtons(screen, textFieldWidget);
        if (symbolChatButtons.isEmpty()) {
            return;
        }

        Optional<FlowLayout> rightLayoutOptional = this.getRightLayout();
        if (rightLayoutOptional.isEmpty()) {
            return;
        }

        FlowLayout rightLayout = rightLayoutOptional.get();
        List<UIComponent> componentList = List.copyOf(rightLayout.children());

        // sort symbol chat buttons at left and original buttons at right
        rightLayout.clearChildren();
        rightLayout.children(symbolChatButtons);

        rightLayout.children(componentList);
    }

}
