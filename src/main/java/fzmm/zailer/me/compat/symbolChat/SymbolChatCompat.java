package fzmm.zailer.me.compat.symbolChat;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.compat.CompatMods;
import fzmm.zailer.me.compat.symbolChat.symbol.SymbolSelectionPanelComponentAdapter;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

public class SymbolChatCompat {

    private static final Text SYMBOL_CHAT_NOT_AVAILABLE_TEXT_TOOLTIP = Text.translatable("fzmm.gui.button.symbolChat.notAvailable.tooltip").setStyle(Style.EMPTY.withColor(0xF2200D));

    private static final Text SYMBOL_BUTTON_TEXT = Text.translatable("fzmm.gui.button.symbolChat.symbol");
    private static final Text SYMBOL_BUTTON_TEXT_TOOLTIP = Text.translatable("fzmm.gui.button.symbolChat.symbol.tooltip");
    private static final Text SYMBOL_BUTTON_NOT_AVAILABLE_TOOLTIP = SYMBOL_BUTTON_TEXT_TOOLTIP.copy().append("\n\n").append(SYMBOL_CHAT_NOT_AVAILABLE_TEXT_TOOLTIP);

    private static final Text FONT_BUTTON_TEXT = Text.translatable("fzmm.gui.button.symbolChat.font");
    private static final Text FONT_BUTTON_TEXT_TOOLTIP = Text.translatable("fzmm.gui.button.symbolChat.font.tooltip");
    private static final Text FONT_BUTTON_NOT_AVAILABLE_TOOLTIP = FONT_BUTTON_TEXT_TOOLTIP.copy().append("\n\n").append(SYMBOL_CHAT_NOT_AVAILABLE_TEXT_TOOLTIP);

    public static final String SYMBOL_SELECTION_PANEL_ID = "symbol-selection-panel";
    public static final String FONT_SELECTION_DROP_DOWN_ID = "font-selection-drop-down";
    private AbstractParentElement symbolSelectionPanel;
    private ClickableWidget fontSelectionDropDownParent;
    private ClickableWidget fontSelectionDropDown;
    private Field fontProcessorSelectedField;
    private boolean symbolSelectionPanelVisible = false;
    private Field symbolSelectionPanelVisibleField;
    private TextFieldWidget selectedComponent = null;

    public void addSymbolChatComponents(BaseFzmmScreen screen) {
        if (CompatMods.SYMBOL_CHAT_PRESENT) {
            this.addSymbolSelectionPanelComponent(screen);
            this.addFontSelectionDropDownComponent(screen);
        }
    }

    private void addSymbolSelectionPanelComponent(BaseFzmmScreen screen) {
        try {
            Class<?> symbolSelectionPanelClass = Class.forName("net.replaceitem.symbolchat.gui.SymbolSelectionPanel");
            Constructor<?> symbolSelectionPanelClassConstructor = symbolSelectionPanelClass.getConstructor(Consumer.class, int.class, int.class);
            this.symbolSelectionPanel = (AbstractParentElement) symbolSelectionPanelClassConstructor.newInstance((Consumer<String>) s -> {
                if (this.selectedComponent != null)
                    this.selectedComponent.write(s);
            }, 0, 0);

            this.symbolSelectionPanelVisibleField = symbolSelectionPanelClass.getField("visible");
            this.symbolSelectionPanelVisibleField.setBoolean(this.symbolSelectionPanel, this.symbolSelectionPanelVisible);

            screen.child(new SymbolSelectionPanelComponentAdapter(this.symbolSelectionPanel, symbolSelectionPanelVisibleField, symbolSelectionPanelClass)
                    .positioning(Positioning.relative(0, 0))
                    .id(SYMBOL_SELECTION_PANEL_ID)
            );


        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolChatCompat] Failed to add symbol selection panel", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }
    }

