package fzmm.zailer.me.client.gui.bannereditor;

import fzmm.zailer.me.client.gui.bannereditor.tabs.AddPatternsTab;
import fzmm.zailer.me.client.gui.bannereditor.tabs.ChangeColorTab;
import fzmm.zailer.me.client.gui.bannereditor.tabs.RemovePatternsTab;
import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;

import java.util.function.Supplier;

public enum BannerEditorTabs implements ITabsEnum {
    ADD_PATTERNS(AddPatternsTab::new),
    CHANGE_COLOR(ChangeColorTab::new),
    REMOVE_PATTERNS(RemovePatternsTab::new);

    private final Supplier<IBannerEditorTab> tabSupplier;
    private final String id;

    BannerEditorTabs(Supplier<IBannerEditorTab> tabSupplier) {
        this.tabSupplier = tabSupplier;
        this.id = this.createTab().getId();
    }

    @Override
    public IBannerEditorTab createTab() {
        return this.tabSupplier.get();
    }

    @Override
    public String getId() {
        return this.id;
    }
}
