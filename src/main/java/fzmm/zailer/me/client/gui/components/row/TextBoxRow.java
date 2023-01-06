package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.compat.symbolChat.symbol.SymbolButtonComponent;
import fzmm.zailer.me.compat.symbolChat.symbol.SymbolSelectionPanelComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TextBoxRow extends AbstractRow {
//    private static final Text FONT_BUTTON_TEXT = Text.translatable("fzmm.gui.button.chatSymbol.font");
    private static final Text SYMBOL_BUTTON_TEXT = Text.translatable("fzmm.gui.button.chatSymbol.symbol");
    private static final Text CHAT_SYMBOL_NOT_AVAILABLE_TEXT = Text.translatable("fzmm.gui.button.chatSymbol.notAvailable.note").setStyle(Style.EMPTY.withColor(0xF2200D));
//    private static final Text NOT_AVAILABLE_FONT_BUTTON_TEXT = FONT_BUTTON_TEXT.copy().append("\n\n").append(CHAT_SYMBOL_NOT_AVAILABLE_TEXT);
    private static final Text NOT_AVALIBLE_SYMBOL_BUTTON_TEXT = SYMBOL_BUTTON_TEXT.copy().append("\n\n").append(CHAT_SYMBOL_NOT_AVAILABLE_TEXT);


    public TextBoxRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, true);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        Component textBox = Components
                .textBox(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(getTextBoxId(id));

        return new Component[]{
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
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);
        boolean removeResetButton = UIParsing.childElements(element).containsKey("removeResetButton") &&
                UIParsing.parseBool(UIParsing.childElements(element).get("removeResetButton"));

        boolean removeHorizontalMargins = UIParsing.childElements(element).containsKey("removeHorizontalMargins") &&
                UIParsing.parseBool(UIParsing.childElements(element).get("removeHorizontalMargins"));

        boolean symbolButton = UIParsing.childElements(element).containsKey("symbolButton") &&
                UIParsing.parseBool(UIParsing.childElements(element).get("symbolButton"));

//        boolean fontButton = UIParsing.childElements(element).containsKey("fontButton") &&
//                UIParsing.parseBool(UIParsing.childElements(element).get("fontButton"));

        int fieldSize = UIParsing.childElements(element).containsKey("removeHorizontalMargins") ?
                UIParsing.parseSignedInt(UIParsing.childElements(element).get("fieldSize")) : -1;

        TextBoxRow row = new TextBoxRow(baseTranslationKey, id, tooltipId);
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
        if (symbolButton && screen instanceof BaseFzmmScreen baseFzmmScreen)
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

//        if (fontButton)
//            rightLayout.child(this.addFontButton(screen));

        rightLayout.child(this.addSymbolButton(screen, textBoxComponent));

        rightLayout.children(componentList);
    }

    public Component addSymbolButton(BaseFzmmScreen screen, TextBoxComponent textBoxComponent) {
        if (screen.getSymbolSelectionPanel().isPresent()) {
            SymbolSelectionPanelComponent symbolSelectionPanel = screen.getSymbolSelectionPanel().get();

            net.replaceitem.symbolchat.gui.widget.symbolButton.SymbolButtonWidget button =
                    new net.replaceitem.symbolchat.gui.widget.symbolButton.OpenSymbolPanelButtonWidget(MinecraftClient.getInstance().currentScreen, 0, 0, 20, 20, (net.replaceitem.symbolchat.gui.SymbolSelectionPanel) symbolSelectionPanel.getSymbolSelectionPanel());
            Optional<ButtonComponent> component = SymbolButtonComponent.of(symbolSelectionPanel.getSymbolSelectionPanel(), button, textBoxComponent);
            if (component.isPresent())
                return component.get()
                        .tooltip(SYMBOL_BUTTON_TEXT)
                        .margins(Insets.left(BaseFzmmScreen.COMPONENT_DISTANCE));
        }

        ButtonComponent button = (ButtonComponent) Components.button(Text.literal("☺"), buttonComponent -> {
                }).sizing(Sizing.fixed(20), Sizing.fixed(20))
                .tooltip(NOT_AVALIBLE_SYMBOL_BUTTON_TEXT)
                .margins(Insets.left(BaseFzmmScreen.COMPONENT_DISTANCE));

        button.active = false;
        return button;
    }

//    public Component addFontButton(BaseFzmmScreen screen) {
//        if (screen.getFontSelectionDropDown().isPresent()) {
//            FontSelectionDropDownComponent fontSelection = screen.getFontSelectionDropDown().get();
//            return Components.button(Text.literal("T"), buttonComponent -> {
//                        boolean isVisible = !fontSelection.isVisible();
//                        fontSelection.setExpanded(isVisible);
//                        fontSelection.setVisible(isVisible);
//
//                        Optional<SymbolSelectionPanelComponent> symbolSelectionPanel = screen.getSymbolSelectionPanel();
//
//                        if (symbolSelectionPanel.isPresent() && SymbolButtonComponent.isVisible(symbolSelectionPanel.get().getSymbolSelectionPanel()))
//                            SymbolButtonComponent.setVisible(symbolSelectionPanel.get().getSymbolSelectionPanel(), false);
//
//                    }).sizing(Sizing.fixed(20), Sizing.fixed(20))
//                    .tooltip(FONT_BUTTON_TEXT);
//        }
//
//        ButtonComponent button = (ButtonComponent) Components.button(Text.literal("T"), buttonComponent -> {
//                }).sizing(Sizing.fixed(20), Sizing.fixed(20))
//                .tooltip(NOT_AVAILABLE_FONT_BUTTON_TEXT);
//
//        button.active = false;
//        return button;
//    }
}
