package fzmm.zailer.me.compat.symbol_chat;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.compat.CompatMods;
import fzmm.zailer.me.compat.symbol_chat.components.FontComponentAdapter;
import fzmm.zailer.me.compat.symbol_chat.components.SymbolComponentAdapter;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.replaceitem.symbolchat.SymbolChat;
import net.replaceitem.symbolchat.gui.SymbolSelectionPanel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SymbolChatCompat {

    private static final Text SYMBOL_CHAT_NOT_AVAILABLE_TEXT_TOOLTIP = Text.translatable("fzmm.gui.button.symbolChat.notAvailable.tooltip").setStyle(Style.EMPTY.withColor(0xF2200D));

    private static final Text SYMBOL_BUTTON_TEXT = Text.translatable("fzmm.gui.button.symbolChat.symbol");
    private static final Text SYMBOL_BUTTON_TEXT_TOOLTIP = Text.translatable("fzmm.gui.button.symbolChat.symbol.tooltip");

    private static final Text FONT_BUTTON_TEXT = Text.translatable("fzmm.gui.button.symbolChat.font");
    private static final Text FONT_BUTTON_TEXT_TOOLTIP = Text.translatable("fzmm.gui.button.symbolChat.font.tooltip");

    private TextFieldWidget selectedComponent = null;
    private final SymbolChatComponentHandler<SymbolComponentAdapter> symbolHandler;
    private final SymbolChatComponentHandler<FontComponentAdapter> fontHandler;

    public SymbolChatCompat() {
        this.symbolHandler = new SymbolChatComponentHandler<>(this, SYMBOL_BUTTON_TEXT, SYMBOL_BUTTON_TEXT_TOOLTIP,
                SYMBOL_CHAT_NOT_AVAILABLE_TEXT_TOOLTIP
        );

        this.fontHandler = new SymbolChatComponentHandler<>(this, FONT_BUTTON_TEXT, FONT_BUTTON_TEXT_TOOLTIP,
                SYMBOL_CHAT_NOT_AVAILABLE_TEXT_TOOLTIP
        );
    }

    // == general ==

    /**
     * @return empty list if config general.showSymbolButton is false
     */
    public List<Component> getButtons(BaseFzmmScreen screen, TextFieldWidget selectedComponent) {
        List<Component> result = new ArrayList<>();

        if (FzmmClient.CONFIG.general.showSymbolButton()) {
            result.add(this.fontHandler.initButton(screen, selectedComponent, this::getFontComponent));
            result.add(this.symbolHandler.initButton(screen, selectedComponent, this::getSymbolComponent));
        }

        return result;
    }

    private SymbolComponentAdapter getSymbolComponent() {
        return new SymbolComponentAdapter(new SymbolSelectionPanel(0, 0, SymbolChat.config.getSymbolPanelHeight(), s -> {
            if (this.selectedComponent != null) {
                this.selectedComponent.write(s);
            }
        }));
    }

    private FontComponentAdapter getFontComponent() {
        FontComponentAdapter.CustomDropDownWidget widget = new FontComponentAdapter.CustomDropDownWidget(0, 0, 180, 15,
                SymbolChat.fontManager.getFontProcessors(), SymbolChat.selectedFont);
        int expandedHeight = 150 + widget.getHeight(); // 150 is hardcoded in DropDownWidget
        widget.setHeight(expandedHeight);
        return new FontComponentAdapter(widget, expandedHeight);
    }

    public TextFieldWidget selectedComponent() {
        return this.selectedComponent;
    }

    public SymbolChatComponentHandler<SymbolComponentAdapter> symbol() {
        return this.symbolHandler;
    }

    public SymbolChatComponentHandler<FontComponentAdapter> font() {
        return this.fontHandler;
    }

    public void selectedComponent(@Nullable TextFieldWidget selectedComponent) {
        this.selectedComponent = selectedComponent;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT) {
            return false;
        }

        return this.symbolHandler.charTyped(chr, modifiers) || this.fontHandler.charTyped(chr, modifiers);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT) {
            return false;
        }

        return this.symbolHandler.keyPressed(keyCode, scanCode, modifiers) || this.fontHandler.keyPressed(keyCode, scanCode, modifiers);
    }

    public void processFont(TextFieldWidget widget, String text, Consumer<String> writeConsumer) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT || this.selectedComponent != widget || !this.fontHandler.isMounted()) {
            writeConsumer.accept(text);
            return;
        }

        this.fontHandler.getComponent().ifPresent(fontComponentAdapter -> fontComponentAdapter.processFont(widget, text, writeConsumer));
    }
}
