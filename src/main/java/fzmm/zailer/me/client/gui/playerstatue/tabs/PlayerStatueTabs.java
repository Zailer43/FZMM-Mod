package fzmm.zailer.me.client.gui.playerstatue.tabs;

import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;

import java.util.function.Supplier;

public enum PlayerStatueTabs implements ITabsEnum {
    CREATE(PlayerStatueGenerateTab::new),
    UPDATE(PlayerStatueUpdateTab::new);

    private final Supplier<IPlayerStatueTab> tabSupplier;
    private final String id;

    PlayerStatueTabs(Supplier<IPlayerStatueTab> tabSupplier) {
        this.tabSupplier = tabSupplier;
        this.id = this.createTab().getId();
    }

    @Override
    public IPlayerStatueTab createTab() {
        return this.tabSupplier.get();
    }

    @Override
    public String getId() {
        return this.id;
    }
}
