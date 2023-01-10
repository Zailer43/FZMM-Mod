package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.client.gui.imagetext.tabs.*;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import io.wispforest.owo.ui.container.FlowLayout;

public enum ImagetextTabs implements IImagetextTab {
    LORE(new ImagetextLoreTab()),
    BOOK_PAGE(new ImagetextBookPageTab()),
    BOOK_TOOLTIP(new ImagetextBookTooltipTab()),
    HOLOGRAM(new ImagetextHolgoramTab()),
    SIGN(new ImagetextSignTab()),
    COPY(new ImagetextCopyTab());

    private final IImagetextTab tab;

    ImagetextTabs(IImagetextTab tab) {
        this.tab = tab;
    }

    @Override
    public void generate(ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        this.tab.generate(logic, data, isExecute);
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
    public void setupComponents(FlowLayout rootComponent) {
        this.tab.setupComponents(rootComponent);
    }
}
