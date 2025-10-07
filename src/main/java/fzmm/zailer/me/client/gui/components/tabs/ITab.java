package fzmm.zailer.me.client.gui.components.tabs;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import net.minecraft.text.Text;

public interface ITab {

    void setupComponents(EFlowLayout rootComponent);

    String getId();

    String getTranslationKey();

    default Text getButtonText() {
        return Text.translatable(this.getTranslationKey());
    }
}
