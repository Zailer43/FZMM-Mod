package fzmm.zailer.me.compat.symbolChat.font;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;

public class FontTextBoxComponent extends TextBoxComponent {
    private boolean enabled;

    public FontTextBoxComponent(Sizing horizontalSizing) {
        super(horizontalSizing);
        this.enabled = false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void write(String text) {
        if (!this.enabled) {
            super.write(text);
        } else if (MinecraftClient.getInstance().currentScreen instanceof BaseFzmmScreen screen) {
            screen.getSymbolChatCompat().processFont(this, text, super::write);
        }
    }

}
