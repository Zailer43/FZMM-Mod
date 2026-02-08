package fzmm.zailer.me.client.gui.text_format.tabs;

import fzmm.zailer.me.client.gui.components.tabs.ITab;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public interface ITextFormatTab extends ITab {
    Component getText(TextFormatLogic logic);

    void setRandomValues();

    void componentsCallback(Consumer<Object> callback);

    boolean hasStyles();

    @Override
    default String getTranslationKey() {
        return "fzmm.gui.textFormat.tab." + this.getId();
    }
}