    private void addFontSelectionDropDownComponent(BaseFzmmScreen screen) {
        try {
            List<?> fontProcessorList = (List<?>) Class.forName("net.replaceitem.symbolchat.font.Fonts").getDeclaredField("fontProcessors").get(null);
            int selectedFontIndex = Class.forName("net.replaceitem.symbolchat.SymbolChat").getDeclaredField("selectedFont").getInt(null);

            Class<?> dropDownWidgetClass = Class.forName("net.replaceitem.symbolchat.gui.widget.DropDownWidget");
            Constructor<?> dropDownWidgetClassConstructor = dropDownWidgetClass.getConstructor(int.class, int.class, int.class, int.class, List.class, int.class);

            this.fontSelectionDropDownParent = (ClickableWidget) dropDownWidgetClassConstructor.newInstance(0, 0, 180, 15, fontProcessorList, selectedFontIndex);

            this.fontSelectionDropDownParent.visible = false;
            Field isExpandedField = dropDownWidgetClass.getDeclaredField("expanded");
            isExpandedField.setBoolean(this.fontSelectionDropDownParent, true);

            Field selectionWidgetField = dropDownWidgetClass.getDeclaredField("selectionWidget");
            selectionWidgetField.setAccessible(true);
            this.fontProcessorSelectedField = dropDownWidgetClass.getDeclaredField("selected");
            // I use the selection widget because it is the really important part,
            // if I want to use the fontSelectionDropDownParent I need to modify the
            // renderButton because if I change its height so that owo-lib allows me to
            // click on the selection widget it puts the text in the middle of the component,
            // and if I don't change its height the selection widget part is completely unclickable,
            // remember: don't use ClickableWidget as parent
            this.fontSelectionDropDown = (ClickableWidget) selectionWidgetField.get(fontSelectionDropDownParent);
            this.fontSelectionDropDown.visible = false;

            screen.child(this.fontSelectionDropDown
                    .positioning(Positioning.relative(0, 0))
                    .id(FONT_SELECTION_DROP_DOWN_ID)
                    .zIndex(100)
            );

        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolChatCompat] Failed to add font selection drop down", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }
    }

    public Component getOpenSymbolChatPanelButton(TextFieldWidget selectedComponent) {
        Component result = Components.button(SYMBOL_BUTTON_TEXT, button -> {
            if (this.fontSelectionDropDown.visible)
                this.fontSelectionDropDown.visible = false;

            if (this.selectedComponent == null || !this.symbolSelectionPanelVisible) {
                this.toggleSymbolChatPanelVisibility();
                this.selectedComponent = selectedComponent;

            } else if (this.selectedComponent != selectedComponent) {
                this.selectedComponent = selectedComponent;

            } else {
                this.toggleSymbolChatPanelVisibility();
                this.selectedComponent = null;
            }
        });
        result.sizing(Sizing.fixed(20));

        ((ButtonComponent) result).active = CompatMods.SYMBOL_CHAT_PRESENT;

        if (CompatMods.SYMBOL_CHAT_PRESENT) {
            result.tooltip(SYMBOL_BUTTON_TEXT_TOOLTIP);
        } else {
            result = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(result)
                    .tooltip(SYMBOL_BUTTON_NOT_AVAILABLE_TOOLTIP);
        }

        return result;
    }

    public Component getOpenFontSelectionDropDownButton(TextFieldWidget selectedComponent) {
        Component result = Components.button(FONT_BUTTON_TEXT, button -> {
            if (this.symbolSelectionPanelVisible)
                this.toggleSymbolChatPanelVisibility();

            if (this.selectedComponent == null || !this.fontSelectionDropDown.visible) {
                this.fontSelectionDropDown.visible = true;
                this.selectedComponent = selectedComponent;

            } else if (this.selectedComponent != selectedComponent) {
                this.selectedComponent = selectedComponent;
            } else {
                this.fontSelectionDropDown.visible = false;
                this.selectedComponent = null;
            }
        });
        result.sizing(Sizing.fixed(20));

        ((ButtonComponent) result).active = CompatMods.SYMBOL_CHAT_PRESENT;

        if (CompatMods.SYMBOL_CHAT_PRESENT) {
            result.tooltip(FONT_BUTTON_TEXT_TOOLTIP);
        } else {
            result = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(result)
                    .tooltip(FONT_BUTTON_NOT_AVAILABLE_TOOLTIP);
        }

        return result;
    }

    public void toggleSymbolChatPanelVisibility() {
        try {
            this.symbolSelectionPanelVisibleField.setBoolean(this.symbolSelectionPanel, !this.symbolSelectionPanelVisible);
            this.symbolSelectionPanelVisible = !this.symbolSelectionPanelVisible;
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolChatCompat] Failed to toggle symbol chat panel visibility", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }
    }

    public void processFont(TextFieldWidget widget, String text, Consumer<String> writeConsumer) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT) {
            writeConsumer.accept(text);
            return;
        }

        try {
            Class<?> fontProcessorClass = Class.forName("net.replaceitem.symbolchat.font.FontProcessor");
            Class<?> fontsClass = Class.forName("net.replaceitem.symbolchat.font.Fonts");

            List<?> fontProcessors = (List<?>) fontsClass.getField("fontProcessors").get(null);
            Object selectedFontProcessor = fontProcessors.get(MathHelper.clamp(this.fontProcessorSelectedField.getInt(this.fontSelectionDropDownParent), 0, fontProcessors.size() - 1));

            text = (String) fontProcessorClass.getMethod("convertString", String.class).invoke(selectedFontProcessor, text);
            writeConsumer.accept(text);


            if (selectedFontProcessor == fontsClass.getField("INVERSE").get(null)) {
                int pos = widget.getCursor() - text.length();
                widget.setSelectionStart(pos);
                widget.setSelectionEnd(pos);
            }
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolChatCompat] Failed to process font", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }
    }

}
