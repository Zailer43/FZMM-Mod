package fzmm.zailer.me.compat.symbolChat.symbol;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class CustomSymbolSelectionPanel {

    public static AbstractParentElement of(Screen screen, int x, int y) {
        if (!FzmmClient.SYMBOL_CHAT_PRESENT)
            return null;

        return new net.replaceitem.symbolchat.gui.SymbolSelectionPanel(screen, x, y) {
            private TextFieldWidget activeTextField;

            public boolean setActiveTextField(TextFieldWidget activeTextField) {
                if (activeTextField == this.activeTextField)
                    return true;

                this.activeTextField = activeTextField;
                return false;
            }

            @Override
            public void onSymbolPasted(String symbol) {
                if (this.activeTextField != null)
                    this.activeTextField.write(symbol);
            }
        };
    }


}
