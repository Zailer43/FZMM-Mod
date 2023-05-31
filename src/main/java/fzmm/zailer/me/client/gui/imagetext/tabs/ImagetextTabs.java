package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;

import java.util.function.Supplier;

public enum ImagetextTabs implements ITabsEnum {
    LORE(ImagetextLoreTab::new),
    BOOK_PAGE(ImagetextBookPageTab::new),
    BOOK_TOOLTIP(ImagetextBookTooltipTab::new),
    TEXT_DISPLAY(ImagetextTextDisplayTab::new),
    SIGN(ImagetextSignTab::new),
    HOLOGRAM(ImagetextHolgoramTab::new),
    COPY(ImagetextCopyTab::new);

    private final Supplier<IImagetextTab> tabSupplier;
    private final String id;

    ImagetextTabs(Supplier<IImagetextTab> tabSupplier) {
        this.tabSupplier = tabSupplier;
        this.id = this.createTab().getId();
    }

    @Override
    public IImagetextTab createTab() {
        return this.tabSupplier.get();
    }

    @Override
    public String getId() {
        return this.id;
    }
}
