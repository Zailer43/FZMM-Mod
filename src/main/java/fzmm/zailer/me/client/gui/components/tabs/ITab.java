package fzmm.zailer.me.client.gui.components.tabs;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import net.minecraft.network.chat.Component;

public interface ITab {

    void setupComponents(EFlowLayout rootComponent);

    String getId();

    String getTranslationKey();

    default Component getButtonText() {
        return Component.translatable(this.getTranslationKey());
    }
}
