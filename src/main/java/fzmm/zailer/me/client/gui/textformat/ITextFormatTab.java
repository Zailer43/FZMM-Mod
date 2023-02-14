package fzmm.zailer.me.client.gui.textformat;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import net.minecraft.text.Text;

import java.util.function.Consumer;


public interface ITextFormatTab extends IScreenTab {
    Text getText(TextFormatLogic logic);

    void setRandomValues();

    void componentsCallback(Consumer<Object> callback);

    boolean hasStyles();
}
