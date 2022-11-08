package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.imagetext.tabs.*;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;

public enum ImagetextTabs implements IImagetextTab {
    LORE(new ImagetextLoreTab()),
    BOOK_PAGE(new ImagetextBookPageTab()),
    BOOK_TOOLTIP(new ImagetextBookTooltipTab()),
    HOLOGRAM(new ImagetextHolgoramTab()),
    JSON(new ImagetextJsonTab());

    private final IImagetextTab tab;

    ImagetextTabs(IImagetextTab tab) {
        this.tab = tab;
    }

    @Override
    public void execute(ImagetextLogic logic) {
        this.tab.execute(logic);
    }

    @Override
    public String getId() {
        return this.tab.getId();
    }

    @Override
    public Component[] getComponents(BaseFzmmScreen parent) {
        return this.tab.getComponents(parent);
    }

    @Override
    public void setupComponents(BaseFzmmScreen parent, FlowLayout rootComponent) {
        this.tab.setupComponents(parent, rootComponent);
    }
}
