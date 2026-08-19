package fzmm.zailer.me.compat.symbol_chat.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;

@SuppressWarnings("UnstableApiUsage")
public class FontTextBoxComponent extends ConfigTextBox {
    private boolean fontProcessEnabled;

    public FontTextBoxComponent(Sizing horizontalSizing) {
        super();
        this.horizontalSizing(horizontalSizing);
        this.fontProcessEnabled = false;
    }

    public void enableFontProcess(boolean enabled) {
        this.fontProcessEnabled = enabled;
    }

    @Override
    public void insertText(String text) {
        if (!this.fontProcessEnabled) {
            super.insertText(text);
        } else if (Minecraft.getInstance().gui.screen() instanceof BaseFzmmScreen screen) {
            screen.getSymbolChatCompat().processFont(this, text, super::insertText);
        }
    }

}
