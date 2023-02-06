package fzmm.zailer.me.client.gui.playerstatue;

import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;
import fzmm.zailer.me.client.gui.playerstatue.tabs.PlayerStatueGenerateTab;
import fzmm.zailer.me.client.gui.playerstatue.tabs.PlayerStatueUpdateTab;

import java.util.function.Supplier;

public enum PlayerStatueTabs implements ITabsEnum {
    CREATE(PlayerStatueGenerateTab::new),
    UPDATE(PlayerStatueUpdateTab::new);

    private final Supplier<IPlayerStatueTab> tabSupplier;
    private final String id;

    PlayerStatueTabs(Supplier<IPlayerStatueTab> tabSupplier) {
        this.tabSupplier = tabSupplier;
        this.id = this.getTab().getId();
    }

    @Override
    public IPlayerStatueTab getTab() {
        return this.tabSupplier.get();
    }

    @Override
    public String getId() {
        return this.id;
    }
}
