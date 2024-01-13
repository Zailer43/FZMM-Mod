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
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.replaceitem.symbolchat.SymbolChat;
import net.replaceitem.symbolchat.font.FontProcessor;
import net.replaceitem.symbolchat.font.Fonts;
import net.replaceitem.symbolchat.gui.SymbolSelectionPanel;
import net.replaceitem.symbolchat.gui.widget.DropDownWidget;

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
    private SymbolSelectionPanel symbolSelectionPanel;
    private DropDownWidget<FontProcessor> fontSelectionDropDownParent;
    private ClickableWidget fontSelectionDropDown;
    private TextFieldWidget selectedComponent = null;
    private int fontSelectionOriginalWidth;
    private Field visibleField;

    public void addSymbolChatComponents(BaseFzmmScreen screen) {
        if (CompatMods.SYMBOL_CHAT_PRESENT) {
            try {
                this.addSymbolSelectionPanelComponent(screen);
                this.addFontSelectionDropDownComponent(screen);
            } catch (Error e) {
                FzmmClient.LOGGER.error("[SymbolChatCompat] Failed to add symbol chat components", e);
                CompatMods.SYMBOL_CHAT_PRESENT = false;
            }
        }
    }

    private void addSymbolSelectionPanelComponent(BaseFzmmScreen screen) {
        this.symbolSelectionPanel = new SymbolSelectionPanel(s -> {
            if (this.selectedComponent != null)
                this.selectedComponent.write(s);
        }, 0, 0, SymbolChat.config.getSymbolPanelHeight());

        try {
            this.visibleField = this.symbolSelectionPanel.getClass().getDeclaredField("visible");
            this.visibleField.setAccessible(true);
            this.setSelectionPanelVisible(false);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolChatCompat] Failed to get visible field", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }

        screen.child(new SymbolSelectionPanelComponentAdapter(this.symbolSelectionPanel, this)
                .positioning(Positioning.relative(0, 0))
                .id(SYMBOL_SELECTION_PANEL_ID)
        );

    }

    private void addFontSelectionDropDownComponent(BaseFzmmScreen screen) {
        this.fontSelectionOriginalWidth = 180;
        this.fontSelectionDropDownParent = new DropDownWidget<>(0, 0, this.fontSelectionOriginalWidth, 15, Fonts.fontProcessors, SymbolChat.selectedFont);

        this.fontSelectionDropDownParent.visible = false;
        this.fontSelectionDropDownParent.expanded = true;

        try {
            Field selectionWidgetField = this.fontSelectionDropDownParent.getClass().getDeclaredField("scrollableGridWidget");
            selectionWidgetField.setAccessible(true);
            // I use the selection widget because it is the really important part,
            // if I want to use the fontSelectionDropDownParent I need to modify the
            // renderButton because if I change its height so that owo-lib allows me to
            // click on the selection widget it puts the text in the middle of the component,
            // and if I don't change its height the selection widget part is completely unclickable,
            // remember: don't use ClickableWidget as parent
            this.fontSelectionDropDown = (ClickableWidget) selectionWidgetField.get(this.fontSelectionDropDownParent);
            this.setFontSelectionVisible(false);

            screen.child(this.fontSelectionDropDown
                    .positioning(Positioning.relative(0, 0))
                    .id(FONT_SELECTION_DROP_DOWN_ID)
                    .zIndex(100)
            );
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolChatCompat] Failed to add font selection drop down component", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }
    }

    public boolean isSelectionPanelVisible() {
        if (!CompatMods.SYMBOL_CHAT_PRESENT)
            return false;

        try {
            return this.visibleField.getBoolean(this.symbolSelectionPanel);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolChatCompat] Failed to get visible field", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
            return false;
        }
    }

    public void setSelectionPanelVisible(boolean visible) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT)
            return;

        try {
            this.visibleField.setBoolean(this.symbolSelectionPanel, visible);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolChatCompat] Failed to set visible field", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }
    }

    public Component getOpenSymbolChatPanelButton(TextFieldWidget selectedComponent) {
        Component result = Components.button(SYMBOL_BUTTON_TEXT, button -> {
            if (this.fontSelectionDropDown.visible)
                this.setFontSelectionVisible(false);

            if (this.selectedComponent == null || !this.isSelectionPanelVisible()) {
                this.setSelectionPanelVisible(!this.isSelectionPanelVisible());
                this.selectedComponent = selectedComponent;

            } else if (this.selectedComponent != selectedComponent) {
                this.selectedComponent = selectedComponent;

            } else {
                this.setSelectionPanelVisible(false);
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
            if (this.isSelectionPanelVisible())
                this.setSelectionPanelVisible(false);

            if (this.selectedComponent == null || !this.fontSelectionDropDown.visible) {
                this.setFontSelectionVisible(true);
                this.selectedComponent = selectedComponent;

            } else if (this.selectedComponent != selectedComponent) {
                this.selectedComponent = selectedComponent;
            } else {
                this.setFontSelectionVisible(false);
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

    public boolean isFontSelectionVisible() {
        if (!CompatMods.SYMBOL_CHAT_PRESENT)
            return false;

        try {
            return this.fontSelectionDropDown.visible;
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolChatCompat] Failed to get visible field", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
            return false;
        }
    }

    public void setFontSelectionVisible(boolean visible) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT)
            return;

        this.fontSelectionDropDown.visible = visible;
        this.fontSelectionDropDown.horizontalSizing(Sizing.fixed(this.fontSelectionDropDown.visible ? this.fontSelectionOriginalWidth : 0));
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
            Object selectedFontProcessor = fontProcessors.get(MathHelper.clamp(this.fontSelectionDropDownParent.selected, 0, fontProcessors.size() - 1));

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

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (CompatMods.SYMBOL_CHAT_PRESENT) {
            return this.symbolSelectionPanel.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (CompatMods.SYMBOL_CHAT_PRESENT) {
            return this.symbolSelectionPanel.charTyped(chr, modifiers);
        }
        return false;
    }
}
