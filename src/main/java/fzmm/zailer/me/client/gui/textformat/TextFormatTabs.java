package fzmm.zailer.me.client.gui.textformat;

import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;
import fzmm.zailer.me.client.gui.textformat.tabs.TextFormatGradientTab;
import fzmm.zailer.me.client.gui.textformat.tabs.TextFormatInterleavedColorsTab;
import fzmm.zailer.me.client.gui.textformat.tabs.TextFormatRainbowTab;
import fzmm.zailer.me.client.gui.textformat.tabs.TextFormatSimpleTab;

import java.util.function.Supplier;

public enum TextFormatTabs implements ITabsEnum {
    SIMPLE(TextFormatSimpleTab::new),
    GRADIENT(TextFormatGradientTab::new),
    RAINBOW(TextFormatRainbowTab::new),
    INTERLEAVED(TextFormatInterleavedColorsTab::new);

    private final Supplier<ITextFormatTab> tabSupplier;
    private final String id;

    TextFormatTabs(Supplier<ITextFormatTab> tabSupplier) {
        this.tabSupplier = tabSupplier;
        this.id = this.getTab().getId();
    }

    @Override
    public ITextFormatTab getTab() {
        return this.tabSupplier.get();
    }

    @Override
    public String getId() {
        return this.id;
    }
}