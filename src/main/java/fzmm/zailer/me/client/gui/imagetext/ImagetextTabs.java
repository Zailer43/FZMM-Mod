package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;
import fzmm.zailer.me.client.gui.imagetext.tabs.*;

import java.util.function.Supplier;

public enum ImagetextTabs implements ITabsEnum {
    LORE(ImagetextLoreTab::new),
    BOOK_PAGE(ImagetextBookPageTab::new),
    BOOK_TOOLTIP(ImagetextBookTooltipTab::new),
    HOLOGRAM(ImagetextHolgoramTab::new),
    SIGN(ImagetextSignTab::new),
    COPY(ImagetextCopyTab::new);

    private final Supplier<IImagetextTab> tabSupplier;
    private final String id;

    ImagetextTabs(Supplier<IImagetextTab> tabSupplier) {
        this.tabSupplier = tabSupplier;
        this.id = this.getTab().getId();
    }

    @Override
    public IImagetextTab getTab() {
        return this.tabSupplier.get();
    }

    @Override
    public String getId() {
        return this.id;
    }
}
