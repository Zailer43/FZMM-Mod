package fzmm.zailer.me.compat.symbol_chat;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.compat.CompatMods;
import fzmm.zailer.me.compat.symbol_chat.components.FontComponentAdapter;
import fzmm.zailer.me.compat.symbol_chat.components.SymbolComponentAdapter;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SymbolChatCompat {

    private static final Component SYMBOL_CHAT_NOT_AVAILABLE_TEXT_TOOLTIP = Component.translatable("fzmm.gui.button.symbolChat.notAvailable.tooltip").setStyle(Style.EMPTY.withColor(0xF2200D));

    private static final Component SYMBOL_BUTTON_TEXT = Component.translatable("fzmm.gui.button.symbolChat.symbol");
    private static final Component SYMBOL_BUTTON_TEXT_TOOLTIP = Component.translatable("fzmm.gui.button.symbolChat.symbol.tooltip");

    private static final Component FONT_BUTTON_TEXT = Component.translatable("fzmm.gui.button.symbolChat.font");
    private static final Component FONT_BUTTON_TEXT_TOOLTIP = Component.translatable("fzmm.gui.button.symbolChat.font.tooltip");

    private EditBox selectedComponent = null;
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
    public List<UIComponent> getButtons(BaseFzmmScreen screen, EditBox selectedComponent) {
        List<UIComponent> result = new ArrayList<>();

        if (FzmmClient.CONFIG.general.showSymbolButton()) {
            // avoid direct lambda because it causes NoClassDefFoundError
            result.add(this.fontHandler.initButton(screen, selectedComponent, this::getFontComponent));
            result.add(this.symbolHandler.initButton(screen, selectedComponent, this::getSymbolComponent));
        }

        return result;
    }

    private FontComponentAdapter getFontComponent() {
        return FontComponentAdapter.get();
    }

    private SymbolComponentAdapter getSymbolComponent() {
        return SymbolComponentAdapter.of(s -> {
            if (this.selectedComponent != null) {
                this.selectedComponent.insertText(s);
            }
        });
    }

    public EditBox selectedComponent() {
        return this.selectedComponent;
    }

    public SymbolChatComponentHandler<SymbolComponentAdapter> symbol() {
        return this.symbolHandler;
    }

    public SymbolChatComponentHandler<FontComponentAdapter> font() {
        return this.fontHandler;
    }

    public void selectedComponent(@Nullable EditBox selectedComponent) {
        this.selectedComponent = selectedComponent;
    }

    public boolean charTyped(CharacterEvent input) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT) return false;

        return this.symbolHandler.charTyped(input) || this.fontHandler.charTyped(input);
    }

    public boolean keyPressed(KeyEvent input) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT) return false;

        return this.symbolHandler.keyPressed(input) || this.fontHandler.keyPressed(input);
    }

    public void processFont(EditBox widget, String text, Consumer<String> writeConsumer) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT || this.selectedComponent != widget || !this.fontHandler.isMounted()) {
            writeConsumer.accept(text);
            return;
        }

        this.fontHandler.getComponent().ifPresent(fontComponentAdapter -> fontComponentAdapter.processFont(widget, text, writeConsumer));
    }
}
