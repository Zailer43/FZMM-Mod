package fzmm.zailer.me.compat.symbolChat.symbol;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public record CustomSymbolSelectionPanel(AbstractParentElement parent, AtomicReference<TextFieldWidget> activeTextFieldReference) {

    @Nullable
    public static CustomSymbolSelectionPanel of(int x, int y) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT)
            return null;

        AtomicReference<TextFieldWidget> activeTextField = new AtomicReference<>(null);
        AbstractParentElement parent;
        try {
            parent = new net.replaceitem.symbolchat.gui.SymbolSelectionPanel(symbol -> {
                if (activeTextField.get() != null)
                    activeTextField.get().write(symbol);
            }, x, y);
        } catch (Throwable e) {
            FzmmClient.LOGGER.error("Failed to create CustomSymbolSelectionPanel", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
            return null;
        }

        return new CustomSymbolSelectionPanel(parent, activeTextField);
    }

}
