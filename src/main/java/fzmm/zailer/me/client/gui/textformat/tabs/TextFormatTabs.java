package fzmm.zailer.me.client.gui.textformat.tabs;

import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;

import java.util.function.Supplier;

public enum TextFormatTabs implements ITabsEnum {
    SIMPLE(TextFormatSimpleTab::new),
    GRADIENT(TextFormatGradientTab::new),
    RAINBOW(TextFormatRainbowTab::new),
    INTERLEAVED(TextFormatInterleavedColorsTab::new),
    PLACEHOLDER_API(TextFormatPlaceholderApiTab::new);

    private final Supplier<ITextFormatTab> tabSupplier;
    private final String id;

    TextFormatTabs(Supplier<ITextFormatTab> tabSupplier) {
        this.tabSupplier = tabSupplier;
        this.id = this.createTab().getId();
    }

    @Override
    public ITextFormatTab createTab() {
        return this.tabSupplier.get();
    }

    @Override
    public String getId() {
        return this.id;
    }
}